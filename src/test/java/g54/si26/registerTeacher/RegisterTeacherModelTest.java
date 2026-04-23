package g54.si26.registerTeacher;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import g54.si26.DTOs.TeacherDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;
import java.util.List;

public class RegisterTeacherModelTest {

    private RegisterTeacherModel model;
    private Database db = new Database();

    @BeforeEach
    public void setUp() {
        db.createDatabase(false);
        // We clean the test teacher if it exists
        db.executeUpdate("DELETE FROM Teacher WHERE fiscal_id = 'TEST001' OR email = 'test@teacher.com'");
        model = new RegisterTeacherModel();
    }

    @Test
    public void testRegisterSuccessful() {
        TeacherDTO t = new TeacherDTO();
        t.setName("Test Teacher");
        t.setFiscalId("TEST001");
        t.setEmail("test@teacher.com");
        t.setPhone("123456789");

        assertDoesNotThrow(() -> model.registerTeacher(t));

        // Verify in DB
        String sql = "SELECT COUNT(*) FROM Teacher WHERE fiscal_id = 'TEST001'";
        List<Object[]> res = db.executeQueryArray(sql);
        assertEquals(1, Integer.parseInt(res.get(0)[0].toString()));
    }

    @Test
    public void testRegisterEmptyName() {
        TeacherDTO t = new TeacherDTO();
        t.setName("   ");
        t.setFiscalId("TEST002");
        t.setEmail("test2@teacher.com");
        t.setPhone("123456789");

        ApplicationException ex = assertThrows(ApplicationException.class, () -> model.registerTeacher(t));
        assertTrue(ex.getMessage().contains("Name"));
    }

    @Test
    public void testRegisterInvalidEmail() {
        TeacherDTO t = new TeacherDTO();
        t.setName("Test Teacher");
        t.setFiscalId("TEST003");
        t.setEmail("invalid-email");
        t.setPhone("123456789");

        ApplicationException ex = assertThrows(ApplicationException.class, () -> model.registerTeacher(t));
        assertTrue(ex.getMessage().contains("format"));
    }

    @Test
    public void testMultipleErrors() {
        TeacherDTO t = new TeacherDTO();
        t.setName(""); // Error 1
        t.setFiscalId(""); // Error 2
        t.setEmail("bad-email"); // Error 3
        t.setPhone(""); // Error 4

        ApplicationException ex = assertThrows(ApplicationException.class, () -> model.registerTeacher(t));
        String msg = ex.getMessage();
        assertTrue(msg.contains("Name"));
        assertTrue(msg.contains("Fiscal ID"));
        assertTrue(msg.contains("format"));
        assertTrue(msg.contains("Phone"));
    }

    @Test
    public void testRegisterDuplicateFiscalId() {
        TeacherDTO t1 = new TeacherDTO();
        t1.setName("Teacher 1");
        t1.setFiscalId("DUP001");
        t1.setEmail("t1@test.com");
        t1.setPhone("111");
        model.registerTeacher(t1);

        TeacherDTO t2 = new TeacherDTO();
        t2.setName("Teacher 2");
        t2.setFiscalId("DUP001");
        t2.setEmail("t2@test.com");
        t2.setPhone("222");

        ApplicationException ex = assertThrows(ApplicationException.class, () -> model.registerTeacher(t2));
        assertTrue(ex.getMessage().contains("Fiscal ID already exists"));
    }
}
