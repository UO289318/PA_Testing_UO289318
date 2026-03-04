package g54.si26.dao;

import g54.si26.utils.DbUtil;
import g54.si26.utils.Database;

import java.util.List;
import java.util.Map;

public class InvoiceDAO extends DbUtil {

    @Override
    public String getUrl() {
        return new Database().getUrl();
    }

    public void createInvoice(
            String date,
            double net,
            double vat,
            double total,
            int teacherId,
            int actionId
    ) {

        String sql = """
            INSERT INTO "Invoice"
            (invoice_date, netAmount, vat, totalAmount, teacher_id, action_id)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        executeUpdate(sql, date, net, vat, total, teacherId, actionId);
    }

    public double getInitialPayment(int actionId) {
        String sql = "SELECT initialPayment FROM \"FormativeAction\" WHERE action_id = ?";
        List<Map<String, Object>> result = executeQueryMap(sql, actionId);
        return ((Number) result.get(0).get("initialPayment")).doubleValue();
    }
}
