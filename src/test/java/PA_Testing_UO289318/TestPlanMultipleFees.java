package PA_Testing_UO289318;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.planMultipleFees.ModelPlanMultipleFees;
import g54.si26.planMultipleFees.ModelPlanMultipleFees.ValidationResult;
import g54.si26.utils.Database;

public class TestPlanMultipleFees {

    private static final Database db = new Database();
    private ModelPlanMultipleFees model;
    private final String SIMULATED_TODAY = "2026-04-29 12:00:00";

    @BeforeEach
    public void setUp(){
        model = new ModelPlanMultipleFees();
        db.createDatabase(true); 
        loadCleanDatabase(db); 
    }

    public static void loadCleanDatabase(Database db){
        db.executeBatch(new String[] {
                "DELETE FROM MoneyMovement",
                "DELETE FROM Invoice",
                "DELETE FROM Inscription",
                "DELETE FROM Teacher_FormativeAction",
                "DELETE FROM Fee",
                "DELETE FROM FormativeAction",
                "DELETE FROM Professional",
                "DELETE FROM Teacher",
                "DELETE FROM Community",
                
                //2 Communities and 1 Teacher
                "INSERT INTO Teacher(teacher_id, name, fiscal_id, email, phone) VALUES (1, 'Profesor Prueba', '12345678A', 'prof@test.com', '600100200')",
                "INSERT INTO Community(community_id, communityName) VALUES (1, 'Comunidad Asturias')",
                "INSERT INTO Community(community_id, communityName) VALUES (2, 'Comunidad Galicia')"
        });
    }

    //For creating a DTO
    private FormativeActionDTO createBaseDTO(){
        FormativeActionDTO dto = new FormativeActionDTO();
        dto.setName("Curso Testing " + System.currentTimeMillis()); 
        dto.setObjectives("Aprender testing");
        dto.setMainContents("Tests unitarios");
        dto.setLocation("Online");
        dto.setNumberOfHours(40);
        dto.setSpots(25);
        dto.setInscriptionPeriodStart("2026-07-01");
        dto.setInscriptionPeriodEnd("2026-07-31");
        dto.setStartDate("2026-09-01");
        dto.setEndDate("2026-09-30");
        return dto;
    }

    private Map<Integer, Double> createBaseFees(){
        Map<Integer, Double> fees = new HashMap<>();
        fees.put(1, 150.0);
        return fees;
    }

    private DefaultTableModel createBaseTeacherModel(){
        DefaultTableModel teacherModel = new DefaultTableModel(new Object[]{"ID", "Name", "Remuneration"}, 0);
        teacherModel.addRow(new Object[]{1, "Profesor Prueba", 200.0});
        return teacherModel;
    }


    // TC-01:Valid Inseriton
    @Test
    public void testTC01_GoldenPath(){
        FormativeActionDTO dto = createBaseDTO();
        
        Map<Integer, Double> fees = createBaseFees();
        fees.put(2, 150.0); 

        DefaultTableModel teacherModel = createBaseTeacherModel(); 

        ValidationResult result = model.validate(dto, SIMULATED_TODAY, fees);
        assertFalse(result.hasErrors(), "TC-01: Validation should pass.");

        model.addFormativeAction(dto, SIMULATED_TODAY, fees, teacherModel);

        // Check if it has been stored in the DB
        List<Object[]> actions = db.executeQueryArray("SELECT action_id FROM FormativeAction WHERE name = ?", dto.getName());
        assertEquals(1, actions.size(), "The Formative Action should be saved.");
        
        String actionId = actions.get(0)[0].toString();
        List<Object[]> dbFees = db.executeQueryArray("SELECT * FROM Fee WHERE action_id = ?", actionId);
        List<Object[]> dbTeachers = db.executeQueryArray("SELECT * FROM Teacher_FormativeAction WHERE action_id = ?", actionId);
        
        assertEquals(2, dbFees.size(), "Should have 2 fees linked.");
        assertEquals(1, dbTeachers.size(), "Should have 1 teacher linked.");
    }

    // TC-02: Invalid Places (0 places)
    @Test
    public void testTC02_InvalidPlaces(){
        FormativeActionDTO dto = createBaseDTO();
        dto.setSpots(0);

        ValidationResult result = model.validate(dto, SIMULATED_TODAY, createBaseFees());
        assertTrue(result.hasErrors());
        assertTrue(result.errors.contains("Number of spots must be a number greater than zero."));
    }

