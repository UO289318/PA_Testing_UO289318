package g54.si26.tmConsulting;

import java.util.List;
import g54.si26.utils.BaseModel;
import g54.si26.utils.Database;

public class TMConsultingModel extends BaseModel {

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

    public double getConfirmedIncome(int actionId, String simDate) {
        String safeDate = (simDate != null && !simDate.isBlank()) ? simDate.substring(0, 10) : "9999-12-31";
        // Only count income for inscriptions that are NOT cancelled
        String sql = "SELECT COALESCE(SUM(mm.amount), 0.0) " +
                     "FROM MoneyMovement mm " +
                     "JOIN Inscription i ON mm.inscription_id = i.inscription_id " +
                     "WHERE i.action_id = ? AND i.state != 'CANCELLED' AND mm.status = 'EXECUTED' AND date(mm.movement_date) <= date(?)";
        return (double) db.executeQueryArray(sql, actionId, safeDate).get(0)[0];
    }

    public double getConfirmedExpenses(int actionId, String simDate) {
        String safeDate = (simDate != null && !simDate.isBlank()) ? simDate.substring(0, 10) : "9999-12-31";
        // Use net sum (sum of negative movements and positive refunds) and then absolute value
        String sql = "SELECT ABS(COALESCE(SUM(mm.amount), 0.0)) " +
                     "FROM MoneyMovement mm " +
                     "JOIN Invoice inv ON mm.invoice_id = inv.invoice_id " +
                     "WHERE inv.action_id = ? AND mm.status = 'EXECUTED' AND date(mm.movement_date) <= date(?)";
        return (double) db.executeQueryArray(sql, actionId, safeDate).get(0)[0];
    }

    public double getExpectedIncome(int actionId, String simDate) {
        String safeDate = (simDate != null && !simDate.isBlank()) ? simDate.substring(0, 10) : "9999-12-31";
        String sql = "SELECT COALESCE(SUM(applied_fee), 0.0) " +
                     "FROM Inscription WHERE action_id = ? AND state != 'CANCELLED' AND date(inscription_date) <= date(?)";
        return (double) db.executeQueryArray(sql, actionId, safeDate).get(0)[0];
    }

    public double getTotalRemuneration(int actionId) {
        String sql = "SELECT COALESCE(SUM(remuneration), 0.0) " +
                     "FROM Teacher_FormativeAction " +
                     "WHERE action_id = ?";
        return (double) db.executeQueryArray(sql, actionId).get(0)[0];
    }

    public double getAvgFee(int actionId) {
        String sql = "SELECT COALESCE(AVG(applied_fee), 0.0) FROM Inscription WHERE action_id = ?";
        return (double) db.executeQueryArray(sql, actionId).get(0)[0];
    }

    public int getPendingInscriptionsCount(int actionId, String simDate) {
        String safeDate = (simDate != null && !simDate.isBlank()) ? simDate.substring(0, 10) : "9999-12-31";
        String sql = "SELECT COUNT(*) FROM Inscription WHERE action_id = ? AND state = 'RECEIVED' AND date(inscription_date) <= date(?)";
        return Integer.parseInt(db.executeQueryArray(sql, actionId, safeDate).get(0)[0].toString());
    }

    public int getConfirmedInscriptionsCount(int actionId, String simDate) {
        String safeDate = (simDate != null && !simDate.isBlank()) ? simDate.substring(0, 10) : "9999-12-31";
        String sql = "SELECT COUNT(*) FROM Inscription WHERE action_id = ? AND state = 'CONFIRMED' AND date(inscription_date) <= date(?)";
        return Integer.parseInt(db.executeQueryArray(sql, actionId, safeDate).get(0)[0].toString());
    }

    public double calculateEstimatedIncome(int totalSpots, int confirmedCount, int pendingEnrollments, double avgFee, double confIncome) {
        int freeSpots = Math.max(0, totalSpots - confirmedCount);
        int expectedNewEnrollments = Math.min(pendingEnrollments, freeSpots);
        
        // Income already confirmed plus what we expect from pending ones
        double expectedFromConfirmed = Math.max(confIncome, confirmedCount * avgFee);
        return expectedFromConfirmed + (expectedNewEnrollments * avgFee);
    }

    public double calculateEstimatedExpenses(double confExpenses, double totalRemuneration) {
        // At least we will pay the remuneration
        return Math.max(confExpenses, totalRemuneration);
    }
    
    public List<Object[]> getReportData(String startDate, String endDate, String statusFilter, String simDate) {
    		String statusSql = getTemporalFaStatusSql(endDate, "fa");
    		// SQL adaptado a las nuevas tablas (MoneyMovement, Inscription con state, etc.)
        String sql = 
        		"SELECT * FROM (" +
            "SELECT " +
            "  fa.startDate, fa.name, " +
            "    (" + statusSql + ") AS status, " +
            "  (SELECT COALESCE(AVG(applied_fee), 0) FROM Inscription WHERE action_id = fa.action_id) as avgFee, " + 
            "  (SELECT COUNT(*) FROM Inscription WHERE action_id = fa.action_id AND state = 'RECEIVED') as pendingInscriptions, " +
            "  (SELECT COALESCE(SUM(mm.amount), 0) FROM MoneyMovement mm " +
            "   JOIN Inscription i ON mm.inscription_id = i.inscription_id " +
            "   WHERE i.action_id = fa.action_id AND i.state != 'CANCELLED') as confIncome, " + 
            "  (SELECT COALESCE(SUM(remuneration), 0) FROM Teacher_FormativeAction WHERE action_id = fa.action_id) as totalRemuneration, " + 
            "  (SELECT COALESCE(ABS(SUM(mm.amount)), 0) FROM MoneyMovement mm " +
            "   JOIN Invoice inv ON mm.invoice_id = inv.invoice_id " +
            "   WHERE inv.action_id = fa.action_id) as confExpenses, " + 
            "  fa.spots, " +
            "  (SELECT COUNT(*) FROM Inscription WHERE action_id = fa.action_id AND state = 'CONFIRMED') as confirmedCount, " +
            "  (fa.startDate || ' to ' || fa.endDate) AS dateRange " +
            "FROM FormativeAction fa " +
            "WHERE fa.startDate >= ? AND fa.startDate <= ? " +
            ") WHERE 1=1 ";

        if (statusFilter!=null && !statusFilter.equals("ALL") && !statusFilter.equals("ACTIVE (Default)")) 
            sql+="AND status = '" + statusFilter + "' ";
        else if ("ACTIVE (Default)".equals(statusFilter)) 
            sql += "AND status NOT IN ('CLOSED') "; 
        
        
        sql += "ORDER BY startDate ASC";

        return db.executeQueryArray(sql, startDate, endDate);
    }
}