package g54.si26.inscriptions.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import g54.si26.DTOs.ProfessionalDTO;
import g54.si26.inscriptions.InscriptionsController;
import g54.si26.inscriptions.InscriptionsModel;
import g54.si26.inscriptions.InscriptionsView;
import g54.si26.utils.Database;

/**
 * Suite de Pruebas Destructivas (Chaos Testing).
 * Objetivo: Provocar NullPointerExceptions, violaciones de integridad,
 * fallos de concurrencia profunda y NumberFormatExceptions en el MVC.
 */
public class ExceptionsTest {

    private Database db;
    private InscriptionsModel model;

    @BeforeEach
    public void setUp() {
        db = new Database();
        model = new InscriptionsModel();
        
        // Limpieza de campo de batalla
        db.executeUpdate("DELETE FROM Inscription WHERE action_id >= 900");
        db.executeUpdate("DELETE FROM FormativeAction WHERE action_id >= 900");
        db.executeUpdate("DELETE FROM Professional WHERE email LIKE '%attack%'");
        db.executeUpdate("INSERT INTO Teacher (teacher_id, name, fiscal_id, email, phone) "
                       + "VALUES (99, 'Test Teacher', '00000000T', 'teacher@test.com', '600000000') ON CONFLICT DO NOTHING");
    }

    private void insertDummyCourse(int id, int spots) {
        String sql = "INSERT INTO FormativeAction (action_id, name, objectives, mainContents, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, fee, status, initialPayment, teacher_id) "
                   + "VALUES (?, 'Target Course', 'Obj', 'Cont', ?, '2026-05-01', '2026-05-15', '20', '2025-01-01', '2026-12-31', 'Online', 100.0, 'ACTIVE', 50.0, 99)";
        db.executeUpdate(sql, id, spots);
    }

    /**
     * TEST 1: Ataque de NullPointerException de Raíz (NPE)
     * * Objetivo: Romper el sistema pasando un DTO nulo.
     * Fallo: El método validateNotNull(profesional.getName(), ...) intenta llamar a .getName() 
     * sobre un objeto nulo ANTES de que la validación ocurra.
     * Riesgo: Caída fulminante del hilo de ejecución (Crash).
     */
    @Test
    public void T1_testNullProfessionalDtoCrash() {
        insertDummyCourse(901, 10);
        
        assertThrows(NullPointerException.class, () -> {
        //    model.enrollProfessional(null, 901, new Date());
        }, "El sistema debería lanzar NullPointerException por no comprobar si el DTO es null antes de leer sus propiedades");
    }

    /**
     * TEST 2: Ataque NullPointerException en Máquina del Tiempo
     * * Objetivo: Romper el cálculo de fechas de corte.
     * Fallo: checkAndReleaseExpiredBookings llama a calculateCutoffDate, el cual hace 
     * simulatedDate.toInstant(). Si simulatedDate es null, explota con NPE.
     * Riesgo: El refresco de la tabla fallará estrepitosamente, dejando la UI en blanco.
     */
    @Test
    public void T2_testNullDateTimeTravelCrash() {
        assertThrows(NullPointerException.class, () -> {
            model.checkAndReleaseExpiredBookings(null);
        }, "Falta un validateNotNull(simulatedDate) en los métodos de viaje en el tiempo");
    }

