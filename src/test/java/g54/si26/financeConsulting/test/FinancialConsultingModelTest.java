package g54.si26.financeConsulting.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.financeConsulting.FinancialConsultingModel;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;

public class FinancialConsultingModelTest {

    private FinancialConsultingModel model;
    private Database db;

    @BeforeEach
    public void setUp() {
        model = new FinancialConsultingModel();
        db = new Database();
        
        // --- LIMPIEZA Y CARGA (Tu estándar de seguridad) ---
        db.createDatabase(true); 
        db.executeUpdate("DELETE FROM MoneyMovement");
        db.executeUpdate("DELETE FROM Invoice");
        db.executeUpdate("DELETE FROM Payment");
        db.executeUpdate("DELETE FROM Inscription");
        db.executeUpdate("DELETE FROM Teacher_FormativeAction");
        db.executeUpdate("DELETE FROM FormativeAction");
        db.executeUpdate("DELETE FROM Professional");
        db.executeUpdate("DELETE FROM Teacher");
        
        db.loadDatabase(); // Carga los datos del script SQL
    }

    @Test
    public void testGetFormativeActionsByStatus_All() {
        List<FormativeActionDTO> actions = model.getFormativeActionsByStatus("All", "2026-04-19");
        assertNotNull(actions);
        // CAMBIO: Ahora solo tienes 2 cursos en el SQL
        assertEquals(2, actions.size(), "Deberían devolverse las 2 acciones formativas actuales");
    }

    @Test
    public void testGetFormativeActionsByStatus_Active() {
        List<FormativeActionDTO> actions = model.getFormativeActionsByStatus("Active", "2026-04-19");
        
        assertNotNull(actions);
        for (FormativeActionDTO dto : actions) {
            assertEquals("ACTIVE", dto.getStatus().toUpperCase(), "Solo deben aparecer cursos ACTIVE");
        }
    }

    @Test
    public void testGetFormativeActionsByStatus_NotActive() {
        // Probamos el filtro "Not Active"
        List<FormativeActionDTO> actions = model.getFormativeActionsByStatus("Not Active", "2026-04-19");
        
        assertNotNull(actions);
        for (FormativeActionDTO dto : actions) {
            assertNotEquals("ACTIVE", dto.getStatus().toUpperCase(), "No debe aparecer ningún curso ACTIVE");
        }
    }
    
    @Test
    public void testGetFormativeActions_NotActiveFilter() {
        // Act
        List<FormativeActionDTO> actions = model.getFormativeActionsByStatus("Not Active", "2026-04-19");

        // Assert
        assertNotNull(actions);
        for (FormativeActionDTO dto : actions) {
            assertNotEquals("ACTIVE", dto.getStatus().toUpperCase(), 
                "El filtro 'Not Active' no debe incluir cursos con estado ACTIVE");
        }
    }

    @Test
    public void testFreeSpotsCalculation_Dynamic() {
        int actionId = 1;
        
        Object[] data = model.getCourseBasicData(actionId, "2026-04-19");
        int totalSpots = (int) data[5];
        int freeSpotsFromModel = (int) data[6];

        String sql = "SELECT COUNT(*) FROM Inscription WHERE action_id = ? AND state IN ('RECEIVED', 'CONFIRMED')";
        int actualInscriptions = db.executeQueryArray(sql, actionId).get(0)[0].toString().equals("") ? 0 : 
                                 Integer.parseInt(db.executeQueryArray(sql, actionId).get(0)[0].toString());

        assertEquals(totalSpots - actualInscriptions, freeSpotsFromModel, 
            "El modelo debe calcular las plazas libres restando las inscripciones actuales de las totales");
    }

    @Test
    public void testGetCourseBasicData_InvalidId() {
        // Debe lanzar ApplicationException si el ID no existe
        assertThrows(ApplicationException.class, () -> {
            model.getCourseBasicData(-1, "2026-04-19");
        });
    }

    @Test
    public void testGetMovements_IncomesAndExpenses() {
        // CAMBIO: Usamos el actionId = 1 porque en tus nuevos datos es el que tiene gastos (MoneyMovement)
        int actionId = 1; 
        List<Object[]> movements = model.getMovements(actionId);
        
        assertNotNull(movements, "La lista de movimientos no debe ser nula");
        
        boolean foundIncome = false;
        boolean foundExpense = false;
        
        for (Object[] m : movements) {
            int isIncome = Integer.parseInt(m[3].toString());
            if (isIncome == 1) foundIncome = true;
            if (isIncome == 0) foundExpense = true;
        }
        
        assertTrue(foundIncome, "Debe haber al menos un ingreso (is_income = 1) para el curso 1");
        assertTrue(foundExpense, "Debe haber al menos un gasto (is_income = 0) para el curso 1");
    }

    @Test
    public void testGetMovements_EmptyCourse() {
        // El curso 9 (Blocked Course) en tu SQL no tiene movimientos de dinero asociados
        List<Object[]> movements = model.getMovements(9);
        
        assertNotNull(movements);
        assertEquals(0, movements.size(), "El curso 9 no debería tener movimientos de dinero");
    }
}