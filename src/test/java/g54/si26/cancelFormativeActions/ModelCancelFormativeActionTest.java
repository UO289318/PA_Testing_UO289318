package g54.si26.cancelFormativeActions;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.utils.Database;

public class ModelCancelFormativeActionTest {

    private ModelCancelFormativeAction model;
    private Database db = new Database();
    
    @BeforeEach
    public void setUp() {
        db.createDatabase(false);
        db.loadDatabase();
        model = new ModelCancelFormativeAction();
    }

    @Test
    public void testGetCancelableActions() {
        Date simulatedDate = getCustomDate(2026, Calendar.APRIL, 1);
        List<FormativeActionDTO> actions = model.getCancelableActions(simulatedDate);
        
        // Action 2 should be in the list
        assertTrue(actions.stream().anyMatch(a -> a.getActionId() == 2));
    }

    @Test
    public void testGetTeachersForAction() {
        List<Object[]> teachers = model.getTeachersWithRemuneration(2);
        // Action 2 has teachers 1 and 2
        assertEquals(2, teachers.size());
        assertTrue(teachers.stream().anyMatch(t -> (int)t[0] == 1));
        assertTrue(teachers.stream().anyMatch(t -> (int)t[0] == 2));
    }

    @Test
    public void testCancelActionFinancials() {
        int actionId = 2;
        double completionPct = 50.0;
        Map<Integer, Double> teacherPcts = new HashMap<>();
        teacherPcts.put(1, 50.0);
        teacherPcts.put(2, 50.0);
        Date simDate = getCustomDate(2026, Calendar.MARCH, 26);

        model.cancelAction(actionId, completionPct, teacherPcts, simDate);

        // Verify FormativeAction status
        String status = (String) db.executeQueryArray("SELECT status FROM FormativeAction WHERE action_id = ?", actionId).get(0)[0];
        assertEquals("CANCELLED", status);

        // Verify Inscriptions
        // Inscription 3: Original fee from Fee table for Action 2/Comm 3 is 300. New fee = 150.
        Object[] ins3 = db.executeQueryArray("SELECT applied_fee, state FROM Inscription WHERE inscription_id = 3").get(0);
        assertEquals(150.0, (Double) ins3[0], 0.001);
        assertEquals("PENDING_COMPENSATION", ins3[1]);

        // Inscription 5: Original fee is 150. New fee = 75. Paid 0.
        Object[] ins5 = db.executeQueryArray("SELECT applied_fee, state FROM Inscription WHERE inscription_id = 5").get(0);
        assertEquals(75.0, (Double) ins5[0], 0.001);
        assertEquals("CANCELLED", ins5[1]);

        // Verify Teachers Remuneration (Action 2: T1=100, T2=200 in data.sql)
        Double rem1 = (Double) db.executeQueryArray("SELECT remuneration FROM Teacher_FormativeAction WHERE action_id = 2 AND teacher_id = 1").get(0)[0];
        assertEquals(50.0, rem1, 0.001);
        
        Double rem2 = (Double) db.executeQueryArray("SELECT remuneration FROM Teacher_FormativeAction WHERE action_id = 2 AND teacher_id = 2").get(0)[0];
        assertEquals(100.0, rem2, 0.001);

        // Verify Invoice creation/update
        Double invAmount = (Double) db.executeQueryArray("SELECT totalAmount FROM Invoice WHERE action_id = 2 AND teacher_id = 1").get(0)[0];
        assertEquals(50.0, invAmount, 0.001);
    }

    private Date getCustomDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return cal.getTime();
    }
}
