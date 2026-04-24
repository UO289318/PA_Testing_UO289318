package g54.si26.moneyMovements;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import g54.si26.utils.Database;
import g54.si26.DTOs.MoneyMovementDTO;
import java.util.List;

public class VATAndTypeTest {
    private MoneyMovementModel model;
    private Database db;

    @BeforeEach
    public void setUp() {
        db = new Database();
        db.createDatabase(false);
        db.loadDatabase();
        model = new MoneyMovementModel();
    }

    @Test
    public void testRegisterMovementMissingType() {
        // Register a movement
        model.registerMovement(1, null, 100.0, "2026-04-23", "EXECUTED");
        
        // Check in database directly if type is set
        String sql = "SELECT type FROM MoneyMovement WHERE amount = 100.0 AND movement_date = '2026-04-23'";
        List<Object[]> results = db.executeQueryArray(sql);
        assertFalse(results.isEmpty());
        Object type = results.get(0)[0];
        assertNotNull(type, "Type should not be null");
        assertEquals("PAYMENT", type.toString(), "Type should be PAYMENT");
    }
}
