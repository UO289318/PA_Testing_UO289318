package g54.si26.payments;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import g54.si26.DTOs.EnrollmentRecordDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;

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
        String sql = "SELECT " +
                     "i.inscription_id AS inscriptionId, " +
                     "fa.name AS courseName, " +
                     "p.name || ' ' || p.surname AS professionalName, " +
                     "p.email AS professionalEmail, " +
                     "i.fee AS fee, " +
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
        // 1. Fetch current enrollment details to validate
        String sqlSelect = "SELECT inscription_id AS inscriptionId, inscription_date AS registrationDate, fee, state " +
                           "FROM Inscription WHERE inscription_id = ?";
        List<EnrollmentRecordDTO> results = db.executeQueryPojo(EnrollmentRecordDTO.class, sqlSelect, inscriptionId);
        
        if (results.isEmpty()) {
            throw new ApplicationException("Enrollment not found.");
        }
        EnrollmentRecordDTO enrollment = results.get(0);

        // 2. Validate Amount (Restriction: Exact amount required in this sprint)
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
        
        if (isAfterTwoWorkingDays(regDate, payDate)) {
            throw new ApplicationException("Validation Error: Payment received after the 2 working days deadline.");
        }

        // 4. Record Payment
        String sqlInsertPayment = "INSERT INTO Payment (amountPaid, inscription_id, payment_date) VALUES (?, ?, ?)";
        db.executeUpdate(sqlInsertPayment, amount, inscriptionId, paymentDateStr);

        // 5. Update Inscription Status to CONFIRMED
        String sqlUpdateStatus = "UPDATE Inscription SET state = 'CONFIRMED' WHERE inscription_id = ?";
        db.executeUpdate(sqlUpdateStatus, inscriptionId);
    }

    /**
     * Business Logic: Checks if payDate is more than 2 working days after regDate.
     */
    public boolean isAfterTwoWorkingDays(LocalDate regDate, LocalDate payDate) {
        LocalDate deadline = regDate;
        int workingDaysAdded = 0;
        while (workingDaysAdded < 2) {
            deadline = deadline.plusDays(1);
            if (deadline.getDayOfWeek() != DayOfWeek.SATURDAY && deadline.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDaysAdded++;
            }
        }
        return payDate.isAfter(deadline);
    }
}
