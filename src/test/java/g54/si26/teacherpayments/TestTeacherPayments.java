package g54.si26.teacherpayments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import g54.si26.DTOs.TeacherInvoiceDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;

public class TestTeacherPayments {
    private Database db = new Database();
    private TeacherPaymentModel model = new TeacherPaymentModel();

    @BeforeEach
    public void setUp() {
        db.createDatabase(false);
        db.executeUpdate("DELETE FROM MoneyMovement");
        db.executeUpdate("DELETE FROM Invoice");
        db.executeUpdate("DELETE FROM FormativeAction");
        db.executeUpdate("DELETE FROM Teacher");

        db.executeUpdate("INSERT INTO Teacher (teacher_id, name, fiscal_id, email, phone) VALUES (1, 'Teacher 1', '111X', 't1@t.com', '111')");
        db.executeUpdate("INSERT INTO FormativeAction (action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, fee, status, teacher_id) " +
                         "VALUES (1, 'Course 1', 10, '2026-05-01', '2026-05-02', '10', '2026-01-01', '2026-04-01', 'Loc', 100.0, 'ACTIVE', 1)");
        
        // Create an Invoice
        db.executeUpdate("INSERT INTO Invoice (invoice_id, invoice_date, netAmount, vat, totalAmount, teacher_id, action_id) " +
                         "VALUES (1, '2026-05-15', 80.0, 20.0, 100.0, 1, 1)");
    }

    @Test
    public void testGetPendingInvoices() {
        List<TeacherInvoiceDTO> pending = model.getPendingInvoices();
        assertEquals(1, pending.size());
        assertEquals(100.0, pending.get(0).getPendingAmount());
    }

    @Test
    public void testRegisterTeacherPaymentSuccess() {
        model.registerTeacherPayment(1, 100.0, "2026-05-20");
        
        List<TeacherInvoiceDTO> pending = model.getPendingInvoices();
        assertEquals(0, pending.size(), "Invoice should no longer be pending after full payment");
    }

    @Test
    public void testRegisterTeacherPaymentBeforeInvoice() {
        ApplicationException ex = assertThrows(ApplicationException.class, () -> {
            model.registerTeacherPayment(1, 100.0, "2026-05-10"); // Invoice is May 15
        });
        assertTrue(ex.getMessage().contains("cannot be before the invoice date"));
    }

    @Test
    public void testRegisterTeacherPaymentWrongAmount() {
        ApplicationException ex = assertThrows(ApplicationException.class, () -> {
            model.registerTeacherPayment(1, 50.0, "2026-05-20");
        });
        assertTrue(ex.getMessage().contains("must exactly match the pending amount"));
    }
}
