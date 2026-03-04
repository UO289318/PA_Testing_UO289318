package g54.si26.closeFormativeAction.tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.Calendar;

import g54.si26.DTOs.CloseValidationDTO;
import g54.si26.closeFormativeActions.ModelCloseFormativeAction;

public class ModelCloseFormativeActionTest {

    private ModelCloseFormativeAction model;
    
    @BeforeEach
    public void setUp() {
        // Inicializamos el modelo antes de cada test
        model = new ModelCloseFormativeAction();
    }

    @Test
    public void testValidClosure_PerfectCourse() {
        // Arrange: Curso 7 (Perfect Course) y simulamos el año 2030
        int perfectCourseId = 7;
        Date futureDate = getCustomDate(2030, Calendar.JANUARY, 1);
        
        // Act
        CloseValidationDTO validation = model.validateClosure(perfectCourseId, futureDate);
        
        // Assert: JUnit 5 pone el mensaje de error al final
        assertTrue(validation.isCanClose(), "El curso debería poder cerrarse");
        assertEquals(0, validation.getErrors().size(), "No debería tener errores");
        assertEquals(0, validation.getWarnings().size(), "No debería tener warnings");
    }

    @Test
    public void testInvalidClosure_BeforeEndDate() {
        // Arrange: Curso 1, fecha 2020 (antes de que acabe)
        int activeCourseId = 1; 
        Date pastDate = getCustomDate(2020, Calendar.JANUARY, 1);
        
        // Act
        CloseValidationDTO validation = model.validateClosure(activeCourseId, pastDate);
        
        // Assert
        assertFalse(validation.isCanClose(), "El curso NO debería poder cerrarse antes de tiempo");
        assertEquals(1, validation.getErrors().size(), "Debería tener 1 error bloqueante");
        assertTrue(validation.getErrors().get(0).contains("before its end date"), "El mensaje debe hablar de la fecha");
    }

    @Test
    public void testWarningClosure_UnhandledRegistrations() {
        // Arrange: El curso 8 tiene un alumno en RECEIVED
        int warningCourseId = 8;
        Date futureDate = getCustomDate(2030, Calendar.JANUARY, 1);
        
        // Act
        CloseValidationDTO validation = model.validateClosure(warningCourseId, futureDate);
        
        // Assert
        assertTrue(validation.isCanClose(), "Debería dejar cerrar, pero avisando");
        assertTrue(validation.getWarnings().size() > 0, "Debería tener al menos 1 warning");
        
        boolean hasRegistrationWarning = validation.getWarnings().stream()
                .anyMatch(w -> w.contains("unhandled professional registrations"));
        assertTrue(hasRegistrationWarning, "Debe incluir el warning de alumnos pendientes");
    }
    
    @Test
    public void testWarningClosure_PendingInvoice() {
        // Arrange: El curso 9 tiene la factura en PENDING
        int blockedCourseId = 9;
        Date futureDate = getCustomDate(2030, Calendar.JANUARY, 1);
        
        // Act
        CloseValidationDTO validation = model.validateClosure(blockedCourseId, futureDate);
        
        // Assert
        assertTrue(validation.isCanClose(), "Debe dejar cerrar a pesar de la factura pendiente");
        
        boolean hasInvoiceWarning = validation.getWarnings().stream()
                .anyMatch(w -> w.contains("NOT PAID yet"));
        assertTrue(hasInvoiceWarning, "Debe incluir el warning de factura no pagada");
    }

    // --- Método de utilidad para crear fechas de prueba ---
    private Date getCustomDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return cal.getTime();
    }
}