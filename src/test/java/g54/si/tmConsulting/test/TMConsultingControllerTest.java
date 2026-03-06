package g54.si.tmConsulting.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import g54.si26.tmConsulting.TMConsultingController;
import g54.si26.tmConsulting.TMConsultingModel;
import g54.si26.tmConsulting.TMConsultingView;
import g54.si26.utils.Database;

import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;

public class TMConsultingControllerTest {

    private TMConsultingModel model;
    private TMConsultingView view;
    private TMConsultingController controller;

    @BeforeEach
    public void setUp() {
        // 1. Inicializamos la arquitectura
        model = new TMConsultingModel();
        view = new TMConsultingView();
        controller = new TMConsultingController(model, view);

        
        Database db = new Database();
        db.createDatabase(true); 
        
        db.executeUpdate("DELETE FROM MoneyMovement");
        db.executeUpdate("DELETE FROM Invoice");
        db.executeUpdate("DELETE FROM Payment");
        db.executeUpdate("DELETE FROM Inscription");
        db.executeUpdate("DELETE FROM Teacher_FormativeAction");
        db.executeUpdate("DELETE FROM FormativeAction");
        db.executeUpdate("DELETE FROM Professional");
        db.executeUpdate("DELETE FROM Teacher");
        
        db.loadDatabase(); // Cargamos los datos del .sql para que el test tenga qué leer
    }

    @Test
    public void testInitView_SetsCorrectDefaultDates() {
        // Arrange: Simulamos que el sistema está en el año 2026
        controller.setSimulatedDate("2026-05-15");
        
        // Act
        controller.initView();

        // Assert: El controlador debería haber puesto el 1 de enero y 31 de diciembre de 2026
        assertEquals("2026-01-01", view.getTxtFechaInicio().getText());
        assertEquals("2026-12-31", view.getTxtFechaFin().getText());
    }

    @Test
    public void testInitView_FallbackDateWhenSimulatedDateIsNull() {
        // Arrange: Sin fecha simulada
        controller.setSimulatedDate(null);
        
        // Act
        controller.initView();
        
        // Assert: Debería usar el año actual real
        String currentYear = String.valueOf(LocalDate.now().getYear());
        assertEquals(currentYear + "-01-01", view.getTxtFechaInicio().getText());
    }

    @Test
    public void testConsultButton_LoadsDataIntoTable() {
        // Arrange
        controller.setSimulatedDate("2026-01-01");
        controller.initController(); // Carga por defecto según el año simulado
        
        // Forzamos un rango manual en la vista que incluya los datos de tu .sql
        view.getTxtFechaInicio().setText("2020-01-01");
        view.getTxtFechaFin().setText("2030-12-31");
        view.getCbEstado().setSelectedItem("All");

        // Act: SIMULAMOS UN CLIC EN EL BOTÓN "Consultar"
        view.getBtnConsultar().doClick();

        // Assert
        DefaultTableModel tableModel = view.getModeloTabla();
        assertNotNull(tableModel, "El modelo de la tabla no debería ser nulo");
        
        // Ahora que cargamos la BD en el setUp, debería haber filas
        assertTrue(tableModel.getRowCount() > 0, "La tabla debería mostrar los cursos cargados desde la base de datos");
        
        // Verificamos que los labels de totales se hayan actualizado (que no contengan el valor inicial vacío)
        assertFalse(view.getLblTotalIngresos().getText().contains("€0.00"), "El total de ingresos debería estar actualizado");
        assertNotNull(view.getLblBalance().getText(), "El balance general debería estar visible");
    }
}