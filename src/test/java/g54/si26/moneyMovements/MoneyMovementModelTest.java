package g54.si26.moneyMovements;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import g54.si26.utils.Database;
import java.util.List;
import g54.si26.DTOs.EnrollmentRecordDTO;

public class MoneyMovementModelTest {
    private MoneyMovementModel model;
    private Database db;

    @Before
    public void setUp() {
        db = new Database();
        db.createDatabase(false);
        db.loadDatabase();
        model = new MoneyMovementModel();
    }

    @Test
    public void testRegisterPaymentAndStatusChange() {
        // Find an enrollment in RECEIVED state
        List<EnrollmentRecordDTO> enrollments = model.getAllEnrollments();
        EnrollmentRecordDTO target = null;
        for (EnrollmentRecordDTO e : enrollments) {
            if ("RECEIVED".equals(e.getState())) {
                target = e;
                break;
            }
        }
        assertNotNull("Should find a RECEIVED enrollment", target);
        
        // Register payment equal to fee
        model.registerMovement(target.getInscriptionId(), null, target.getFee(), "2026-03-25", "EXECUTED");
        
        // Check if status changed to CONFIRMED
        List<EnrollmentRecordDTO> updated = model.getAllEnrollments();
        boolean found = false;
        for (EnrollmentRecordDTO e : updated) {
            if (e.getInscriptionId() == target.getInscriptionId()) {
                assertEquals("CONFIRMED", e.getState());
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testCompensationTrigger() {
        // Find an enrollment
        List<EnrollmentRecordDTO> enrollments = model.getAllEnrollments();
        EnrollmentRecordDTO target = enrollments.get(0);
        
        // Register an overpayment
        model.registerMovement(target.getInscriptionId(), null, target.getFee() + 100, "2026-03-25", "EXECUTED");
        
        // Check state
        List<EnrollmentRecordDTO> updated = model.getAllEnrollments();
        for (EnrollmentRecordDTO e : updated) {
            if (e.getInscriptionId() == target.getInscriptionId()) {
                assertEquals("PENDING_COMPENSATION", e.getState());
            }
        }
    }
}
