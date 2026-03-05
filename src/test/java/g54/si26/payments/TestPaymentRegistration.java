package g54.si26.payments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import g54.si26.DTOs.*;
import g54.si26.utils.*;


import g54.si26.DTOs.EnrollmentRecordDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;


public class TestPaymentRegistration {
    private g54.si26.utils.Database db = new Database();
    private PaymentModel model = new PaymentModel();

    @BeforeEach
    public void setUp() {
        db.createDatabase(false);
        // Clean and Load initial test data
        db.executeUpdate("DELETE FROM Payment");
        db.executeUpdate("DELETE FROM Inscription");
        db.executeUpdate("DELETE FROM FormativeAction");
        db.executeUpdate("DELETE FROM Professional");
        db.executeUpdate("DELETE FROM Teacher");

        // Insert Teacher
        db.executeUpdate("INSERT INTO Teacher (teacher_id, name, fiscal_id, email, phone) VALUES (1, 'Test Teacher', '123B', 't@t.com', '123')");
        
        // Insert Formative Action
        db.executeUpdate("INSERT INTO FormativeAction (action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, fee, status, teacher_id) " +
                         "VALUES (1, 'Active Course', 10, '2026-05-01', '2026-05-02', '10', '2026-01-01', '2026-04-01', 'Loc', 100.0, 'ACTIVE', 1)");

        // Insert Professional
        db.executeUpdate("INSERT INTO Professional (professional_id, name, surname, phone, email) VALUES (1, 'John', 'Doe', '555', 'john@doe.com')");

        // Insert Inscription
        db.executeUpdate("INSERT INTO Inscription (inscription_id, inscription_date, fee, state, professional_id, action_id) " +
                         "VALUES (1, '2026-03-02', 100.0, 'RECEIVED', 1, 1)");
    }

    @Test
    public void testGetPendingEnrollments() {
        List<EnrollmentRecordDTO> pending = model.getPendingEnrollments();
        assertEquals(1, pending.size());
        assertEquals("Active Course", pending.get(0).getCourseName());
    }

    @Test
    public void testRegisterPaymentSuccess() {
        model.registerPayment(1, 100.0, "2026-03-02");
        
        List<EnrollmentRecordDTO> results = db.executeQueryPojo(EnrollmentRecordDTO.class, 
            "SELECT state FROM Inscription WHERE inscription_id = 1");
        assertEquals("CONFIRMED", results.get(0).getState());
    }

    @Test
    public void testTwoWorkingDaysRule() {
        LocalDate regDate = LocalDate.of(2026, 3, 2); // Monday
        assertFalse(model.isAfterTwoWorkingDays(regDate, LocalDate.of(2026, 3, 4)), "Wednesday is fine");
        assertTrue(model.isAfterTwoWorkingDays(regDate, LocalDate.of(2026, 3, 5)), "Thursday is late");
    }

    @Test
    public void testPaymentBeforeRegistration() {
        ApplicationException ex = assertThrows(ApplicationException.class, () -> {
            model.registerPayment(1, 100.0, "2025-01-01");
        });
        assertTrue(ex.getMessage().contains("cannot be before the registration date"));
    }
}
