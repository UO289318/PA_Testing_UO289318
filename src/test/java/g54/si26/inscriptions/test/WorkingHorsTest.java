package g54.si26.inscriptions.test;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import g54.si26.utils.Database;
import g54.si26.utils.Util; // Asumo que aquí tienes tu dateToIsoString / isoStringToDate
import g54.si26.inscriptions.*;

public class WorkingHorsTest {

    @Test
    public void testWeekendIsIgnoredIn48hRule() {
        // --- 1. PREPARACIÓN DEL ESCENARIO ---
        Database db = new Database();
        
        // Limpiamos los datos del curso 98 por si el test se corre varias veces
        db.executeUpdate("DELETE FROM Inscription WHERE action_id = 98");
        db.executeUpdate("DELETE FROM FormativeAction WHERE action_id = 98");
        
        // Insertamos un curso de prueba (ID 98)
        db.executeUpdate("INSERT INTO FormativeAction (action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, fee, status, teacher_id) "
                       + "VALUES (98, 'Curso Supervivencia Fin de Semana', 20, '2026-05-01', '2026-05-15', '10', '2025-01-01', '2026-12-31', 'Online', 100.0, 'ACTIVE', 1)");
        
        // Insertamos un profesional (si no existe) y su inscripción.
        // ¡OJO A LA FECHA! Jueves 26 de Febrero de 2026 a las 18:00
        db.executeUpdate("INSERT INTO Professional (professional_id, name, surname, phone, email) "
                       + "VALUES (999, 'Test', 'Weekend', '600999999', 'weekend@test.com') "
                       + "ON CONFLICT(email) DO NOTHING"); // SQLite syntax por si ya existe
        
        db.executeUpdate("INSERT INTO Inscription (inscription_date, fee, state, professional_id, action_id) "
                       + "VALUES ('2026-02-26 18:00:00', 100.0, 'RECEIVED', 999, 98)");
        
        System.out.println("Escenario preparado: Inscripción realizada el JUEVES 26/02/2026 a las 18:00.");
        
        InscriptionsModel model = new InscriptionsModel();

        // --- 2. PRIMERA COMPROBACIÓN: VIAJAMOS AL LUNES POR LA MAÑANA ---
        // Lunes 2 de Marzo de 2026 a las 10:00. 
        // Han pasado casi 4 días reales, pero LABORABLES solo han pasado unas 16 horas.
        Date simulatedMonday = Util.isoStringToDate("2026-03-02 10:00:00");
        System.out.println("\nViajando al LUNES 02/03/2026 a las 10:00...");
        
        model.checkAndReleaseExpiredBookings(simulatedMonday);
        
        // Verificamos en BD. ¡Debería seguir en RECEIVED!
        String stateMonday = getInscriptionState(db, 98, 999);
        System.out.println("Estado de la inscripción el Lunes: " + stateMonday);
        assertEquals("RECEIVED", stateMonday, "❌ FALLO: El sistema ha cancelado la inscripción antes de tiempo. ¡Está contando el fin de semana!");


        // --- 3. SEGUNDA COMPROBACIÓN: VIAJAMOS AL MARTES POR LA TARDE ---
        // Martes 3 de Marzo de 2026 a las 19:00.
        // Ahora sí, han pasado más de 48h laborables desde el jueves a las 18:00.
        Date simulatedTuesday = Util.isoStringToDate("2026-03-03 19:00:00");
        System.out.println("\nViajando al MARTES 03/03/2026 a las 19:00...");
        
        model.checkAndReleaseExpiredBookings(simulatedTuesday);
        
        // Verificamos en BD. ¡Ahora sí debería estar CANCELLED!
        String stateTuesday = getInscriptionState(db, 98, 999);
        System.out.println("Estado de la inscripción el Martes: " + stateTuesday);
        assertEquals("CANCELLED", stateTuesday, "❌ FALLO: El sistema NO ha cancelado la inscripción después de 48h laborables.");
        
        System.out.println("\n✅ TEST SUPERADO: El algoritmo calcula perfectamente las horas laborables saltando el fin de semana.");
    }

    /**
     * Método auxiliar para ir a buscar el estado a la base de datos de forma limpia.
     */
    private String getInscriptionState(Database db, int actionId, int profId) {
        String sql = "SELECT state FROM Inscription WHERE action_id = ? AND professional_id = ?";
        List<Object[]> result = db.executeQueryArray(sql, actionId, profId);
        if (result.isEmpty()) return "NO_EXISTE";
        return result.get(0)[0].toString();
    }
}