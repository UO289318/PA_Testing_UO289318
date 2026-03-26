package g54.si26.tmConsulting;

import java.util.List;
import g54.si26.utils.Database;

public class TMConsultingModel {

    private Database db = new Database();

    public List<Object[]> getReportData(String startDate, String endDate, String statusFilter) {
        String sql = "SELECT fa.startDate, fa.name, fa.status, " +
                     // Recuperar fee de la tabla Fee (primer valor disponible para el curso)
                     "(SELECT amount FROM Fee WHERE action_id = fa.action_id LIMIT 1) AS fee, " +
                     // Inscriptions en estado RECEIVED
                     "(SELECT COUNT(*) FROM Inscription i WHERE i.action_id = fa.action_id AND i.state = 'RECEIVED') AS pending_enrollments, " +
                     // Ingresos Confirmados (MoneyMovement de tipo PAYMENT vinculado a Inscription directamente)
                     "(SELECT COALESCE(SUM(mm.amount), 0) FROM MoneyMovement mm JOIN Inscription i ON mm.inscription_id = i.inscription_id WHERE i.action_id = fa.action_id AND mm.type = 'PAYMENT') AS conf_income, " +
                     // Gastos Potenciales (Remuneración total de profesores asignados)
                     "(SELECT COALESCE(SUM(tfa.remuneration), 0) FROM Teacher_FormativeAction tfa WHERE tfa.action_id = fa.action_id) AS total_expenses, " +
                     // Gastos Confirmados (MoneyMovement de tipo PAYMENT vinculado a Invoice directamente. Usamos ABS para asegurar que sume en positivo)
                     "(SELECT COALESCE(SUM(ABS(mm.amount)), 0) FROM MoneyMovement mm JOIN Invoice inv ON mm.invoice_id = inv.invoice_id WHERE inv.action_id = fa.action_id AND mm.type = 'PAYMENT') AS conf_expenses " +
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