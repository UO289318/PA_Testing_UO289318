package g54.si26.tmConsulting;

import java.util.List;
import g54.si26.utils.Database;

public class TMConsultingModel {

    private Database db = new Database();

    public List<Object[]> getReportData(String startDate, String endDate, String statusFilter) {
        // SQL query with subqueries to group the financial data per course
    	String sql = "SELECT fa.startDate, fa.name, fa.status, fa.fee, " +
                "(SELECT COUNT(*) FROM Inscription i WHERE i.action_id = fa.action_id AND i.state = 'RECEIVED'), " +
                "(SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p INNER JOIN Inscription i ON p.inscription_id = i.inscription_id WHERE i.action_id = fa.action_id), " +
                "(SELECT COALESCE(SUM(tfa.remuneration), 0) FROM Teacher_FormativeAction tfa WHERE tfa.action_id = fa.action_id), " +
                // FÍJATE EN ESTA COMA AL FINAL DE LA LÍNEA --------v
                "(SELECT COALESCE(SUM(mm.amount), 0) FROM MoneyMovement mm INNER JOIN Invoice inv ON mm.invoice_id = inv.invoice_id WHERE inv.action_id = fa.action_id), " +
                
                "fa.spots " + // Y aquí ya no hace falta coma porque es el último
                
                "FROM FormativeAction fa " +
                "WHERE fa.startDate >= ? AND fa.startDate <= ? ";

        // Apply the status filter if it's not "All"
        if ("Active".equals(statusFilter)) {
            sql += "AND fa.status = 'ACTIVE' ";
        } else if ("Closed".equals(statusFilter)) {
            // We assume that closed statuses can be CLOSED or CANCELLED
            sql += "AND fa.status IN ('CLOSED', 'CANCELLED') "; 
        }
        
        sql += "ORDER BY fa.startDate ASC";

        return db.executeQueryArray(sql, startDate, endDate);
    }
}