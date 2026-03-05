package g54.si26.inscriptions.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.utils.Database;
import g54.si26.utils.Util;
import g54.si26.inscriptions.*;

public class BoundaryDateTest {

    @Test
    public void testInscriptionPeriodBoundaries() {
        // --- 1. PREPARACIÓN DEL ESCENARIO ---
        Database db = new Database();
        
        // Limpiamos la base de datos para nuestro curso de prueba (ID 97)
        db.executeUpdate("DELETE FROM Inscription WHERE action_id = 97");
        db.executeUpdate("DELETE FROM FormativeAction WHERE action_id = 97");
        
        // Insertamos un curso cuyo plazo de inscripción termina exactamente el 26 de febrero de 2026.
        // Fíjate en inscriptionPeriodEnd = '2026-02-26'
        db.executeUpdate("INSERT INTO FormativeAction (action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, fee, status, teacher_id) "
                       + "VALUES (97, 'Curso al Límite del Tiempo', 10, '2026-05-01', '2026-05-15', '10', '2026-02-01', '2026-02-26', 'Online', 100.0, 'ACTIVE', 1)");
        
        System.out.println("Escenario preparado: Curso ID 97 creado. Fecha límite de inscripción: 2026-02-26");
        
        InscriptionsModel model = new InscriptionsModel();

        // --- 2. PRIMERA COMPROBACIÓN: EL ÚLTIMO DÍA (Dentro del límite) ---
        // Buscamos cursos simulando que hoy es el mismo día 26 de febrero.
        Date boundaryDate = Util.isoStringToDate("2026-02-26");
        System.out.println("\nBuscando cursos disponibles el día 2026-02-26...");
        
        List<FormativeActionDTO> availableCourses = model.getAvailableCourses(boundaryDate);
        
        // Comprobamos si nuestro curso 97 está en la lista que devuelve el modelo
        boolean isCourseAvailable = isCourseInList(availableCourses, 97);
        
        System.out.println("¿El curso 97 sale en la lista? " + isCourseAvailable);
        assertTrue(isCourseAvailable, "❌ FALLO: El curso NO está disponible el último día de plazo. Hay un error con el operador <= en la SQL.");


        // --- 3. SEGUNDA COMPROBACIÓN: UN DÍA DESPUÉS (Fuera del límite) ---
        // Avanzamos solo un día: 27 de febrero. ¡El curso ya debería desaparecer!
        Date pastBoundaryDate = Util.isoStringToDate("2026-02-27");
        System.out.println("\nBuscando cursos disponibles el día 2026-02-27...");
        
        List<FormativeActionDTO> availableCoursesAfter = model.getAvailableCourses(pastBoundaryDate);
        
        // Comprobamos si el curso 97 milagrosamente sigue ahí
        boolean isCourseAvailableAfter = isCourseInList(availableCoursesAfter, 97);
        
        System.out.println("¿El curso 97 sale en la lista? " + isCourseAvailableAfter);
        assertFalse(isCourseAvailableAfter, "❌ FALLO: El curso SIGUE DISPONIBLE un día después de haber cerrado el plazo.");
        
        System.out.println("\n✅ TEST SUPERADO: El modelo respeta escrupulosamente los límites del periodo de inscripción.");
    }

    /**
     * Método auxiliar para buscar un ID concreto dentro de la lista de DTOs devuelta.
     */
    private boolean isCourseInList(List<FormativeActionDTO> courses, int targetId) {
        for (FormativeActionDTO course : courses) {
            if (course.getActionId() == targetId) {
                return true;
            }
        }
        return false;
    }
}