package g54.si26.MultipleFeesTests;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import g54.si26.DTOs.ProfessionalDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;
import g54.si26.inscriptions.*;


public class TestMultipleFeesInscriptions {

    private static Database db = new Database();

    @BeforeEach
    public void setUp() {
        db.createDatabase(true);
        loadCleanDatabase(db);
    }

    //Loads setup
    public static void loadCleanDatabase(Database db) {
        db.executeBatch(new String[] {
            "DELETE FROM MoneyMovement",
            "DELETE FROM Invoice",
            "DELETE FROM Inscription",
            "DELETE FROM Teacher_FormativeAction",
            "DELETE FROM Fee",
            "DELETE FROM FormativeAction",
            "DELETE FROM Professional",
            "DELETE FROM Community",
            "DELETE FROM Teacher",
            "INSERT INTO Community (community_id, communityName) VALUES (1, 'General Public')",
            "INSERT INTO Community (community_id, communityName) VALUES (2, 'Unemployed')",
            "INSERT INTO FormativeAction (action_id, name, objectives, mainContents, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, status, creationDate) " +
            "VALUES (100, 'Test Automation Course', 'Aprender testing', 'JUnit y Selenium', 2, '2026-10-01', '2026-10-05', '20', '2026-01-01', '2026-09-30', 'Online', 'ACTIVE', '2025-12-31')",
            "INSERT INTO Fee (amount, community_id, action_id) VALUES (100.0, 1, 100)",
            "INSERT INTO Fee (amount, community_id, action_id) VALUES (0.0, 2, 100)"
        });
    }


    //Enrolment with fee>0€, should be saved as RECEIVED
    @Test
    public void testEnrollProfessional_PaidFee_SavesAsReceived() {
        InscriptionsModel model = new InscriptionsModel();
        
        ProfessionalDTO prof = new ProfessionalDTO();
        prof.setName("Alice"); prof.setSurname("García"); 
        prof.setPhone("611222333"); prof.setEmail("alice@test.com");

        Date simulatedDate = parseDate("2026-02-15 10:00:00");
        model.enrollProfessional(prof, 100, 1, simulatedDate);

        List<Object[]> insc = db.executeQueryArray("SELECT state, applied_fee FROM Inscription WHERE action_id = 100");
        assertEquals(1, insc.size(), "Debería haberse creado 1 matrícula");
        assertEquals("RECEIVED", insc.get(0)[0].toString(), "Si hay que pagar, el estado debe ser RECEIVED");
        assertEquals("100.0", insc.get(0)[1].toString(), "La tasa aplicada debe ser 100.0");
    }

    //Enrolment with Fee 0€, should be saved as CONFIRMED
    @Test
    public void testEnrollProfessional_FreeFee_SavesAsConfirmed() {
        InscriptionsModel model = new InscriptionsModel();
        
        ProfessionalDTO prof = new ProfessionalDTO();
        prof.setName("Bob"); prof.setSurname("Martínez"); 
        prof.setPhone("644555666"); prof.setEmail("bob@test.com");

        Date simulatedDate = parseDate("2026-02-15 10:30:00");
        model.enrollProfessional(prof, 100, 2, simulatedDate);

        List<Object[]> insc = db.executeQueryArray("SELECT state, applied_fee FROM Inscription WHERE action_id = 100");
        assertEquals(1, insc.size(), "Debería haberse creado 1 matrícula");
        assertEquals("CONFIRMED", insc.get(0)[0].toString(), "Si la tarifa es 0, el estado DEBE ser CONFIRMED automáticamente");
        assertEquals("0.0", insc.get(0)[1].toString(), "La tasa aplicada debe ser 0.0");
    }

    //Full FA and duplicates Logic
    @Test
    public void testEnrollmentValidations() {
        InscriptionsModel model = new InscriptionsModel();
        Date simulatedDate = parseDate("2026-02-15 10:00:00");
        
        ProfessionalDTO p1 = new ProfessionalDTO();
        p1.setName("User1"); p1.setSurname("A"); p1.setPhone("111"); p1.setEmail("u1@test.com");
        model.enrollProfessional(p1, 100, 1, simulatedDate);
        ApplicationException dupEx = assertThrows(ApplicationException.class, () -> {
            model.enrollProfessional(p1, 100, 1, simulatedDate); 
        });
        assertEquals("Already enrolled.", dupEx.getMessage());

        ProfessionalDTO p2 = new ProfessionalDTO();
        p2.setName("User2"); p2.setSurname("B"); p2.setPhone("222"); p2.setEmail("u2@test.com");
        model.enrollProfessional(p2, 100, 1, simulatedDate);

        ProfessionalDTO p3 = new ProfessionalDTO();
        p3.setName("User3"); p3.setSurname("C"); p3.setPhone("333"); p3.setEmail("u3@test.com");
        ApplicationException fullEx = assertThrows(ApplicationException.class, () -> {
            model.enrollProfessional(p3, 100, 1, simulatedDate); 
        });
        assertEquals("Course is full.", fullEx.getMessage());
    }

    //Automatic cancellation after 48 working hrs
    @Test
    public void testCheckAndReleaseExpiredBookings() {
        InscriptionsModel model = new InscriptionsModel();
        
        db.executeUpdate("INSERT INTO Professional (professional_id, name, surname, phone, email, community_id) VALUES (10, 'Charlie', 'Brown', '999', 'charlie@test.com', 1)");
        db.executeUpdate("INSERT INTO Inscription (inscription_id, inscription_date, applied_fee, state, professional_id, action_id) " +
                         "VALUES (500, '2026-02-02 10:00:00', 100.0, 'RECEIVED', 10, 100)"); 

        Date wednesday = parseDate("2026-02-04 11:00:00");
        model.checkAndReleaseExpiredBookings(wednesday);

        List<Object[]> res = db.executeQueryArray("SELECT state FROM Inscription WHERE inscription_id = 500");
        assertEquals("CANCELLED", res.get(0)[0].toString(), "Pasadas 48h sin pago en MoneyMovement, el estado debe ser CANCELLED");
    }

    // Helper method
    private Date parseDate(String dateStr) {
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
        } catch (Exception e) {
            return new Date();
        }
    }
}