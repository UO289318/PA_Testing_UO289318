package g54.si26.inscriptions.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.DTOs.ProfessionalDTO;
import g54.si26.inscriptions.InscriptionsModel;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;
import g54.si26.utils.Util;

public class RobustnessTest {

    private Database db;
    private InscriptionsModel model;

    @BeforeEach
    public void setUp() {
        db = new Database();
        model = new InscriptionsModel();
        
        // Limpiamos escenarios de prueba por seguridad
        db.executeUpdate("DELETE FROM Inscription WHERE action_id >= 100");
        db.executeUpdate("DELETE FROM FormativeAction WHERE action_id >= 100");
        
        // Nos aseguramos de que haya al menos un profesor para cumplir la constraint (teacher_id = 99)
        db.executeUpdate("DELETE FROM Teacher WHERE teacher_id = 99");
        db.executeUpdate("INSERT INTO Teacher (teacher_id, name, fiscal_id, email, phone) VALUES (99, 'Test Teacher', '00000000T', 'teacher@test.com', '600000000')");
    }

    // --- MÉTODO AUXILIAR PARA EVITAR EL ERROR "NOT NULL CONSTRAINT FAILED" ---
    private void insertDummyCourse(int id, String name, int spots, String status) {
        String sql = "INSERT INTO FormativeAction (action_id, name, objectives, mainContents, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, fee, status, initialPayment, teacher_id) "
                   + "VALUES (?, ?, 'Obj', 'Cont', ?, '2026-05-01', '2026-05-15', '20', '2025-01-01', '2026-12-31', 'Online', 100.0, ?, 50.0, 99)";
        db.executeUpdate(sql, id, name, spots, status);
    }

