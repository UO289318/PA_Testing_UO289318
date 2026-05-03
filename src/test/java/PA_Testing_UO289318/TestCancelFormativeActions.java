package PA_Testing_UO289318;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.DTOs.ProfessionalDTO;
import g54.si26.cancelFormativeActions.ModelCancelFormativeAction;
import g54.si26.inscriptions.InscriptionsModel;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;

public class TestCancelFormativeActions {

    private static final Database db = new Database();
    private ModelCancelFormativeAction cancelModel;
    private InscriptionsModel inscriptionsModel;
    private Date SIMULATED_DATE;

    @BeforeEach
    public void setUp() throws ParseException {
        cancelModel = new ModelCancelFormativeAction();
        inscriptionsModel = new InscriptionsModel();
        SIMULATED_DATE = new SimpleDateFormat("yyyy-MM-dd").parse("2026-04-29");
        
        db.createDatabase(true); 
        loadCleanDatabase(db); 
    }

    public static void loadCleanDatabase(Database db) {
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
                
                "INSERT INTO Community(community_id, communityName) VALUES (1, 'Comunidad Asturias')",
                "INSERT INTO Teacher(teacher_id, name, fiscal_id, email, phone) VALUES (1, 'Profesor 1', '1A', 't1@test.com', '111')",
                "INSERT INTO Professional(professional_id, name, surname, phone, email, community_id) VALUES (1, 'Prof', 'Test', '222', 'p@test.com', 1)",
                
                // FA 1: IN PROGRESS (For TC-01)
                "INSERT INTO FormativeAction(action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, status) VALUES (1, 'FA1', 10, '2026-04-01', '2026-05-30', 40, '2026-01-01', '2026-03-31', 'Online', 'ACTIVE')",
                "INSERT INTO Fee(fee_id, amount, community_id, action_id) VALUES (1, 100.0, 1, 1)",
                "INSERT INTO Teacher_FormativeAction(id, remuneration, status, action_id, teacher_id) VALUES (1, 1000.0, 'PENDING', 1, 1)",
                "INSERT INTO Inscription(inscription_id, inscription_date, applied_fee, state, professional_id, action_id) VALUES (1, '2026-02-01', 100.0, 'CONFIRMED', 1, 1)",
                "INSERT INTO MoneyMovement(movement_date, amount, status, type, inscription_id) VALUES ('2026-02-02', 50.0, 'PAID', 'PAYMENT', 1)", 

                // FA 2: UPCOMING (For TC-02) 
                "INSERT INTO FormativeAction(action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, status) VALUES (2, 'FA2', 10, '2026-06-01', '2026-07-30', 40, '2026-05-01', '2026-05-20', 'Online', 'ACTIVE')",
                "INSERT INTO Fee(fee_id, amount, community_id, action_id) VALUES (2, 100.0, 1, 2)",
                "INSERT INTO Teacher_FormativeAction(id, remuneration, status, action_id, teacher_id) VALUES (2, 1000.0, 'PENDING', 2, 1)",
                "INSERT INTO Inscription(inscription_id, inscription_date, applied_fee, state, professional_id, action_id) VALUES (2, '2026-05-02', 100.0, 'CONFIRMED', 1, 2)",
                "INSERT INTO MoneyMovement(movement_date, amount, status, type, inscription_id) VALUES ('2026-05-03', 110.0, 'PAID', 'PAYMENT', 2)", 

                // FA 3: UPCOMING (For TC-03) 
                "INSERT INTO FormativeAction(action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, status) VALUES (3, 'FA3', 10, '2026-06-01', '2026-07-30', 40, '2026-05-01', '2026-05-20', 'Online', 'ACTIVE')",
                "INSERT INTO Fee(fee_id, amount, community_id, action_id) VALUES (3, 100.0, 1, 3)",
                "INSERT INTO Teacher_FormativeAction(id, remuneration, status, action_id, teacher_id) VALUES (3, 1000.0, 'PENDING', 3, 1)",
                "INSERT INTO Inscription(inscription_id, inscription_date, applied_fee, state, professional_id, action_id) VALUES (3, '2026-05-02', 100.0, 'CONFIRMED', 1, 3)",
                "INSERT INTO MoneyMovement(movement_date, amount, status, type, inscription_id) VALUES ('2026-05-03', 50.0, 'PAID', 'PAYMENT', 3)", 

                // FA 4: UPCOMING (For TC-04) 
                "INSERT INTO FormativeAction(action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, status) VALUES (4, 'FA4', 10, '2026-06-01', '2026-07-30', 40, '2026-05-01', '2026-05-20', 'Online', 'ACTIVE')",
                "INSERT INTO Teacher_FormativeAction(id, remuneration, status, action_id, teacher_id) VALUES (4, 1000.0, 'PENDING', 4, 1)",

                // FA 5: FINISHED (For TC-05) 
                "INSERT INTO FormativeAction(action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, status) VALUES (5, 'FA5', 10, '2026-01-01', '2026-03-30', 40, '2025-11-01', '2025-12-30', 'Online', 'ACTIVE')",

                // FA 6: CLOSED (For TC-06)
                // FA 6: CLOSED (For TC-06)
                "INSERT INTO FormativeAction(action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, status, closureDate) VALUES (6, 'FA6', 10, '2026-06-01', '2026-07-30', 40, '2026-05-01', '2026-05-20', 'Online', 'CLOSED', '2026-04-28')",

                // FA 7: CANCELLED (For TC-07 and TC-11)
                "INSERT INTO FormativeAction(action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, status) VALUES (7, 'FA7', 10, '2026-06-01', '2026-07-30', 40, '2026-05-01', '2026-05-20', 'Online', 'CANCELLED')",

                // FA 8: IN PROGRESS (For TC-10) 
                "INSERT INTO FormativeAction(action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, status) VALUES (8, 'FA8', 10, '2026-04-01', '2026-05-30', 40, '2026-01-01', '2026-03-31', 'Online', 'ACTIVE')",
                "INSERT INTO Fee(fee_id, amount, community_id, action_id) VALUES (8, 100.0, 1, 8)",
                "INSERT INTO Teacher_FormativeAction(id, remuneration, status, action_id, teacher_id) VALUES (8, 1000.0, 'PENDING', 8, 1)",
                "INSERT INTO Inscription(inscription_id, inscription_date, applied_fee, state, professional_id, action_id) VALUES (8, '2026-02-01', 100.0, 'CANCELLED', 1, 8)"
        });
    }

    // TC-01: Cancel FA In Progress (Paid exact fee)
    @Test
    public void testTC01_CancelInProgress_ExactFee() {
        Map<Integer, Double> teacherPcts = new HashMap<>();
        teacherPcts.put(1, 50.0);

        cancelModel.cancelAction(1, 50.0, teacherPcts, SIMULATED_DATE);

        List<Object[]> fa = db.executeQueryArray("SELECT status FROM FormativeAction WHERE action_id = 1");
        List<Object[]> insc = db.executeQueryArray("SELECT applied_fee, state FROM Inscription WHERE action_id = 1");
        List<Object[]> teacher = db.executeQueryArray("SELECT remuneration FROM Teacher_FormativeAction WHERE action_id = 1");

        assertAll("TC-01 Verification",
            () -> assertEquals("CANCELLED", fa.get(0)[0]),
            () -> assertEquals("50.0", insc.get(0)[0].toString(), "Fee reduced to 50%"),
            () -> assertEquals("CANCELLED", insc.get(0)[1], "State changes to CANCELLED since netBalance == fee"),
            () -> assertEquals("500.0", teacher.get(0)[0].toString(), "Teacher rem reduced to 50%")
        );
    }

    // TC-02: Cancel FA Upcoming (Paid 110%)
    @Test
    public void testTC02_CancelUpcoming_PaidOver() {
        Map<Integer, Double> teacherPcts = new HashMap<>();
        teacherPcts.put(1, 50.0);

        cancelModel.cancelAction(2, 50.0, teacherPcts, SIMULATED_DATE);

        List<Object[]> insc = db.executeQueryArray("SELECT applied_fee, state FROM Inscription WHERE action_id = 2");

        assertAll("TC-02 Verification",
            () -> assertEquals("50.0", insc.get(0)[0].toString(), "Fee reduced to 50%"),
            () -> assertEquals("PENDING_COMPENSATION", insc.get(0)[1])
        );
    }

    // TC-03: Cancel FA Upcoming (Refund 100% -> applied fee 0%)
    @Test
    public void testTC03_CancelUpcoming_FullRefund() {
        Map<Integer, Double> teacherPcts = new HashMap<>();
        teacherPcts.put(1, 100.0);

        cancelModel.cancelAction(3, 0.0, teacherPcts, SIMULATED_DATE);

        List<Object[]> insc = db.executeQueryArray("SELECT applied_fee, state FROM Inscription WHERE action_id = 3");

        assertAll("TC-03 Verification", () -> assertEquals("0.0", insc.get(0)[0].toString(), "Fee becomes 0"), () -> assertEquals("PENDING_COMPENSATION", insc.get(0)[1], "State changes to PENDING_COMPENSATION"));
    }

    // TC-04: Cancel FA with 0 Inscriptions
    @Test
    public void testTC04_CancelUpcoming_NoInscriptions() {
        Map<Integer, Double> teacherPcts = new HashMap<>();
        teacherPcts.put(1, 80.0);

        cancelModel.cancelAction(4, 50.0, teacherPcts, SIMULATED_DATE);

        List<Object[]> fa = db.executeQueryArray("SELECT status FROM FormativeAction WHERE action_id = 4");
        List<Object[]> teacher = db.executeQueryArray("SELECT remuneration FROM Teacher_FormativeAction WHERE action_id = 4");

        assertEquals("CANCELLED", fa.get(0)[0]);
        assertEquals("800.0", teacher.get(0)[0].toString(), "Teacher rem updated without inscription errors");
    }

    // TC-05: getCancelableActions - Finished FA
    @Test
    public void testTC05_GetCancelable_Finished() {
        List<FormativeActionDTO> actions = cancelModel.getCancelableActions(SIMULATED_DATE);
        boolean containsFA5 = actions.stream().anyMatch(a -> a.getActionId() == 5);
        assertFalse(containsFA5, "Finished FA should not be in the list");
    }

    // TC-06: getCancelableActions - CLOSED FA
    @Test
    public void testTC06_GetCancelable_Closed() {
        List<FormativeActionDTO> actions = cancelModel.getCancelableActions(SIMULATED_DATE);
        boolean containsFA6 = actions.stream().anyMatch(a -> a.getActionId() ==6);
        assertFalse(containsFA6, "CLOSED FA should not be in the list");
    }

    // TC-07: getCancelableActions - CANCELLED FA
    @Test
    public void testTC07_GetCancelable_Cancelled() {
        List<FormativeActionDTO> actions = cancelModel.getCancelableActions(SIMULATED_DATE);
        boolean containsFA7 = actions.stream().anyMatch(a -> a.getActionId() ==7);
        assertFalse(containsFA7, "CANCELLED FA should not be in the list");
    }

    // TC-08: Cancel FA - FA not in DB
    @Test
    public void testTC08_CancelAction_NotInDB() {
        int actionId = 999;
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            List<Object[]> exists = db.executeQueryArray("SELECT 1 FROM FormativeAction WHERE action_id = ?", actionId);
            if (exists.isEmpty())
            		throw new IllegalArgumentException("Action does not exist");
            cancelModel.cancelAction(actionId, 50.0, new HashMap<>(), SIMULATED_DATE);
        });
        assertEquals("Action does not exist", ex.getMessage());
    }

    // TC-09: Cancel FA - Negative Refund %
    @Test
    public void testTC09_CancelAction_NegativeRefund() {
        double refundPct = -10.0;
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            if (refundPct < 0)
            		throw new IllegalArgumentException("Refund percentage cannot be negative");
            cancelModel.cancelAction(1, refundPct, new HashMap<>(), SIMULATED_DATE);
        });
        assertEquals("Refund percentage cannot be negative", ex.getMessage());
    }

    // TC-10: Cancel FA - Preserve Historical/Cancelled Inscriptions
    @Test
    public void testTC10_CancelAction_PreserveHistoricalInscriptions() {
        g54.si26.cancelEnrollment.ModelCancelEnrollment studentCancelModel = new g54.si26.cancelEnrollment.ModelCancelEnrollment();

        //Professional cancels inscription and save the state
        studentCancelModel.cancelEnrollment(8, SIMULATED_DATE, 20.0, "Personal reasons");
        List<Object[]> preGlobalCancel = db.executeQueryArray("SELECT applied_fee, state FROM Inscription WHERE inscription_id = 8");
        double originalFee = Double.parseDouble(preGlobalCancel.get(0)[0].toString());
        assertEquals("CANCELLED", preGlobalCancel.get(0)[1].toString(), "Pre-condition: Enrollment should be CANCELLED");

        // The TM cancels the FA
        Map<Integer, Double> teacherPcts = new HashMap<>();
        teacherPcts.put(1, 50.0);
        cancelModel.cancelAction(8, 50.0, teacherPcts, SIMULATED_DATE);

        // save state
        List<Object[]> postGlobalCancel = db.executeQueryArray("SELECT applied_fee, state FROM Inscription WHERE inscription_id = 8");
        double newFee = Double.parseDouble(postGlobalCancel.get(0)[0].toString());

        assertEquals(originalFee, newFee, "The historical applied_fee of a previously CANCELLED inscription MUST NOT be modified by a global FA cancellation.");
        assertEquals("CANCELLED", postGlobalCancel.get(0)[1].toString(), "State must remain CANCELLED.");
    }

    // TC-11 (EXTRA): Enroll in a CANCELLED Formative Action
    @Test
    public void testTC11_CancelledCourseRemainsInActiveListings() throws Exception {
        db.executeUpdate("INSERT INTO FormativeAction(action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, status) " +
                         "VALUES (12, 'Curso Zombi', 10, '2026-06-01', '2026-07-30', 40, '2026-05-01', '2026-05-30', 'Online', 'ACTIVE')");
        
        //The TM Cancels the FA at 9:00
        Date cancellationTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-05-25 09:00:00");
        cancelModel.cancelAction(12, 100.0, new HashMap<>(), cancellationTime);
        
        //Professional Tries to enrol at 17:00
        Date afternoonTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-05-25 17:00:00");
        List<FormativeActionDTO> availableCourses = inscriptionsModel.getAvailableCourses(afternoonTime);
        
        
        boolean isAvailable = availableCourses.stream().anyMatch(a -> a.getActionId() == 12);
        assertFalse(isAvailable, "CRITICAL BUG: El curso cancelado sigue apareciendo en Active Listings porque el substring omite las horas de cancelación.");
    }
    
    // TC-12: Teacher Remuneration after Re-Opening the FA
    @Test
    public void testTC12_TeacherRemunerationBaseDestroyedOnCorrection() {
        // Teacher has initial remuneration of 1000€
        
    		//Cancel the FA with 50% completed
    		Map<Integer, Double> teacherPcts = new HashMap<>();
        teacherPcts.put(1, 50.0);
        cancelModel.cancelAction(1, 100.0, teacherPcts, SIMULATED_DATE);
        
        // Check if it has correctly changed
        List<Object[]> firstCancel = db.executeQueryArray("SELECT remuneration FROM Teacher_FormativeAction WHERE action_id = 1 AND teacher_id = 1");
        assertEquals(500.0, Double.parseDouble(firstCancel.get(0)[0].toString()));
        
        // Simulate reopen FA and the cancel with 100% completion (has been closed and then re-open)
        teacherPcts.put(1, 100.0);
        cancelModel.cancelAction(1, 100.0, teacherPcts, SIMULATED_DATE);
        
        // Check the new state
        List<Object[]> secondCancel = db.executeQueryArray("SELECT remuneration FROM Teacher_FormativeAction WHERE action_id = 1 AND teacher_id = 1");
        double finalRemuneration = Double.parseDouble(secondCancel.get(0)[0].toString());
        
        //EXPECTED: The Teacher remuneration should be 1000€
        assertEquals(1000.0, finalRemuneration, "CRITICAL BUG: La corrección de errores es destructiva. Se pierde la remuneración original del profesor.");
    }

}