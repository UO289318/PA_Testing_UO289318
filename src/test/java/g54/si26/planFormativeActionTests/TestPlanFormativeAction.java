package g54.si26.planFormativeActionTests;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.utils.Database;
import g54.si26.planFormativeAction.*;


public class TestPlanFormativeAction {

    private static Database db = new Database();

    @BeforeEach
    public void setUp() {
        // Inicializamos la base de datos de forma limpia antes de cada test
        db.createDatabase(true);
        loadCleanDatabase(db);
    }

    /**
     * Limpia e inserta los datos mínimos necesarios para que las pruebas funcionen.
     */
    public static void loadCleanDatabase(Database db) {
        db.executeBatch(new String[] {
            "DELETE FROM MoneyMovement",
            "DELETE FROM Invoice",
            "DELETE FROM Teacher_FormativeAction",
            "DELETE FROM Fee",
            "DELETE FROM FormativeAction",
            "DELETE FROM Professional",
            "DELETE FROM Community",
            "DELETE FROM Teacher",
            
         
            "INSERT INTO Community (community_id, communityName) VALUES (3, 'General Public')",
            
         
            "INSERT INTO Teacher (teacher_id, name, fiscal_id, email, phone) VALUES (1, 'Claudio', '11111111A', 'claudio@test.com', '600111111')",
            "INSERT INTO Teacher (teacher_id, name, fiscal_id, email, phone) VALUES (2, 'Raquel', '22222222B', 'raquel@test.com', '600222222')"
        });
    }

    //Logic Tests
    @Test
    public void testEnrolmentLeadTimeRule() {
        ModelPlanFormativeAction model = new ModelPlanFormativeAction();
        
        // 3 weeks exactly (Should be true)
        assertTrue(model.enrolmentMeetsLeadTimeRule("2026-03-30", "2026-04-20"));
        
        // More than 3 weeks (Should be true)
        assertTrue(model.enrolmentMeetsLeadTimeRule("2026-03-01", "2026-04-20"));
        
        // Less than 3 weeks (false)
        assertFalse(model.enrolmentMeetsLeadTimeRule("2026-04-10", "2026-04-20"));
    }

    
    //Logic in validate()
    @Test
    public void testValidateFormativeActionErrors() {
        ModelPlanFormativeAction model = new ModelPlanFormativeAction();
        FormativeActionDTO dto = new FormativeActionDTO();
        
        //Empty fields
        dto.setName(""); 
        dto.setObjectives("Aprender testing");
        dto.setMainContents("JUnit 5");
        dto.setLocation("Online");
        dto.setSpots(-5); 
        dto.setFee(-50);  
        dto.setNumberOfHours(0); 
        
        //Logic for dates
        dto.setStartDate("2026-05-15");
        dto.setEndDate("2026-05-10"); // Error
        dto.setInscriptionPeriodStart("2026-04-01");
        dto.setInscriptionPeriodEnd("2026-04-30");

        ModelPlanFormativeAction.ValidationResult result = model.validate(dto, "2026-01-01");

        assertAll("Comprobación de acumulación de errores",
            () -> assertTrue(result.hasErrors(), "El DTO debería tener errores"),
            () -> assertTrue(result.errors.contains("Course name is required.")),
            () -> assertTrue(result.errors.contains("Number of spots must be greater than zero.")),
            () -> assertTrue(result.errors.contains("Fee must be zero or a positive value.")),
            () -> assertTrue(result.errors.contains("Duration must be at least 1 hour.")),
            () -> assertTrue(result.errors.contains("The end date cannot be before the start date."))
        );
    }

    
    //Check if insertions are correctly managed.
    @Test
    public void testAddFormativeActionUpdatesDB() {
        ModelPlanFormativeAction model = new ModelPlanFormativeAction();
        
        FormativeActionDTO dto = new FormativeActionDTO();
        dto.setName("JUnit Advanced Course");
        dto.setObjectives("Mastering JUnit 5");
        dto.setMainContents("Assertions, Exceptions, Mocks");
        dto.setSpots(20);
        dto.setStartDate("2026-10-01");
        dto.setEndDate("2026-10-05");
        dto.setNumberOfHours(15);
        dto.setInscriptionPeriodStart("2026-09-01");
        dto.setInscriptionPeriodEnd("2026-09-15");
        dto.setLocation("Online");
        dto.setFee(150.0);

        DefaultTableModel teacherModel = new DefaultTableModel(new String[]{"ID", "Name", "Remuneration (€)"}, 0);
        teacherModel.addRow(new Object[]{1, "Claudio", 500.0});


        model.addFormativeAction(dto, "2026-01-01", teacherModel);

        //COMPROBACIONES EN BASE DE DATOS
        
        // Formative Action Table
        List<Object[]> actions = db.executeQueryArray("SELECT name, spots, status FROM FormativeAction WHERE name = 'JUnit Advanced Course'");
        assertEquals(1, actions.size(), "Debería haberse insertado 1 curso");
        assertEquals("20", actions.get(0)[1].toString(), "Las plazas deben ser 20");
        assertEquals("ACTIVE", actions.get(0)[2].toString(), "El curso debe estar ACTIVE");

        // Fee Table
        List<Object[]> fees = db.executeQueryArray("SELECT amount, community_id FROM Fee");
        assertEquals(1, fees.size(), "Debería haber 1 tarifa generada");
        assertEquals("150.0", fees.get(0)[0].toString());
        assertEquals("3", fees.get(0)[1].toString());

        //Teacger_FormativeAction table
        List<Object[]> teacherFA = db.executeQueryArray("SELECT teacher_id, remuneration, status FROM Teacher_FormativeAction");
        assertEquals(1, teacherFA.size(), "Debería haber 1 profesor asignado");
        assertEquals("1", teacherFA.get(0)[0].toString(), "El ID del profe debe ser 1 (Claudio)");
        assertEquals("500.0", teacherFA.get(0)[1].toString(), "La remuneración debe ser 500");
        assertEquals("PENDING", teacherFA.get(0)[2].toString(), "El estado debe ser PENDING");

        // Invoices are created?
        List<Object[]> invoices = db.executeQueryArray("SELECT totalAmount, status, invoice_date FROM Invoice");
        assertEquals(1, invoices.size(), "Debería haberse generado 1 factura automáticamente");
        assertEquals("500.0", invoices.get(0)[0].toString(), "El total de la factura debe ser igual a la remuneración");
        assertEquals("PENDING", invoices.get(0)[1].toString(), "La factura debe estar PENDING de pago");
        assertEquals("2026-10-05", invoices.get(0)[2].toString(), "La fecha de la factura debe ser el final del curso (endDate)");
    }
}