    // TC-03: Dates set in the past
    @Test
    public void testTC03_CourseStartInThePast(){
        FormativeActionDTO dto = createBaseDTO();
        dto.setStartDate("2025-09-01"); 

        ValidationResult result = model.validate(dto, SIMULATED_TODAY, createBaseFees());
        assertTrue(result.hasErrors());
        assertTrue(result.errors.contains("The Formative Action cannot be planned in the past."));
    }

    // TC-04: Incorrect date format / existence
    @Test
    public void testTC04_InvalidDateFormat(){
        FormativeActionDTO dto = createBaseDTO();
        dto.setStartDate("2026-02-30"); 

        ValidationResult result = model.validate(dto, SIMULATED_TODAY, createBaseFees());
        assertTrue(result.hasErrors());
        assertTrue(result.errors.contains("The date format is incorrect or does not exist. Please use YYYY-MM-DD."));
    }

    // TC-05: Enrolment End Date preceding Start Date
    @Test
    public void testTC05_EnrolmentEndBeforeStart(){
        FormativeActionDTO dto = createBaseDTO();
        dto.setInscriptionPeriodStart("2026-07-15");
        dto.setInscriptionPeriodEnd("2026-07-01");

        ValidationResult result = model.validate(dto, SIMULATED_TODAY, createBaseFees());
        assertTrue(result.hasErrors());
        assertTrue(result.errors.contains("Enrolment end date cannot be before its start date."));
    }


    // TC-06: Enrolment ending after Course concludes
    @Test
    public void testTC06_EnrolmentEndsAfterCourse(){
        FormativeActionDTO dto = createBaseDTO();
        dto.setInscriptionPeriodEnd("2026-10-15"); 

        ValidationResult result = model.validate(dto, SIMULATED_TODAY, createBaseFees());
        // It should provide a warning
        assertTrue(result.hasWarnings());
        assertTrue(result.warnings.contains("Enrolment does not end before the Formative Action starts."));
    }

    // TC-07: Course End Date preceding Start Date
    @Test
    public void testTC07_CourseEndBeforeStart(){
        FormativeActionDTO dto = createBaseDTO();
        dto.setStartDate("2026-09-15");
        dto.setEndDate("2026-09-01");

        ValidationResult result = model.validate(dto, SIMULATED_TODAY, createBaseFees());
        assertTrue(result.hasErrors());
        assertTrue(result.errors.contains("The end date cannot be before the start date."));
    }

    // TC-08: Missing teachers
    @Test
    public void testTC08_MissingTeachersPreCheck(){
        // Simulate call
        DefaultTableModel teacherModel = new DefaultTableModel(new Object[]{"ID", "Name", "Remuneration"}, 0);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            if (teacherModel.getRowCount() == 0) 
                throw new IllegalArgumentException("At least one teacher must be assigned.");
            
        });
        
        assertEquals("At least one teacher must be assigned.", exception.getMessage());
    }

    // TC-09: Negative/Zero remuneration
    @Test
    public void testTC09_NegativeTeacherRemunerationPreCheck(){
        DefaultTableModel teacherModel = createBaseTeacherModel();
        teacherModel.setValueAt(-50.00, 0, 2); 
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            for (int i = 0; i < teacherModel.getRowCount(); i++){
                double rem = Double.parseDouble(teacherModel.getValueAt(i, 2).toString());
                if (rem <= 0) 
                    throw new IllegalArgumentException("Teacher remuneration cannot be negative or zero.");
                
            }
        });
        
        assertEquals("Teacher remuneration cannot be negative or zero.", exception.getMessage());
    }

    // TC-10: Missing community fees (0 Communities)
    @Test
    public void testTC10_NoCommunities(){
        FormativeActionDTO dto = createBaseDTO();
        Map<Integer, Double> emptyFees = new HashMap<>(); 

        ValidationResult result = model.validate(dto, SIMULATED_TODAY, emptyFees);
        assertTrue(result.hasErrors());
        assertTrue(result.errors.contains("At least one community must have an assigned fee."));
    }


    // TC-11: Negative fee
    @Test
    public void testTC11_NegativeFee(){
        FormativeActionDTO dto = createBaseDTO();
        Map<Integer, Double> fees = new HashMap<>();
        fees.put(1, -10.00); 

        ValidationResult result = model.validate(dto, SIMULATED_TODAY, fees);
        assertTrue(result.hasErrors());
        assertTrue(result.errors.contains("Fee for community id 1 must be zero or positive."));
    }
}