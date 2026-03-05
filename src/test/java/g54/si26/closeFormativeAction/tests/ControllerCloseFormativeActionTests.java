package g54.si26.closeFormativeAction.tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import g54.si26.closeFormativeActions.ControllerCloseFormativeAction;
import g54.si26.closeFormativeActions.ModelCloseFormativeAction;
import g54.si26.closeFormativeActions.ViewCloseFormativeAction;

import javax.swing.table.TableModel;

public class ControllerCloseFormativeActionTests {

    private ViewCloseFormativeAction view;
    private ModelCloseFormativeAction model;

    @BeforeEach
    public void setUp() {
        view = new ViewCloseFormativeAction();
        model = new ModelCloseFormativeAction();
        
        g54.si26.utils.Database db = new g54.si26.utils.Database();
        db.createDatabase(true); 
        
        // 🧹 LIMPIEZA MANUAL: Vaciamos las tablas por si el schema.sql no las borró bien
        // El orden es importante para no romper las claves foráneas (borramos hijos y luego padres)
        db.executeUpdate("DELETE FROM MoneyMovement");
        db.executeUpdate("DELETE FROM Invoice");
        db.executeUpdate("DELETE FROM Payment");
        db.executeUpdate("DELETE FROM Inscription");
        db.executeUpdate("DELETE FROM Teacher_FormativeAction");
        db.executeUpdate("DELETE FROM FormativeAction");
        db.executeUpdate("DELETE FROM Professional");
        db.executeUpdate("DELETE FROM Teacher");
        
        // Ahora que la BD está vacía 100% segura, cargamos tus datos
        db.loadDatabase();       
    }

    @Test
    public void testLoadCourses_PopulatesTableCorrectly() {
        // Arrange
        ControllerCloseFormativeAction controller = new ControllerCloseFormativeAction(view, model, "2026-03-04");
        
        // Act
        controller.initController();
        
        // Assert
        TableModel tableModel = view.getTabCourses().getModel();
        assertTrue(tableModel.getRowCount() > 0, "La tabla de cursos debería haber cargado datos de la BD");
        
        // SOLUCIÓN: Leemos el nombre de la cabecera visual, no del modelo interno
        String headerName = view.getTabCourses().getColumnModel().getColumn(1).getHeaderValue().toString();
        assertEquals("Course Name", headerName, "Las cabeceras deben haberse renombrado correctamente");
    }

    @Test
    public void testControllerInitialization_WithInvalidDate() {
        // Arrange: Inyectamos texto basura. 
        // No simularemos el clic en la tabla para evitar que el JOptionPane bloquee JUnit.
        ControllerCloseFormativeAction badController = new ControllerCloseFormativeAction(view, model, "FECHA INVALIDA");
        
        // Act
        assertDoesNotThrow(() -> {
            badController.initController();
        }, "El controlador debería inicializarse sin explotar aunque la fecha sea inválida");
        
        // Assert: Comprobamos que, por seguridad, la tabla de validación arranca vacía
        assertEquals(0, view.getTabValidation().getRowCount(), "La tabla de validación debería estar vacía al inicio");
    }
}