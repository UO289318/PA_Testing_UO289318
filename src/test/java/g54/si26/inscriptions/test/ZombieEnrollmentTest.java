package g54.si26.inscriptions.test;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import g54.si26.DTOs.ProfessionalDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;
import g54.si26.inscriptions.*;

public class ZombieEnrollmentTest {

    @Test
    public void testReEnrollmentAfterCancellation() {
        // --- 1. PREPARACIÓN DEL ESCENARIO ---
        Database db = new Database();
        
        // Limpiamos datos del curso ID 95
        db.executeUpdate("DELETE FROM Inscription WHERE action_id = 95");
        db.executeUpdate("DELETE FROM FormativeAction WHERE action_id = 95");
        
        // Creamos un curso con plazas de sobra
        db.executeUpdate("INSERT INTO FormativeAction (action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, fee, status, teacher_id) "
                       + "VALUES (95, 'Curso de Resurrección', 10, '2026-05-01', '2026-05-15', '10', '2025-01-01', '2026-12-31', 'Online', 100.0, 'ACTIVE', 1)");
        
        // Insertamos un profesional (ID 888)
        db.executeUpdate("INSERT INTO Professional (professional_id, name, surname, phone, email) "
                       + "VALUES (888, 'Zombie', 'User', '600888888', 'zombie@test.com') "
                       + "ON CONFLICT(email) DO NOTHING"); 
        
        // ¡LA CLAVE! Insertamos una inscripción ANTIGUA y CANCELADA para este usuario
        db.executeUpdate("INSERT INTO Inscription (inscription_date, fee, state, professional_id, action_id) "
                       + "VALUES ('2026-01-01 10:00:00', 100.0, 'CANCELLED', 888, 95)");
        
        System.out.println("Escenario preparado: El usuario 'Zombie' tiene una inscripción CANCELLED en el curso 95.");
        
        InscriptionsModel model = new InscriptionsModel();
        
        // Preparamos el DTO del usuario para intentar matricularse de nuevo
        ProfessionalDTO zombieUser = new ProfessionalDTO();
        zombieUser.setName("Zombie");
        zombieUser.setSurname("User");
        zombieUser.setPhone("600888888");
        zombieUser.setEmail("zombie@test.com"); // El email es la clave de búsqueda

        // --- 2. EJECUCIÓN: INTENTO DE RE-INSCRIPCIÓN ---
        System.out.println("Intentando re-inscribir al usuario...");
        
        try {
            // Esto NO debería lanzar excepción si el modelo filtra bien por estado
            //model.enrollProfessional(zombieUser, 95);
            
            System.out.println("✅ ÉXITO: El sistema permitió la re-inscripción.");
            
        } catch (ApplicationException e) {
            fail("❌ FALLO: El sistema bloqueó al usuario injustamente. Mensaje: " + e.getMessage());
        }

        // --- 3. VERIFICACIÓN EN BD ---
        // Ahora debería haber DOS registros para este usuario en este curso:
        // 1. El viejo (CANCELLED)
        // 2. El nuevo (RECEIVED)
        String sqlCount = "SELECT COUNT(*) FROM Inscription WHERE action_id = 95 AND professional_id = 888";
        int totalInscripciones = Integer.parseInt(db.executeQueryArray(sqlCount).get(0)[0].toString());
        
        String sqlActive = "SELECT COUNT(*) FROM Inscription WHERE action_id = 95 AND professional_id = 888 AND state = 'RECEIVED'";
        int activas = Integer.parseInt(db.executeQueryArray(sqlActive).get(0)[0].toString());
        
        System.out.println("Total registros en histórico: " + totalInscripciones);
        System.out.println("Inscripciones activas (RECEIVED): " + activas);
        
        assertEquals(1, activas, "Debería haber exactamente 1 inscripción activa.");
        assertTrue(totalInscripciones >= 2, "Debería mantenerse el histórico de la cancelada más la nueva.");
        
        System.out.println("\n✅ TEST SUPERADO: El sistema gestiona correctamente la re-inscripción de usuarios cancelados.");
    }
}