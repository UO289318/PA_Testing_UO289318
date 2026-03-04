package g54.si26.financeConsulting;

import java.util.List;
import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;

public class FinancialConsultingModel {

    private Database db = new Database();

    // Gets the courses filtered by their status (All, Active, Not Active)
    public List<FormativeActionDTO> getFormativeActionsByStatus(String filter) {
        String sql = "SELECT action_id AS actionId, name, status FROM FormativeAction";
        
        if ("Active".equals(filter)) {
            sql += " WHERE status = 'ACTIVE'";
        } else if ("Not Active".equals(filter) || "Not Active (Closed/Cancelled)".equals(filter)) {
            sql += " WHERE status != 'ACTIVE'";
        }
        sql += " ORDER BY name ASC";
        
        return db.executeQueryPojo(FormativeActionDTO.class, sql);
    }

    public Object[] getCourseBasicData(int actionId) {
        String sql = "SELECT fa.name, fa.status, fa.inscriptionPeriodStart, fa.inscriptionPeriodEnd, " +
                     "fa.startDate, fa.spots, " +
                     "(fa.spots - (SELECT COUNT(*) FROM Inscription i WHERE i.action_id = fa.action_id AND i.state IN ('RECEIVED', 'CONFIRMED'))) as freeSpots " +
                     "FROM FormativeAction fa WHERE fa.action_id = ?";
        
        List<Object[]> rows = db.executeQueryArray(sql, actionId);
        if (rows.isEmpty()) {
            throw new ApplicationException("The Formative Action could not be found.");
        }
        return rows.get(0);
    }

    public List<Object[]> getMovements(int actionId) {
        String sql =
            // INCOME
            "SELECT i.inscription_date as date, " +
            "('Enrollment Payment - ' || p.name || ' ' || p.surname) as concept, " +
            "pay.amountPaid as amount, " +
            "1 as is_income " +
            "FROM Payment pay " +
            "INNER JOIN Inscription i ON pay.inscription_id = i.inscription_id " +
            "INNER JOIN Professional p ON i.professional_id = p.professional_id " +
            "WHERE i.action_id = ? " +
            "UNION ALL " +
            // EXPENSES
            "SELECT mm.movement_date as date, " +
            "('Teacher Payment - ' || t.name) as concept, " +
            "mm.amount as amount, " +
            "0 as is_income " +
            "FROM MoneyMovement mm " +
            "INNER JOIN Invoice inv ON mm.invoice_id = inv.invoice_id " +
            "INNER JOIN Teacher t ON inv.teacher_id = t.teacher_id " +
            "WHERE inv.action_id = ? " +
            "ORDER BY date ASC";

        return db.executeQueryArray(sql, actionId, actionId);
    }
}