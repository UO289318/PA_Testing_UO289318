package g54.si26.MultipleFeesTests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.utils.Database;
import g54.si26.planMultipleFees.*;


public class PlanMultipleFees {

    private static Database db = new Database();

    @BeforeEach
    public void setUp() {
        // Inicializamos la base de datos limpia antes de cada test
        db.createDatabase(true);
        loadCleanDatabase(db);
    }

    /**
     * Limpia e inserta los datos mínimos (Comunidades y Profesores) para los tests.
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
            
            // Insertamos 3 comunidades de prueba
            "INSERT INTO Community (community_id, communityName) VALUES (1, 'College Members')",
            "INSERT INTO Community (community_id, communityName) VALUES (2, 'Uniovi Students')",
            "INSERT INTO Community (community_id, communityName) VALUES (3, 'General Public')",
            
            // Insertamos 2 profesores de prueba
            "INSERT INTO Teacher (teacher_id, name, fiscal_id, email, phone) VALUES (1, 'Claudio', '11111111A', 'claudio@test.com', '600111111')",
            "INSERT INTO Teacher (teacher_id, name, fiscal_id, email, phone) VALUES (2, 'Raquel', '22222222B', 'raquel@test.com', '600222222')"
        });
    }

    //Logic Tests
    @Test
    public void testEnrolmentLeadTimeRule() {
        ModelPlanMultipleFees model = new ModelPlanMultipleFees();
        
        // 3 weeks exactly (Should be true)
        assertTrue(model.enrolmentMeetsLeadTimeRule("2026-03-30", "2026-04-20"));
        
        // Less than 3 weeks (false)
        assertFalse(model.enrolmentMeetsLeadTimeRule("2026-04-10", "2026-04-20"));
    }

    //Logic in validate(), this time includes multiple fees logic
    @Test
    public void testValidateFormativeActionErrors() {
        ModelPlanMultipleFees model = new ModelPlanMultipleFees();
        FormativeActionDTO dto = new FormativeActionDTO();
        
        // Empty fields
        dto.setName(""); 
        dto.setObjectives("Learn Neo4J");
        dto.setMainContents("Graph DBs");
        dto.setLocation("Aulario Sur");
        dto.setSpots(0); 
        dto.setNumberOfHours(0); 
        
        //Logic for dates
        dto.setStartDate("2026-05-15");
        dto.setEndDate("2026-05-10"); 
        dto.setInscriptionPeriodStart("2026-04-01");
        dto.setInscriptionPeriodEnd("2026-04-30");

        // Logic for Fees (Wrong fee)
        Map<Integer, Double> emptyFees = new HashMap<>();
        Map<Integer, Double> negativeFees = new HashMap<>();
        negativeFees.put(1, -50.0);

        ModelPlanMultipleFees.ValidationResult resultEmptyFees = model.validate(dto, "2026-01-01", emptyFees);
        ModelPlanMultipleFees.ValidationResult resultNegativeFees = model.validate(dto, "2026-01-01", negativeFees);

        assertAll("Comprobación de acumulación de errores de validación",
            () -> assertTrue(resultEmptyFees.hasErrors(), "El DTO debería tener errores"),
            () -> assertTrue(resultEmptyFees.errors.contains("Course name is required.")),
            () -> assertTrue(resultEmptyFees.errors.contains("Number of spots must be greater than zero.")),
            () -> assertTrue(resultEmptyFees.errors.contains("Duration must be at least 1 hour.")),
            () -> assertTrue(resultEmptyFees.errors.contains("The end date cannot be before the start date.")),
            () -> assertTrue(resultEmptyFees.errors.contains("At least one community must have an assigned fee.")), // Error específico de Map vacío
            () -> assertTrue(resultNegativeFees.errors.contains("Fee for community id 1 must be zero or positive.")) // Error específico de tarifa negativa
        );
    }

    //Check if insertions are correctly managed
    @Test
    public void testAddFormativeActionMultipleFeesUpdatesDB() {
        ModelPlanMultipleFees model = new ModelPlanMultipleFees();
        
        FormativeActionDTO dto = new FormativeActionDTO();
        dto.setName("Neo4J Database Administration");
        dto.setObjectives("Databases basics");
        dto.setMainContents("Neo4J, Cypher");
        dto.setSpots(20);
        dto.setStartDate("2026-04-20");
        dto.setEndDate("2026-04-24");
        dto.setNumberOfHours(24);
        dto.setInscriptionPeriodStart("2026-02-23");
        dto.setInscriptionPeriodEnd("2026-03-31");
        dto.setLocation("Aulario Sur");

        //Fees
        Map<Integer, Double> fees = new HashMap<>();
        fees.put(1, 150.0); // College Members a 150€
        fees.put(3, 300.0); // General Public a 300€

        //Teacher
        DefaultTableModel teacherModel = new DefaultTableModel(new String[]{"ID", "Name", "Remuneration (€)"}, 0);
        teacherModel.addRow(new Object[]{1, "Claudio", 100.0});
        teacherModel.addRow(new Object[]{2, "Raquel", 200.0});

        model.addFormativeAction(dto, "2026-01-01", fees, teacherModel);

        List<Object[]> actions = db.executeQueryArray("SELECT action_id, name, spots, status FROM FormativeAction WHERE name = 'Neo4J Database Administration'");
        assertEquals(1, actions.size(), "Debería haberse insertado 1 curso");
        String actionId = actions.get(0)[0].toString();
        assertEquals("20", actions.get(0)[2].toString());
        assertEquals("ACTIVE", actions.get(0)[3].toString());

        List<Object[]> savedFees = db.executeQueryArray("SELECT amount, community_id FROM Fee WHERE action_id = ? ORDER BY community_id", actionId);
        assertEquals(2, savedFees.size(), "Debería haber 2 tarifas generadas");
        assertEquals("150.0", savedFees.get(0)[0].toString(), "Tarifa 1: 150€");
        assertEquals("1", savedFees.get(0)[1].toString(), "Tarifa 1: Comunidad 1");
        assertEquals("300.0", savedFees.get(1)[0].toString(), "Tarifa 2: 300€");
        assertEquals("3", savedFees.get(1)[1].toString(), "Tarifa 2: Comunidad 3");

        List<Object[]> teacherFA = db.executeQueryArray("SELECT teacher_id, remuneration, status FROM Teacher_FormativeAction WHERE action_id = ? ORDER BY teacher_id", actionId);
        assertEquals(2, teacherFA.size(), "Debería haber 2 profesores asignados");
        assertEquals("1", teacherFA.get(0)[0].toString(), "Profe 1: ID 1");
        assertEquals("100.0", teacherFA.get(0)[1].toString(), "Profe 1: Remuneración 100€");
        assertEquals("2", teacherFA.get(1)[0].toString(), "Profe 2: ID 2");
        assertEquals("200.0", teacherFA.get(1)[1].toString(), "Profe 2: Remuneración 200€");

        List<Object[]> invoices = db.executeQueryArray("SELECT teacher_id, totalAmount, status, invoice_date FROM Invoice WHERE action_id = ? ORDER BY teacher_id", actionId);
        assertEquals(2, invoices.size(), "Debería haberse generado 2 facturas automáticamente");
        assertEquals("100.0", invoices.get(0)[1].toString(), "Factura 1: Total 100€");
        assertEquals("200.0", invoices.get(1)[1].toString(), "Factura 2: Total 200€");
        assertEquals("PENDING", invoices.get(0)[2].toString(), "Factura 1: Estado PENDING");
        assertEquals("2026-04-24", invoices.get(0)[3].toString(), "Facturas deben tener la fecha de fin de curso (endDate)");
    }
}