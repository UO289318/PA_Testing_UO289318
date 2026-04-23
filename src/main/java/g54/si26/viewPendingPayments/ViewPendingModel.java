package g54.si26.viewPendingPayments;

import java.util.List;
import g54.si26.utils.Database; 

public class ViewPendingModel {

    private Database db = new Database(); 

    public List<Object[]> getPendingPaymentsData(String typeFilter) {
        
        // Consulta limpia, sin comentarios '--' que anulen el resto del texto
        String sql = 
            "WITH ProfessionalPayments AS ( " +
            "    SELECT p.name || ' ' || p.surname AS profName, fa.name AS faName, " +
            "           COALESCE(SUM(mm.amount), 0) AS totalPaid, " +
            "           i.applied_fee AS expectedAmount, UPPER(i.state) AS state " +
            "    FROM Inscription i " +
            "    JOIN Professional p ON i.professional_id = p.professional_id " +
            "    JOIN FormativeAction fa ON i.action_id = fa.action_id " +
            "    LEFT JOIN MoneyMovement mm ON mm.inscription_id = i.inscription_id " +
            "    GROUP BY i.inscription_id, p.name, p.surname, fa.name, i.applied_fee, i.state " +
            "), " +
            "TeacherPayments AS ( " +
            "    SELECT t.name AS profName, fa.name AS faName, " +
            "           COALESCE(SUM(ABS(mm.amount)), 0) AS totalPaid, " +
            "           inv.totalAmount AS expectedAmount " +
            "    FROM Invoice inv " +
            "    JOIN Teacher t ON inv.teacher_id = t.teacher_id " +
            "    JOIN FormativeAction fa ON inv.action_id = fa.action_id " +
            "    LEFT JOIN MoneyMovement mm ON mm.invoice_id = inv.invoice_id " +
            "    GROUP BY inv.invoice_id, t.name, fa.name, inv.totalAmount " +
            "), " +
            "PendingMovements AS ( " +
            "    SELECT profName, (totalPaid - expectedAmount) AS amount, faName, 'Refund' AS reason " +
            "    FROM ProfessionalPayments WHERE (state != 'CANCELLED' OR state IS NULL) AND totalPaid > expectedAmount " +
            "    UNION ALL " +
            "    SELECT profName, totalPaid AS amount, faName, 'Refund' AS reason " +
            "    FROM ProfessionalPayments WHERE state = 'CANCELLED' AND totalPaid > 0 " +
            "    UNION ALL " +
            "    SELECT profName, (expectedAmount - totalPaid) AS amount, faName, 'Compensation' AS reason " +
            "    FROM ProfessionalPayments WHERE (state != 'CANCELLED' OR state IS NULL) AND totalPaid > 0 AND totalPaid < expectedAmount " +
            "    UNION ALL " +
            "    SELECT profName, (expectedAmount - totalPaid) AS amount, faName, 'Compensation' AS reason " +
            "    FROM TeacherPayments WHERE totalPaid < expectedAmount " +
            "    UNION ALL " +
            "    SELECT profName, (totalPaid - expectedAmount) AS amount, faName, 'Refund' AS reason " +
            "    FROM TeacherPayments WHERE totalPaid > expectedAmount " +
            ") " +
            "SELECT profName, amount, faName, reason FROM PendingMovements WHERE amount > 0 ";

        if (!"ALL".equals(typeFilter)) {
            sql += " AND reason = ? ";
            return db.executeQueryArray(sql, typeFilter);
        }
        
        return db.executeQueryArray(sql);
    }
}