    /**
     * TEST 3: Condición de Carrera de Instancias Múltiples (Race Condition DB)
     * * Objetivo: Bypassear el 'synchronized' instanciando múltiples modelos (como ocurriría en un servidor real).
     * Fallo: getOrCreateProfessional comprueba si el email existe (SELECT) y luego inserta (INSERT). 
     * Al haber múltiples instancias, dos hilos leen a la vez que NO existe, e intentan hacer INSERT a la vez.
     * Resultado: Excepción fatal de la base de datos (UNIQUE CONSTRAINT FAILED: Professional.email).
     * Riesgo: Corrupción de base de datos o denegación de servicio (DoS) por saturación de errores SQL.
     */
    @Test
    public void T3_testMultiInstanceRaceConditionOnCreation() throws InterruptedException {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicInteger exceptionsCaught = new AtomicInteger(0);

        insertDummyCourse(902, 100);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    // Cada hilo representa a un usuario web con su propio modelo
                    InscriptionsModel isolatedModel = new InscriptionsModel();
                    ProfessionalDTO attackUser = new ProfessionalDTO();
                    attackUser.setName("Clon");
                    attackUser.setSurname("Attack");
                    attackUser.setPhone("123");
                    attackUser.setEmail("race_attack@test.com"); // Mismo email para todos

                    startLatch.await(); // Sincronización perfecta para choque frontal
                    //isolatedModel.enrollProfessional(attackUser, 902, new Date());
                } catch (Exception e) {
                    // Esperamos que SQLite explote con org.sqlite.SQLiteException: [SQLITE_CONSTRAINT_UNIQUE]
                    exceptionsCaught.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();

        assertTrue(exceptionsCaught.get() > 0, 
            "Múltiples instancias del modelo provocaron un choque de INSERTs simultáneos. " +
            "El synchronized del método no protege la base de datos a nivel global.");
    }

    /**
     * TEST 4: Ataque de Inyección / Datos Extremadamente Largos
     * * Objetivo: Insertar datos que excedan los límites lógicos de una BD relacional o rompan el formato.
     * Fallo: El sistema confía ciegamente en la longitud del texto provisto por el usuario.
     * Riesgo: Excepciones SQL o truncado silencioso de datos, pérdida de información.
     */
    @Test
    public void T4_testExtremeDataLengthAndMaliciousChars() {
        insertDummyCourse(903, 10);
        
        ProfessionalDTO maliciousUser = new ProfessionalDTO();
        // Generamos un nombre de 50.000 caracteres
        String massiveString = new String(new char[50000]).replace('\0', 'A');
        
        maliciousUser.setName(massiveString);
        maliciousUser.setSurname("'; DROP TABLE Inscription; --");
        maliciousUser.setPhone("Ocurrió un error");
        maliciousUser.setEmail("malicious_attack@test.com");

        // Dependiendo de la configuración de SQLite, esto podría ser tragado o podría explotar.
        // Si se lo traga, la base de datos se inflará absurdamente (Vulnerabilidad de saturación de disco).
        //assertDoesNotThrow(() -> model.enrollProfessional(maliciousUser, 903, new Date()),
          //  "El sistema ha permitido la inserción de 50,000 caracteres y caracteres maliciosos. Falta validación de longitud/sanitización.");
    }

    /**
     * TEST 5: Ataque de Estado Alterado en el Controlador (NumberFormatException)
     * * Objetivo: Simular qué ocurre si la tabla de la vista no tiene los datos esperados 
     * (por un bug visual o una inyección de memoria) al pulsar el botón "Enroll".
     * Fallo: El Controlador asume que la columna 0 es un Integer válido y la columna 4 existe. 
     * Integer.parseInt(selectedKey) explotará.
     * Riesgo: La interfaz gráfica del usuario (GUI) se congela o cierra repentinamente sin guardar datos.
     */
    @Test
    public void T5_testControllerMalformedTableCrash() {
        InscriptionsView view = new InscriptionsView();
        InscriptionsController controller = new InscriptionsController(model, view);
        
        // Falsificamos la selección en la vista
        view.getTablaCursos().setModel(new javax.swing.table.DefaultTableModel(
            new Object[][] {{"SOY_UN_TEXTO", "Curso Falso", 10, 10, "100.0", "Periodo"}},
            new String[] {"actionId", "name", "spots", "availableSpots", "fee", "enrolmentPeriod"}
        ));
        
        // Forzamos la selección de la primera fila
        view.getTablaCursos().setRowSelectionInterval(0, 0);

        // Al ejecutar processEnrollment, leerá "SOY_UN_TEXTO" e intentará hacer Integer.parseInt()
        assertThrows(NumberFormatException.class, () -> {
            controller.processEnrollment();
        }, "El controlador confía ciegamente en el contenido de la JTable sin capturar errores de Parseo numérico.");
    }
}