    @Test
    public void S1_testExtremeConcurrencyOverbooking() throws InterruptedException {
        // Usamos el método auxiliar seguro
        insertDummyCourse(101, "Stress Test", 1, "ACTIVE");

        int numThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latchReady = new CountDownLatch(1);
        CountDownLatch latchDone = new CountDownLatch(numThreads);
        AtomicInteger successfulEnrollments = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    latchReady.await(); // Esperan todos en la línea de salida
                    ProfessionalDTO p = new ProfessionalDTO();
                    p.setName("ThreadUser" + userId);
                    p.setSurname("Test");
                    p.setPhone("60000000" + userId);
                    p.setEmail("user" + userId + "@stress.com");

                    model.enrollProfessional(p, 101, new Date());
                    successfulEnrollments.incrementAndGet();
                } catch (ApplicationException e) {
                    // Excepción esperada por falta de plazas o por base de datos bloqueada temporalmente
                } catch (Exception e) {
                    // Ignoramos errores de SQLite Locked, nos importa el recuento final
                } finally {
                    latchDone.countDown();
                }
            });
        }

        latchReady.countDown(); // ¡Disparo de salida!
        latchDone.await();      // Esperamos a que terminen los 5
        executor.shutdown();

        // Verificación BD
        String sql = "SELECT COUNT(*) FROM Inscription WHERE action_id = 101 AND state = 'RECEIVED'";
        int totalEnrolled = Integer.parseInt(db.executeQueryArray(sql).get(0)[0].toString());

        assertEquals(1, successfulEnrollments.get(), "Solo 1 hilo debería haber tenido éxito");
        assertEquals(1, totalEnrolled, "La base de datos debe contener exactamente 1 inscripción");
    }

    @Test
    public void S2_testFutureParadoxCorrection() {
        insertDummyCourse(102, "Time Paradox", 10, "ACTIVE");
        
        ProfessionalDTO marty = new ProfessionalDTO();
        marty.setName("Marty");
        marty.setSurname("McFly");
        marty.setPhone("5551234");
        marty.setEmail("marty@time.com");

        Date day10 = Util.isoStringToDate("2026-03-10 12:00:00");
        Date day09 = Util.isoStringToDate("2026-03-09 12:00:00");

        // 1. Matricular en el futuro
        model.enrollProfessional(marty, 102, day10);
        
        // 2. Viajar al pasado y matricular de nuevo
        assertDoesNotThrow(() -> model.enrollProfessional(marty, 102, day09), "Debería permitir reescribir la historia");

        // 3. Verificamos que solo existe la del pasado (la del futuro fue purgada)
        String sql = "SELECT COUNT(*) FROM Inscription WHERE action_id = 102 AND professional_id = "
                   + "(SELECT professional_id FROM Professional WHERE email = 'marty@time.com')";
        int totalRecords = Integer.parseInt(db.executeQueryArray(sql).get(0)[0].toString());

        assertEquals(1, totalRecords, "La inscripción del futuro debería haber sido eliminada por clearFutureParadox");
    }

    @Test
    public void S3_testZombieHistoricalReEnrollment() {
        insertDummyCourse(103, "Zombie Recovery", 5, "ACTIVE");
        
        // Insertamos profesional seguro
        db.executeUpdate("DELETE FROM Professional WHERE email = 'zombie@test.com'");
        db.executeUpdate("INSERT INTO Professional (professional_id, name, surname, phone, email) "
                       + "VALUES (888, 'Zombie', 'User', '666', 'zombie@test.com')"); 
        
        // CORRECCIÓN: Añadimos la columna 'fee' que es obligatoria (NOT NULL)
        db.executeUpdate("INSERT INTO Inscription (inscription_date, fee, state, professional_id, action_id) "
                       + "VALUES ('2025-01-01 10:00:00', 100.0, 'CANCELLED', 888, 103)");

        ProfessionalDTO zombie = new ProfessionalDTO();
        zombie.setName("Zombie");
        zombie.setSurname("User");
        zombie.setPhone("666");
        zombie.setEmail("zombie@test.com");

        assertDoesNotThrow(() -> model.enrollProfessional(zombie, 103, new Date()), 
                           "El sistema NO debe bloquear inscripciones si las previas están en estado CANCELLED");
    }

    @Test
    public void S4_testWorkingDays48hRule() {
        insertDummyCourse(104, "Weekend Bypass", 10, "ACTIVE");
        
        db.executeUpdate("DELETE FROM Professional WHERE email = 'weekend@test.com'");
        db.executeUpdate("INSERT INTO Professional (professional_id, name, surname, phone, email) "
                       + "VALUES (999, 'Weekend', 'Tester', '777', 'weekend@test.com')"); 
        
        // CORRECCIÓN: Añadimos la columna 'fee' que es obligatoria (NOT NULL)
        db.executeUpdate("INSERT INTO Inscription (inscription_date, fee, state, professional_id, action_id) "
                       + "VALUES ('2026-02-26 18:00:00', 100.0, 'RECEIVED', 999, 104)");

        // Simulamos LUNES 10:00 (Fin de semana ignorado, pocas horas laborables reales)
        Date mondayMorning = Util.isoStringToDate("2026-03-02 10:00:00");
        model.checkAndReleaseExpiredBookings(mondayMorning);
        
        String sqlState = "SELECT state FROM Inscription WHERE action_id = 104 AND professional_id = 999";
        assertEquals("RECEIVED", db.executeQueryArray(sqlState).get(0)[0].toString(), "El Lunes aún debe ser válido (ignora fin de semana)");

        // Simulamos MARTES 19:00 (49h laborables reales)
        Date tuesdayEvening = Util.isoStringToDate("2026-03-03 19:00:00");
        model.checkAndReleaseExpiredBookings(tuesdayEvening);
        
        assertEquals("CANCELLED", db.executeQueryArray(sqlState).get(0)[0].toString(), "El Martes la inscripción debe haber caducado");
    }
    @Test
    public void S5_testHardConstraintsBypass() {
        insertDummyCourse(105, "Closed Course", 10, "CLOSED");

        ProfessionalDTO hacker = new ProfessionalDTO();
        hacker.setName("Hacker");
        hacker.setSurname("Bypass");
        hacker.setPhone("000");
        hacker.setEmail("hacker@test.com");

        // 1. Bypass status CLOSED
        ApplicationException e1 = assertThrows(ApplicationException.class, () -> {
            model.enrollProfessional(hacker, 105, new Date());
        });
        assertTrue(e1.getMessage().contains("Security Error"), "El modelo debe rechazar cursos CLOSED");
    }
}