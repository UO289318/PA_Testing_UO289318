package g54.si26.reopenFormativeActions;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import g54.si26.utils.Database;
import g54.si26.DTOs.FormativeActionDTO;
import java.util.List;

public class ModelReopenFormativeActionTest {
    private ModelReopenFormativeAction model;
    private Database db = new Database();

    @BeforeEach
    public void setUp() {
        model = new ModelReopenFormativeAction();
        db.createDatabase(false); 
        db.loadDatabase();
        
        // Ensure we have at least one CLOSED action for testing
        db.executeUpdate("UPDATE FormativeAction SET status = 'CLOSED' WHERE action_id = 1");
    }

    @Test
    public void testGetClosedFormativeActions() {
        List<FormativeActionDTO> closedActions = model.getClosedFormativeActions();
        assertNotNull(closedActions);
        assertTrue(closedActions.size() >= 1, "Should have at least one closed action");
        assertEquals("CLOSED", closedActions.get(0).getStatus());
    }

    @Test
    public void testReopenFormativeAction() {
        // action_id 1 is CLOSED due to setUp
        boolean success = model.reopenFormativeAction(1);
        assertTrue(success, "Reopen should be successful");

        // Verify status in DB
        List<Object[]> result = db.executeQueryArray("SELECT status FROM FormativeAction WHERE action_id = 1");
        assertEquals("ACTIVE", result.get(0)[0].toString());
    }

    @Test
    public void testReopenNonExistentAction() {
        boolean success = model.reopenFormativeAction(999);
        assertFalse(success, "Reopen should fail for non-existent action");
    }
}
