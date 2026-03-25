package g54.si26.teacherpayments;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import g54.si26.DTOs.TeacherInvoiceDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;

/**
 * Model for the "Record Teacher Payments" functionality.
 * Updated to support the new Payment and MoneyMovement schema.
 */
public class TeacherPaymentModel {
    private Database db = new Database();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Retrieves invoices that have not been fully paid yet.
     */
    public List<TeacherInvoiceDTO> getPendingInvoices() {
        // Consultamos el total pagado desde MoneyMovement filtrando por tipo PAYMENT del Registro Formal asociado
        String sql = "SELECT " +
                     "i.invoice_id AS invoiceId, " +
                     "t.name AS teacherName, " +
                     "fa.name AS courseName, " +
                     "i.totalAmount AS totalAmount, " +
                     "i.invoice_date AS invoiceDate, " +
                     "(SELECT COALESCE(ABS(SUM(mm.amount)), 0) FROM MoneyMovement mm " +
                     " JOIN Payment p ON mm.payment_id = p.payment_id " +
                     " WHERE mm.invoice_id = i.invoice_id AND p.type = 'PAYMENT') AS amountPaid " +
                     "FROM Invoice i " +
                     "JOIN Teacher t ON i.teacher_id = t.teacher_id " +
                     "JOIN FormativeAction fa ON i.action_id = fa.action_id " +
                     "WHERE amountPaid < totalAmount";
        return db.executeQueryPojo(TeacherInvoiceDTO.class, sql);
    }

    /**
     * Registers a new bank transfer for an invoice.
     * Updates: inserts a formal Payment and a real MoneyMovement (with negative sign).
     */
    public void registerTeacherPayment(int invoiceId, double amount, String transferDateStr) {
        // 1. Fetch invoice details
        String sqlSelect = "SELECT i.invoice_id AS invoiceId, i.totalAmount AS totalAmount, i.invoice_date AS invoiceDate, " +
                           "(SELECT COALESCE(ABS(SUM(mm.amount)), 0) FROM MoneyMovement mm " +
                           " JOIN Payment p ON mm.payment_id = p.payment_id " +
                           " WHERE mm.invoice_id = i.invoice_id AND p.type = 'PAYMENT') AS amountPaid " +
                           "FROM Invoice i WHERE i.invoice_id = ?";
        List<TeacherInvoiceDTO> results = db.executeQueryPojo(TeacherInvoiceDTO.class, sqlSelect, invoiceId);
        
        if (results.isEmpty()) {
            throw new ApplicationException("Invoice not found.");
        }
        TeacherInvoiceDTO invoice = results.get(0);

        // 2. Validate Amount
        double pending = invoice.getTotalAmount() - invoice.getAmountPaid();
        if (Math.abs(amount - pending) > 0.001) {
            throw new ApplicationException("Error: The amount transferred (" + amount + ") must exactly match the pending amount (" + pending + ").");
        }

        // 3. Validate Date
        LocalDate invDate;
        LocalDate transDate;
        try {
            String invDateStr = invoice.getInvoiceDate();
            if (invDateStr.length() > 10) invDateStr = invDateStr.substring(0, 10);
            invDate = LocalDate.parse(invDateStr, FORMATTER);
            transDate = LocalDate.parse(transferDateStr, FORMATTER);
        } catch (Exception e) {
            throw new ApplicationException("Invalid date format. Expected: YYYY-MM-DD");
        }

        if (transDate.isBefore(invDate)) {
            throw new ApplicationException("Error: Transfer date cannot be before the invoice date (" + invDate + ").");
        }

        // 4. Record Payment (Formal)
        String sqlInsertPayment = "INSERT INTO Payment (amount, payment_date, status, type, invoice_id) VALUES (?, ?, ?, ?, ?)";
        db.executeUpdate(sqlInsertPayment, amount, transferDateStr, "PAID", "PAYMENT", invoiceId);
        
        // Get generated ID
        int paymentId = db.executeQueryPojo(g54.si26.DTOs.PaymentDTO.class, "SELECT last_insert_rowid() AS paymentId").get(0).getPaymentId();

        // 5. Record Money Movement (Real) - Negative for Outcome
        String sqlInsertMovement = "INSERT INTO MoneyMovement (amount, movement_date, status, payment_id, invoice_id) VALUES (?, ?, ?, ?, ?)";
        db.executeUpdate(sqlInsertMovement, -amount, transferDateStr, "EXECUTED", paymentId, invoiceId);
        
        // 6. Update Invoice status to PAID if fully covered
        if (Math.abs(amount - pending) < 0.001) {
            db.executeUpdate("UPDATE Invoice SET status = 'PAID' WHERE invoice_id = ?", invoiceId);
        }
    }
}
