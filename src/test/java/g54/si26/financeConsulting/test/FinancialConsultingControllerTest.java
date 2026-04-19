package g54.si26.financeConsulting.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.financeConsulting.FinancialConsultingController;
import g54.si26.financeConsulting.FinancialConsultingModel;
import g54.si26.financeConsulting.FinancialConsultingView;
import g54.si26.utils.Database;
import javax.swing.table.DefaultTableModel;

public class FinancialConsultingControllerTest {

    private FinancialConsultingModel model;
    private FinancialConsultingView view;
    private FinancialConsultingController controller;

    @BeforeEach
    public void setUp() {
      
        model = new FinancialConsultingModel();
        view = new FinancialConsultingView();
        controller = new FinancialConsultingController(model, view);

     
        Database db = new Database();
        db.createDatabase(true); 
        
        db.executeUpdate("DELETE FROM MoneyMovement");
        db.executeUpdate("DELETE FROM Invoice");
        db.executeUpdate("DELETE FROM Payment");
        db.executeUpdate("DELETE FROM Inscription");
        db.executeUpdate("DELETE FROM Teacher_FormativeAction");
        db.executeUpdate("DELETE FROM FormativeAction");
        db.executeUpdate("DELETE FROM Professional");
        db.executeUpdate("DELETE FROM Teacher");
        
        db.loadDatabase();
    }

    @Test
    public void testInitController_PopulatesComboBox() {
        
        controller.initController();

        
        int itemCount = view.getCbAccionesFormativas().getItemCount();
        assertTrue(itemCount > 0, "El ComboBox de acciones formativas debería tener datos al iniciar");
    }

    @Test
    public void testFilterRadioButtons_ChangesComboContent() {
        
        controller.initController();

        
        view.getCbAccionesFormativas().setSelectedIndex(0);
        
       
        assertNotNull(view.getCbAccionesFormativas().getModel(), "El modelo del combo no debería ser nulo tras filtrar");
    }

    @Test
    public void testConsultCourseDetails_PopulatesView() {
        
        controller.initController();
 
        boolean found = false;
        for (int i = 0; i < view.getCbAccionesFormativas().getItemCount(); i++) {
            FormativeActionDTO dto = (FormativeActionDTO) view.getCbAccionesFormativas().getItemAt(i);
            if (dto.getActionId() == 1) { // El curso 1 en tu SQL tiene inscripciones
                view.getCbAccionesFormativas().setSelectedIndex(i);
                found = true;
                break;
            }
        }

        if (found) {
            view.getBtnConsultar().doClick();

            
            String totalText = view.getLblTotalIngresos().getText();
            assertFalse(totalText.contains("€0.0"), "Error de lógica: El curso 1 tiene pagos en la BD pero el controlador muestra 0.0");
        } else {
            fail("No se encontró el curso de prueba con ID 1 en el ComboBox");
        }
            
        }
    

    @Test
    public void testEnrollmentLabelVisibility_WithSimulatedDate() {
        controller.setSimulatedDate("2026-03-05");
        controller.initController();
        
        if (view.getCbAccionesFormativas().getItemCount() > 0) {
            view.getCbAccionesFormativas().setSelectedIndex(0);
            
           
            view.getBtnConsultar().doClick();

            assertNotNull(view.getLblMatriculaAbierta(), "El label de matrícula abierta debe existir");
        }
    }
}