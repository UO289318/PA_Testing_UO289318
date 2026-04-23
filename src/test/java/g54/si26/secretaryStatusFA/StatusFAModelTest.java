package g54.si26.secretaryStatusFA;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import g54.si26.utils.Database;
import g54.si26.secretaryStatusFA.dto.FAStatusDTO;
import java.util.List;

public class StatusFAModelTest {

    private StatusFAModel model;
    private Database db = new Database();

    @BeforeEach
    public void setUp() {
        db.createDatabase(false);
        db.loadDatabase();
        model = new StatusFAModel();
    }

    @Test
    public void testGetFormativeActions() {
        List<FAStatusDTO> actions = model.getFormativeActions("2023-01-01");
        assertNotNull(actions);
        assertFalse(actions.isEmpty());
    }

    @Test
    public void testGetFADetail() {
        // Assuming action_id 1 exists in initial data
        FAStatusDTO detail = model.getFADetail(1, "9999-12-31");
        if (detail != null) {
            assertEquals(1, detail.getActionId());
            // Check that financials are at least 0 (not negative or null)
            assertTrue(detail.getConfirmedIncome() >= 0);
            assertTrue(detail.getConfirmedExpenses() >= 0);
            // Total remuneration - confirmed expenses = estimated
            assertEquals(detail.getTotalRemuneration() - detail.getConfirmedExpenses(), detail.getEstimatedExpenses(), 0.01);
        }
    }

    @Test
    public void testGetFARegistrations() {
        // Assuming action_id 1 exists
        var regs = model.getFARegistrations(1, "9999-12-31");
        assertNotNull(regs);
    }
}
