package g54.si26.reopenFormativeActions;

import java.util.List;
import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.utils.Database;

public class ModelReopenFormativeAction {
    private Database db = new Database();

    public List<FormativeActionDTO> getClosedFormativeActions() {
        String sql = "SELECT action_id AS actionId, name, startDate, endDate, status " +
                     "FROM FormativeAction " +
                     "WHERE status = 'CLOSED' " +
                     "ORDER BY startDate DESC";
        return db.executeQueryPojo(FormativeActionDTO.class, sql);
    }

    public boolean reopenFormativeAction(int actionId, String simulatedDate) {
        String sql = "UPDATE FormativeAction SET status = 'ACTIVE', reopenDate = ? WHERE action_id = ?";
        return db.executeUpdate(sql, simulatedDate, actionId) > 0;
    }
}
