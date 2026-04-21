package g54.si26.consultFormativeActionsSecretary;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import g54.si26.DTOs.FormativeActionManagementDTO;
import g54.si26.DTOs.FormativeActionDetailsDTO;

public class ModelConsultFormativeActionsTest {

    private ModelConsultFormativeActions model;

    @BeforeEach
    public void setUp() {       
        model = new ModelConsultFormativeActions();
    }

    // =======================
    //COMBINACIONES VALIDAS
    // ========================

    @Test
    public void testValid_ActiveFilter_ShowsOnlyNotClosed() {
        // Situación Válida 1: Filtro por defecto (null) en una fecha estándar.
        // Debe devolver información de las FAs, pero NINGUNA debe estar CLOSED.
        String statusFilter = null; // Representa el "ACTIVE (Default)"
        String simulatedDate = "2026-05-15"; 

        List<FormativeActionManagementDTO> results = model.getFormativeActions(statusFilter, simulatedDate);

        assertNotNull(results, "La lista no debe ser nula");
        for (FormativeActionManagementDTO action : results) {
            assertNotNull(action.getName(), "El nombre de la FA no debe ser nulo");
            assertNotEquals("CLOSED", action.getStatus(), 
                "El filtro por defecto no debe devolver acciones CLOSED. Falló en: " + action.getName());
        }
    }

    @Test
    public void testValid_SpecificStatusFilter_Cancelled() {
        // Situación Válida 2: Buscar específicamente acciones Canceladas en una fecha.
        String statusFilter = "Cancelled"; 
        String simulatedDate = "2026-05-15"; 

        List<FormativeActionManagementDTO> results = model.getFormativeActions(statusFilter, simulatedDate);

        assertNotNull(results);
        for (FormativeActionManagementDTO action : results) {
            assertEquals("Cancelled", action.getStatus(), 
                "El sistema debería enseñar el estado correcto ('Cancelled') para el filtro aplicado.");
        }
    }

    @Test
    public void testValid_GetCorrectInformation_ActionDetails() {
        // Situación Válida 3: Obtener información detallada correcta para un ID válido.
        int validActionId = 1; // Asumimos que el curso con ID 1 existe.

        FormativeActionDetailsDTO details = model.getActionDetails(validActionId);

        assertNotNull(details, "Los detalles deben encontrarse para un ID válido");
        assertNotNull(details.getObjectives(), "El campo de objetivos debe contener información");
        assertTrue(details.getTotalRegisters() >= 0, "El total de registros no puede ser negativo");
    }

    @Test
    public void testValid_GetCorrectInformation_CourseFees() {
        // Situación Válida 4: Obtener tarifas correctamente.
        int validActionId = 1;

        List<Object[]> fees = model.getCourseFees(validActionId);

        assertNotNull(fees, "La lista de tarifas (fees) no debe ser nula");
        // Comprobamos que si hay tarifas, traen la estructura correcta (3 columnas)
        if (!fees.isEmpty()) {
            assertEquals(3, fees.get(0).length, "Debería traer 3 datos: ID, Comunidad y Precio");
        }
    }

    // ====================================
    // SITUACIONES INCORRECTAS / EDGE CASES 
    // ====================================

    @Test
    public void testIncorrect_DetailsForNonExistentAction() {
        // Situación Incorrecta 1: Se pide información detallada de una FA que no existe.
        // El sistema NO debe lanzar excepción, debe devolver null.
        int invalidActionId = -999; 

        FormativeActionDetailsDTO details = model.getActionDetails(invalidActionId);

        assertNull(details, "El sistema debe devolver null cuando se busca un ID que no existe");
    }

    @Test
    public void testIncorrect_FeesForNonExistentAction() {
        // Situación Incorrecta 2: Se piden tarifas de una FA que no existe.
        // El sistema debe devolver una lista vacía, no null ni romper.
        int invalidActionId = -999;

        List<Object[]> fees = model.getCourseFees(invalidActionId);

        assertNotNull(fees, "El sistema nunca debe devolver una lista nula para las fees");
        assertTrue(fees.isEmpty(), "La lista de tarifas debe estar vacía para un ID inexistente");
    }

    @Test
    public void testIncorrect_UnmatchedStatusFilter() {
        // Situación Incorrecta 3: Se envía un filtro de estado que no existe en la base de datos.
        String invalidStatusFilter = "ESTADO_INVENTADO";
        String simulatedDate = "2026-05-15";

        List<FormativeActionManagementDTO> results = model.getFormativeActions(invalidStatusFilter, simulatedDate);

        assertNotNull(results, "No debe devolver null");
        assertTrue(results.isEmpty(), "Debe devolver una lista vacía al no haber coincidencias de estado");
    }

    @Test
    public void testIncorrect_NullOrEmptyDateFilter() {
        // Situación Incorrecta 4: Se envía una fecha nula o vacía.
        // El sistema está programado (safeDate) para usar "9999-12-31" por defecto.
        String emptyDate = "";
        String statusFilter = "ALL"; // Traemos todo para comprobar que la query funciona

        // Ejecutamos con string vacío
        List<FormativeActionManagementDTO> resultsEmpty = model.getFormativeActions(statusFilter, emptyDate);
        assertNotNull(resultsEmpty, "El sistema debe manejar fechas vacías sin lanzar excepción");

        // Ejecutamos con fecha nula
        List<FormativeActionManagementDTO> resultsNull = model.getFormativeActions(statusFilter, null);
        assertNotNull(resultsNull, "El sistema debe manejar fechas nulas sin lanzar excepción");
    }
}