package g54.si26.payments;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import g54.si26.DTOs.EnrollmentRecordDTO;
import g54.si26.DTOs.PaymentDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;
import g54.si26.utils.Util;

/**
 * Model for the "Register Payments" functionality.
 * Handles all database interactions and business rules.
 */
public class PaymentModel {
    private Database db = new Database();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Retrieves all enrollments that are in 'RECEIVED' state (pending payment)
     * for formative actions that are 'ACTIVE'.
     */
    public List<EnrollmentRecordDTO> getPendingEnrollments() {
        // Cambio: i.fee -> i.applied_fee
        String sql = "SELECT " +
                     "i.inscription_id AS inscriptionId, " +
                     "fa.name AS courseName, " +
                     "p.name || ' ' || p.surname AS professionalName, " +
                     "p.email AS professionalEmail, " +
                     "i.applied_fee AS fee, " +
                     "i.inscription_date AS registrationDate " +
                     "FROM Inscription i " +
                     "JOIN FormativeAction fa ON i.action_id = fa.action_id " +
                     "JOIN Professional p ON i.professional_id = p.professional_id " +
                     "WHERE i.state = 'RECEIVED' AND fa.status = 'ACTIVE'";
        return db.executeQueryPojo(EnrollmentRecordDTO.class, sql);
    }

    /**
     * Business Logic: Registers a new payment and updates enrollment status if correct.
     */
    public void registerPayment(int inscriptionId, double amount, String paymentDateStr) {
        // 1. Fetch current enrollment details to validate (Cambio: fee -> applied_fee)
        String sqlSelect = "SELECT inscription_id AS inscriptionId, inscription_date AS registrationDate, applied_fee AS fee, state " +
                           "FROM Inscription WHERE inscription_id = ?";
        List<EnrollmentRecordDTO> results = db.executeQueryPojo(EnrollmentRecordDTO.class, sqlSelect, inscriptionId);
        
        if (results.isEmpty()) {
            throw new ApplicationException("Enrollment not found.");
        }
        EnrollmentRecordDTO enrollment = results.get(0);

        // 2. Validate Amount
        if (Math.abs(amount - enrollment.getFee()) > 0.001) {
            throw new ApplicationException("Error: The amount paid (" + amount + ") does not match the required fee (" + enrollment.getFee() + ").");
        }

        // 3. Validate Date Logic
        LocalDate regDate;
        LocalDate payDate;
        try {
            String regDateStr = enrollment.getRegistrationDate();
            if (regDateStr.length() > 10) regDateStr = regDateStr.substring(0, 10);
            
            regDate = LocalDate.parse(regDateStr, FORMATTER);
            payDate = LocalDate.parse(paymentDateStr, FORMATTER);
        } catch (Exception e) {
            throw new ApplicationException("Invalid date format. Expected: YYYY-MM-DD");
        }
        
        if (payDate.isBefore(regDate)) {
            throw new ApplicationException("Error: Payment date cannot be before the registration date (" + regDate + ").");
        }
        
        if (Util.isAfterTwoWorkingDays(regDate, payDate)) {
            throw new ApplicationException("Validation Error: Payment received after the 2 working days deadline.");
        }

        // 4. Record Payment (Formal)
        String sqlInsertPayment = "INSERT INTO Payment (amount, payment_date, status, type, inscription_id) VALUES (?, ?, ?, ?, ?)";
        db.executeUpdate(sqlInsertPayment, amount, paymentDateStr, "PAID", "PAYMENT", inscriptionId);
        
        // Get the generated ID for the payment
        int paymentId = db.executeQueryPojo(PaymentDTO.class, "SELECT last_insert_rowid() AS paymentId").get(0).getPaymentId();

        // 5. Record Money Movement (Real) - linked to the formal payment
        String sqlInsertMovement = "INSERT INTO MoneyMovement (amount, movement_date, status, payment_id, inscription_id) VALUES (?, ?, ?, ?, ?)";
        db.executeUpdate(sqlInsertMovement, amount, paymentDateStr, "EXECUTED", paymentId, inscriptionId);

        // 6. Update Inscription Status to CONFIRMED
        String sqlUpdateStatus = "UPDATE Inscription SET state = 'CONFIRMED' WHERE inscription_id = ?";
        db.executeUpdate(sqlUpdateStatus, inscriptionId);
    }
}
