package g54.si26.moneyMovements;

import java.util.List;
import g54.si26.DTOs.EnrollmentRecordDTO;
import g54.si26.DTOs.TeacherInvoiceDTO;
import g54.si26.DTOs.MoneyMovementDTO;
import g54.si26.utils.Database;
import g54.si26.utils.ApplicationException;

public class MoneyMovementModel {
    private Database db = new Database();

    public List<EnrollmentRecordDTO> getAllEnrollments() {
        String sql = "SELECT " +
                     "i.inscription_id AS inscriptionId, " +
                     "fa.name AS courseName, " +
                     "p.name || ' ' || p.surname AS professionalName, " +
                     "i.applied_fee AS fee, " +
                     "i.state AS state, " +
                     "i.inscription_date AS registrationDate, " +
                     "(SELECT COALESCE(SUM(amount), 0) FROM MoneyMovement WHERE inscription_id = i.inscription_id) AS netBalance " +
                     "FROM Inscription i " +
                     "JOIN FormativeAction fa ON i.action_id = fa.action_id " +
                     "JOIN Professional p ON i.professional_id = p.professional_id";
        return db.executeQueryPojo(EnrollmentRecordDTO.class, sql);
    }

    public List<EnrollmentRecordDTO> getEnrollmentsPendingCompensation() {
        String sql = "SELECT * FROM (" +
                     "SELECT i.inscription_id AS inscriptionId, fa.name AS courseName, p.name || ' ' || p.surname AS professionalName, " +
                     "i.applied_fee AS fee, i.state AS state, i.inscription_date AS registrationDate, " +
                     "((SELECT COALESCE(SUM(amount), 0) FROM MoneyMovement WHERE inscription_id = i.inscription_id) - i.applied_fee) AS netBalance " +
                     "FROM Inscription i JOIN FormativeAction fa ON i.action_id = fa.action_id " +
                     "JOIN Professional p ON i.professional_id = p.professional_id" +
                     ") WHERE netBalance > 0.001";
        return db.executeQueryPojo(EnrollmentRecordDTO.class, sql);
    }

    public List<TeacherInvoiceDTO> getAllInvoices() {
        String sql = "SELECT " +
                     "i.invoice_id AS invoiceId, " +
                     "t.name AS teacherName, " +
                     "fa.name AS courseName, " +
                     "i.netAmount AS netAmount, " +
                     "i.vat AS vat, " +
                     "i.totalAmount AS totalAmount, " +
                     "i.invoice_date AS invoiceDate, " +
                     "i.status AS status, " +
                     "(SELECT COALESCE(SUM(amount), 0) FROM MoneyMovement WHERE invoice_id = i.invoice_id) AS netBalance " +
                     "FROM Invoice i " +
                     "JOIN Teacher t ON i.teacher_id = t.teacher_id " +
                     "JOIN FormativeAction fa ON i.action_id = fa.action_id";
        return db.executeQueryPojo(TeacherInvoiceDTO.class, sql);
    }

    public List<TeacherInvoiceDTO> getInvoicesPendingCompensation() {
        String sql = "SELECT * FROM (" +
                     "SELECT i.invoice_id AS invoiceId, t.name AS teacherName, fa.name AS courseName, " +
                     "i.netAmount AS netAmount, i.vat AS vat, i.totalAmount AS totalAmount, " +
                     "(ABS((SELECT COALESCE(SUM(amount), 0) FROM MoneyMovement WHERE invoice_id = i.invoice_id)) - i.totalAmount) AS netBalance " +
                     "FROM Invoice i JOIN Teacher t ON i.teacher_id = t.teacher_id " +
                     "JOIN FormativeAction fa ON i.action_id = fa.action_id" +
                     ") WHERE netBalance > 0.001";
        return db.executeQueryPojo(TeacherInvoiceDTO.class, sql);
    }

