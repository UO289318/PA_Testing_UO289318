package g54.si26.tmConsulting;

import java.util.List;
import g54.si26.utils.Database;

public class TMConsultingModel {
    private Database db = new Database();

    // 1. Obtener lista para el ComboBox filtrada por estado
    public List<Object[]> getActionList(String filter) {
        String sql = "SELECT action_id, name FROM FormativeAction";
        if ("Active".equals(filter)) {
            sql += " WHERE status = 'ACTIVE'";
        } else if ("NotActive".equals(filter)) {
            sql += " WHERE status IN ('CLOSED', 'CANCELLED')";
        }
        return db.executeQueryArray(sql);
    }

    // 2. Obtener detalles básicos del curso y plazas libres
    public Object[] getActionDetails(String actionId) {
        String sql = "SELECT name, status, " +
                     "inscriptionPeriodStart || ' to ' || inscriptionPeriodEnd, " +
                     "startDate, spots, " +
                     "(spots - (SELECT COUNT(*) FROM Inscription WHERE action_id = ? AND state = 'CONFIRMED')) as left " +
                     "FROM FormativeAction WHERE action_id = ?";
        List<Object[]> res = db.executeQueryArray(sql, actionId, actionId);
        return res.isEmpty() ? null : res.get(0);
    }

    // 3. Obtener historial de movimientos (MoneyMovement)
    public List<Object[]> getMovements(String actionId) {
        String sql = "SELECT movement_date, " +
                     "COALESCE(type, 'General') || ' - ' || status as concept, " +
                     "amount " +
                     "FROM MoneyMovement mm " +
                     "LEFT JOIN Inscription i ON mm.inscription_id = i.inscription_id " +
                     "LEFT JOIN Invoice inv ON mm.invoice_id = inv.invoice_id " +
                     "WHERE i.action_id = ? OR inv.action_id = ? " +
                     "ORDER BY movement_date DESC";
        return db.executeQueryArray(sql, actionId, actionId);
    }
    
    public List<Object[]> getReportData(String startDate, String endDate, String statusFilter) {
        // SQL adaptado a las nuevas tablas (MoneyMovement, Inscription con state, etc.)
        String sql = 
            "SELECT " +
            "  fa.startDate, fa.name, fa.status, " +
            "  (SELECT COALESCE(AVG(applied_fee), 0) FROM Inscription WHERE action_id = fa.action_id) as avgFee, " + 
            "  (SELECT COUNT(*) FROM Inscription WHERE action_id = fa.action_id AND state = 'RECEIVED') as pendingInscriptions, " +
            "  (SELECT COALESCE(SUM(mm.amount), 0) FROM MoneyMovement mm " +
            "   JOIN Inscription i ON mm.inscription_id = i.inscription_id " +
            "   WHERE i.action_id = fa.action_id AND mm.status = 'CONFIRMED') as confIncome, " + 
            "  (SELECT COALESCE(SUM(remuneration), 0) FROM Teacher_FormativeAction WHERE action_id = fa.action_id) as totalRemuneration, " + 
            "  (SELECT COALESCE(SUM(mm.amount), 0) FROM MoneyMovement mm " +
            "   JOIN Invoice inv ON mm.invoice_id = inv.invoice_id " +
            "   WHERE inv.action_id = fa.action_id AND mm.status = 'CONFIRMED') as confExpenses, " + 
            "  fa.spots, " +
            "  (SELECT COUNT(*) FROM Inscription WHERE action_id = fa.action_id AND state = 'CONFIRMED') as confirmedCount " + 
            "FROM FormativeAction fa " +
            "WHERE fa.startDate >= ? AND fa.startDate <= ? ";

        if ("Active".equals(statusFilter)) {
            sql += "AND fa.status = 'ACTIVE' ";
        } else if ("Closed".equals(statusFilter)) {
            sql += "AND fa.status IN ('CLOSED', 'CANCELLED') "; 
        }
        
        sql += "ORDER BY fa.startDate ASC";

        return db.executeQueryArray(sql, startDate, endDate);
    }
}