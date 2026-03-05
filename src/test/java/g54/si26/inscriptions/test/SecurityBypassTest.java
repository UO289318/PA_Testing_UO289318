package g54.si26.inscriptions.test;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import g54.si26.DTOs.ProfessionalDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;
import g54.si26.inscriptions.*;

public class SecurityBypassTest {

    @Test
    public void testCannotEnrollInClosedCourse() {
        // --- 1. PREPARACIÓN DEL ESCENARIO ---
        Database db = new Database();
        
        // Limpiamos la base de datos para el curso fantasma (ID 96)
        db.executeUpdate("DELETE FROM Inscription WHERE action_id = 96");
        db.executeUpdate("DELETE FROM FormativeAction WHERE action_id = 96");
        
        // ¡LA TRAMPA! Insertamos un curso que tiene plazas (10) y está en plazo, pero su estado es 'CLOSED'
        db.executeUpdate("INSERT INTO FormativeAction (action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, fee, status, teacher_id) "
                       + "VALUES (96, 'Curso Fantasma Cerrado', 10, '2026-05-01', '2026-05-15', '10', '2025-01-01', '2026-12-31', 'Online', 100.0, 'CLOSED', 1)");
        
        InscriptionsModel model = new InscriptionsModel();
        
        // Preparamos al usuario "Atacante"
        ProfessionalDTO hacker = new ProfessionalDTO();
        hacker.setName("Mr");
        hacker.setSurname("Robot");
        hacker.setPhone("666000666");
        hacker.setEmail("hacker@fsociety.com");

        System.out.println("Escenario preparado. Intentando vulnerar el sistema: Matriculando en curso CLOSED (ID 96)...");

        // --- 2. EJECUCIÓN Y COMPROBACIÓN (TODO EN UNO) ---
        // assertThrows comprueba que el código de dentro LANCE OBLIGATORIAMENTE una ApplicationException.
        // Si el código NO lanza la excepción y matricula al usuario, el test FALLA (franja roja).
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            
            model.enrollProfessional(hacker, 96); // Intento de Bypass
            
        }, "❌ VULNERABILIDAD CRÍTICA: El modelo permitió inscribirse en un curso CLOSED. ¡Falta validación de estado en el Modelo!");
        
        // Si llega aquí, es que la excepción saltó y bloqueó al atacante.
        System.out.println("✅ TEST SUPERADO: El modelo se defendió por sí solo y bloqueó el ataque. Mensaje: " + exception.getMessage());
    }
}