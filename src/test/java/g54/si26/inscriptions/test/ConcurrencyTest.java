package g54.si26.inscriptions.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import g54.si26.DTOs.ProfessionalDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;
import g54.si26.inscriptions.*;

public class ConcurrencyTest {

    @Test
    public void testConcurrentEnrollmentLastSpot() throws InterruptedException {
        // --- 0. PREPARACIÓN DEL ESCENARIO (Inyección directa en BD) ---
        Database db = new Database();
        
        // Limpiamos por si ejecutamos el test varias veces
        db.executeUpdate("DELETE FROM Inscription WHERE action_id = 99");
        db.executeUpdate("DELETE FROM FormativeAction WHERE action_id = 99");
        
        // Insertamos un curso de prueba (ID 99) con SOLO 2 PLAZAS
        db.executeUpdate("INSERT INTO FormativeAction (action_id, name, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, fee, status, teacher_id) "
                       + "VALUES (99, 'Curso Test Concurrencia', 2, '2026-05-01', '2026-05-15', '10', '2025-01-01', '2026-12-31', 'Online', 100.0, 'ACTIVE', 1)");
        
        // Insertamos un alumno falso para ocupar 1 plaza. ¡SOLO QUEDA 1 PLAZA LIBRE!
        db.executeUpdate("INSERT INTO Inscription (inscription_date, fee, state, professional_id, action_id) "
                       + "VALUES ('2026-02-26 10:00:00', 100.0, 'CONFIRMED', 1, 99)");
        
        System.out.println("Escenario preparado: Curso 99 creado. Plazas totales: 2. Plazas ocupadas: 1. Libres: 1.");
        // --------------------------------------------------------------

        // 1. Instanciamos el modelo
        InscriptionsModel model = new InscriptionsModel();
        
        // 2. Preparamos los hilos
        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latchReady = new CountDownLatch(1);
        CountDownLatch latchDone = new CountDownLatch(numThreads);

        // 3. Preparamos al Profesional 1 (Mortadelo)
        Runnable task1 = () -> {
            try {
                latchReady.await(); 
                ProfessionalDTO p1 = new ProfessionalDTO();
                p1.setName("Mortadelo");
                p1.setSurname("Ibáñez");
                p1.setPhone("600000001");
                p1.setEmail("mortadelo@test.com");
                
                model.enrollProfessional(p1, 99);
                System.out.println("✅ Hilo 1: Mortadelo matriculado con éxito.");
            } catch (ApplicationException e) {
                System.out.println("❌ Hilo 1 falló (Esperado si no hay plaza): " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latchDone.countDown();
            }
        };

        // 4. Preparamos al Profesional 2 (Filemón)
        Runnable task2 = () -> {
            try {
                latchReady.await(); 
                ProfessionalDTO p2 = new ProfessionalDTO();
                p2.setName("Filemón");
                p2.setSurname("Pi");
                p2.setPhone("600000002");
                p2.setEmail("filemon@test.com");
                
                model.enrollProfessional(p2, 99);
                System.out.println("✅ Hilo 2: Filemón matriculado con éxito.");
            } catch (ApplicationException e) {
                System.out.println("❌ Hilo 2 falló (Esperado si no hay plaza): " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latchDone.countDown();
            }
        };

        // 5. Metemos los corredores a la pista y DISPARAMOS
        executor.submit(task1);
        executor.submit(task2);
        latchReady.countDown();

        // 6. Esperamos a que los dos terminen
        latchDone.await();
        executor.shutdown();

        // --- 7. VERIFICACIÓN AUTOMÁTICA ---
        String sqlCount = "SELECT COUNT(*) FROM Inscription WHERE action_id = 99 AND state IN ('RECEIVED', 'CONFIRMED')";
        int totalInscritos = Integer.parseInt(db.executeQueryArray(sqlCount).get(0)[0].toString());
        
        System.out.println("\n--- RESULTADO FINAL ---");
        System.out.println("Total de inscritos en la BD para el curso 99: " + totalInscritos);
        
        if (totalInscritos == 3) {
            System.out.println("⚠️ ¡PELIGRO! Condición de carrera detectada. Hay 3 inscritos para 2 plazas.");
            // Hacemos fallar el test a propósito para que Eclipse lo marque en rojo
            fail("Overbooking detectado: El sistema permitió más inscripciones que plazas.");
        } else if (totalInscritos == 2) {
            System.out.println("✅ ¡ÉXITO! El sistema es robusto. Solo 2 inscritos.");
        }
    }
}