    public List<MoneyMovementDTO> getAllMovements() {
        String sql = "SELECT mm.movement_id AS movementId, mm.amount, mm.movement_date AS movementDate, mm.status, mm.type, mm.inscription_id AS inscriptionId, mm.invoice_id AS invoiceId, " +
                     "COALESCE('Insc: ' || p.surname || ' (' || fa.name || ')', 'Inv: ' || t.name || ' (' || fa2.name || ')') AS relatedTo " +
                     "FROM MoneyMovement mm " +
                     "LEFT JOIN Inscription i ON mm.inscription_id = i.inscription_id " +
                     "LEFT JOIN Professional p ON i.professional_id = p.professional_id " +
                     "LEFT JOIN FormativeAction fa ON i.action_id = fa.action_id " +
                     "LEFT JOIN Invoice inv ON mm.invoice_id = inv.invoice_id " +
                     "LEFT JOIN Teacher t ON inv.teacher_id = t.teacher_id " +
                     "LEFT JOIN FormativeAction fa2 ON inv.action_id = fa2.action_id " +
                     "ORDER BY mm.movement_date DESC, mm.movement_id DESC";
        return db.executeQueryPojo(MoneyMovementDTO.class, sql);
    }

    public List<MoneyMovementDTO> getMovementsByInscription(int id) {
        String sql = "SELECT movement_id AS movementId, amount, movement_date AS movementDate, status, type, inscription_id AS inscriptionId " +
                     "FROM MoneyMovement WHERE inscription_id = ? ORDER BY movement_date DESC";
        return db.executeQueryPojo(MoneyMovementDTO.class, sql, id);
    }

    public List<MoneyMovementDTO> getMovementsByInvoice(int id) {
        String sql = "SELECT movement_id AS movementId, amount, movement_date AS movementDate, status, type, invoice_id AS invoiceId " +
                     "FROM MoneyMovement WHERE invoice_id = ? ORDER BY movement_date DESC";
        return db.executeQueryPojo(MoneyMovementDTO.class, sql, id);
    }

    public void registerMovement(Integer inscriptionId, Integer invoiceId, double amount, String date, String status) {
        String sql = "INSERT INTO MoneyMovement (amount, movement_date, status, type, inscription_id, invoice_id) VALUES (?, ?, ?, 'PAYMENT', ?, ?)";
        db.executeUpdate(sql, amount, date, status, inscriptionId, invoiceId);
        
        if (inscriptionId != null) updateInscriptionStatus(inscriptionId);
        if (invoiceId != null) updateInvoiceStatus(invoiceId);
    }

    private void updateInscriptionStatus(int id) {
        String sql = "SELECT applied_fee AS fee, (SELECT COALESCE(SUM(amount), 0) FROM MoneyMovement WHERE inscription_id = ?) AS netBalance FROM Inscription WHERE inscription_id = ?";
        List<EnrollmentRecordDTO> results = db.executeQueryPojo(EnrollmentRecordDTO.class, sql, id, id);
        if (!results.isEmpty()) {
            EnrollmentRecordDTO e = results.get(0);
            if (e.getNetBalance() > e.getFee()) {
                db.executeUpdate("UPDATE Inscription SET state = 'PENDING_COMPENSATION' WHERE inscription_id = ?", id);
            } else if (e.getNetBalance() >= e.getFee()) {
                db.executeUpdate("UPDATE Inscription SET state = 'CONFIRMED' WHERE inscription_id = ?", id);
            } else {
                db.executeUpdate("UPDATE Inscription SET state = 'RECEIVED' WHERE inscription_id = ?", id);
            }
        }
    }

    private void updateInvoiceStatus(int id) {
        String sql = "SELECT totalAmount, (SELECT COALESCE(SUM(amount), 0) FROM MoneyMovement WHERE invoice_id = ?) AS netBalance FROM Invoice WHERE invoice_id = ?";
        List<TeacherInvoiceDTO> results = db.executeQueryPojo(TeacherInvoiceDTO.class, sql, id, id);
        if (!results.isEmpty()) {
            TeacherInvoiceDTO i = results.get(0);
            if (Math.abs(i.getNetBalance()) >= i.getTotalAmount()) {
                db.executeUpdate("UPDATE Invoice SET status = 'PAID' WHERE invoice_id = ?", id);
            } else {
                db.executeUpdate("UPDATE Invoice SET status = 'PENDING' WHERE invoice_id = ?", id);
            }
        }
    }
}
