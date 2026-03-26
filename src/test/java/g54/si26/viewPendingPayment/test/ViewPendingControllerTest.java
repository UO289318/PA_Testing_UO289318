package g54.si26.viewPendingPayment.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import g54.si26.DTOs.MoneyMovementDTO;
import g54.si26.viewPendingPayments.ViewPendingController;
import g54.si26.viewPendingPayments.ViewPendingModel;
import g54.si26.viewPendingPayments.ViewPendingView;

public class ViewPendingControllerTest {

    private ViewPendingModel realModel;
    private ViewPendingView realView;
    private ViewPendingController controller;

    // IMPORTANTE: Asegúrate de que este nombre coincide con el de tu proyecto
    private String dbUrl = "jdbc:sqlite:database.db";

    @BeforeEach
    public void setUp() throws Exception {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
             
            // 1. Limpieza total
            stmt.execute("DROP TABLE IF EXISTS MoneyMovement");
            stmt.execute("DROP TABLE IF EXISTS Inscription");
            stmt.execute("DROP TABLE IF EXISTS Invoice");
            stmt.execute("DROP TABLE IF EXISTS Professional");
            stmt.execute("DROP TABLE IF EXISTS Teacher");
            stmt.execute("DROP TABLE IF EXISTS FormativeAction");

            // 2. Creación de tablas siguiendo TU esquema exacto
            stmt.execute("CREATE TABLE FormativeAction (action_id INTEGER PRIMARY KEY, name TEXT, status TEXT)");
            stmt.execute("CREATE TABLE Professional (professional_id INTEGER PRIMARY KEY, name TEXT, surname TEXT)");
            stmt.execute("CREATE TABLE Teacher (teacher_id INTEGER PRIMARY KEY, name TEXT)");
            
            stmt.execute("CREATE TABLE Inscription (inscription_id INTEGER PRIMARY KEY, professional_id INTEGER, action_id INTEGER, inscription_date TEXT, applied_fee REAL, state TEXT)");
            
            stmt.execute("CREATE TABLE Invoice (invoice_id INTEGER PRIMARY KEY, teacher_id INTEGER, action_id INTEGER, invoice_date TEXT, netAmount REAL, vat REAL, totalAmount REAL, status TEXT)");
            
            stmt.execute("CREATE TABLE MoneyMovement (movement_id INTEGER PRIMARY KEY, movement_date TEXT, amount REAL, status TEXT, type TEXT, inscription_id INTEGER, invoice_id INTEGER)");

            // 3. Inserción de datos coherentes para un 'Refund'
            stmt.execute("INSERT INTO FormativeAction (action_id, name, status) VALUES (1, 'Curso Test', 'ACTIVE')");
            stmt.execute("INSERT INTO Professional (professional_id, name, surname) VALUES (1, 'Ana', 'Gomez')");
            stmt.execute("INSERT INTO Inscription (inscription_id, professional_id, action_id, inscription_date, applied_fee, state) " +
                         "VALUES (10, 1, 1, '2024-01-01', 50.0, 'ACTIVE')");
            
            // Insertamos el movimiento vinculado a la inscripción 10
            stmt.execute("INSERT INTO MoneyMovement (movement_id, amount, movement_date, status, type, inscription_id) " +
                         "VALUES (100, 50.0, '2024-01-02', 'PENDING', 'Refund', 10)");
        }

        realModel = new ViewPendingModel();
        realView = new ViewPendingView();
        controller = new ViewPendingController(realModel, realView);
    }

    @AfterEach
    public void tearDown() {
        if (realView != null && realView.getFrame() != null) {
            realView.getFrame().dispose();
        }
    }

    @Test
    public void testLoadDataButton_PopulatesTableWithDatabaseData() {
        // 1. Iniciamos controlador
        controller.initController();
        
        // 2. Verificación PREVIA: ¿El modelo encuentra algo en la BD?
        List<MoneyMovementDTO> listaDto = realModel.getPendingPayments("ALL");
        assertFalse(listaDto.isEmpty(), "El modelo NO encuentra datos en la BD. Revisa la ruta del .db o el SQL.");

        // 3. Simulamos selección y clic
        realView.getCbFilter().setSelectedItem("ALL");
        realView.getBtnLoadData().doClick();

        // 4. Verificación FINAL: ¿La tabla tiene la fila?
        int rowCount = realView.getTabPayments().getModel().getRowCount();
        assertEquals(1, rowCount, "La tabla debería tener 1 fila");
        
        // Verificamos el contenido de la primera celda (Nombre)
        assertEquals("Ana Gomez", realView.getTabPayments().getModel().getValueAt(0, 0));
    }
}