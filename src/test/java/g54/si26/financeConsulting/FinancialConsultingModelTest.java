package g54.si26.financeConsulting;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.utils.ApplicationException;

public class FinancialConsultingModelTest {

    private FinancialConsultingModel model;

    @BeforeEach
    public void setUp() {
        // Se ejecuta antes de cada test para tener un modelo limpio
        model = new FinancialConsultingModel();
    }

    @Test
    public void testGetFormativeActionsByStatus_All() {
        // Probamos que devuelva la lista completa sin fallar
        List<FormativeActionDTO> actions = model.getFormativeActionsByStatus("All", "2026-04-19");
        assertNotNull(actions, "La lista de acciones formativas no debería ser nula");
        // Asegúrate de tener al menos 1 curso en tu base de datos para que esto pase
        assertTrue(actions.size() > 0, "Debería haber al menos una acción formativa en la BD");
    }

    @Test
    public void testGetFormativeActionsByStatus_Active() {
        // Probamos el filtro de Activos
        List<FormativeActionDTO> actions = model.getFormativeActionsByStatus("Active", "2026-04-19");
        assertNotNull(actions);
        // Aquí no comprobamos tamaño porque podría no haber activos, 
        // pero verificamos que no lance excepción
    }

    @Test
    public void testGetCourseBasicData_ValidId() {
        // IMPORTANTE: Cambia el '1' por el ID de un curso que sepas que EXISTE en tu BD.db
        int validActionId = 1; 
        Object[] basicData = model.getCourseBasicData(validActionId, "2026-04-19");
        
        assertNotNull(basicData, "Los datos básicos no deben ser nulos");
        assertEquals(7, basicData.length, "Debe devolver exactamente 7 columnas (name, status, etc)");
        assertNotNull(basicData[0], "El nombre del curso no debe ser nulo");
    }

    @Test
    public void testGetCourseBasicData_InvalidId() {
        // Probamos un ID que NO existe (ej. -999) para ver si lanza nuestra ApplicationException
        assertThrows(ApplicationException.class, () -> {
            model.getCourseBasicData(-999, "2026-04-19");
        });
    }

    @Test
    public void testGetMovements() {
        // IMPORTANTE: Cambia el '1' por un ID válido
        int validActionId = 1;
        List<Object[]> movements = model.getMovements(validActionId);
        
        assertNotNull(movements, "La lista de movimientos no debe ser nula");
        if (!movements.isEmpty()) {
            Object[] firstMovement = movements.get(0);
            assertEquals(4, firstMovement.length, "Cada fila de movimiento debe tener 4 columnas (fecha, concepto, cantidad, es_ingreso)");
        }
    }
}
