package g54.si26.utils;

import java.awt.EventQueue;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Date;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import g54.si26.DTOs.ProfessionalDTO;

import g54.si26.cancelEnrollment.*;

import g54.si26.inscriptions.*;
import g54.si26.invoiceManagement.*;
import g54.si26.payments.*;
import g54.si26.teacherpayments.*;
import g54.si26.tmConsulting.*;
import g54.si26.viewPendingPayments.*;
import g54.si26.closeFormativeActions.*;
import g54.si26.consultFormativeActionsSecretary.*;
import g54.si26.financeConsulting.*;
import g54.si26.reopenFormativeActions.*;
import g54.si26.planFormativeAction.*;
import g54.si26.planMultipleFees.*;
import g54.si26.cancelFormativeActions.*;
import g54.si26.secretaryStatusFA.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Puntu d'entrada principal qu'inclúi botones pa la execución de les pantayes 
 * de les aplicaciones d'exemplu y aiciones d'inicialización de la base de datos.
 */
public class SwingMain {

    private JFrame frame;
    private JTextField txtSystemDate;
    private JComboBox<ProfessionalDTO> cbProfessional;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() { //NOSONAR códigu autoxeneráu
            public void run() {
                try {
                    SwingMain window = new SwingMain();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace(); //NOSONAR códigu autoxeneráu
                }
            }
        });
    }

    public SwingMain() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Main Window - G54 SI26");
        frame.setBounds(100, 100, 750, 650);
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(5, 5));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // --- FECHA SIMULADA GLOBAL ---
        JPanel dateRow = new JPanel(new BorderLayout(5, 0));
        JLabel lblDate = new JLabel("Simulated System Date (ISO):");
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSystemDate = new JTextField(Util.dateToIsoString(new Date()));
        txtSystemDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateRow.add(lblDate, BorderLayout.WEST);
        dateRow.add(txtSystemDate, BorderLayout.CENTER);
        topPanel.add(dateRow);

        JPanel professionalRow = new JPanel(new BorderLayout(5, 0));
        JLabel lblProfessional = new JLabel("Professional:              ");
        lblProfessional.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbProfessional = new JComboBox<>();
        cbProfessional.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        professionalRow.add(lblProfessional, BorderLayout.WEST);
        professionalRow.add(cbProfessional, BorderLayout.CENTER);
        topPanel.add(professionalRow);

        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        frame.getContentPane().add(columnsPanel, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel();
        leftPanel.setBorder(new TitledBorder(null, "Existing User Stories",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), null));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        columnsPanel.add(leftPanel);

        JPanel rightPanel = new JPanel();
        rightPanel.setBorder(new TitledBorder(null, "New User Stories",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new java.awt.Color(0, 120, 215)));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        columnsPanel.add(rightPanel);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        // --- BOTÓN P'ARRINCAR LA HISTORIA D'USUARIU: INSCRIPCIONES ---
        JButton btnEjecutarInscripciones = new JButton("Enrol in a Formative Action");
        btnEjecutarInscripciones.addActionListener(new ActionListener() { //NOSONAR códigu autoxeneráu
            public void actionPerformed(ActionEvent e) {
                InscriptionsModel model = new InscriptionsModel();
                InscriptionsView view = new InscriptionsView();
                InscriptionsController controller = new InscriptionsController(model, view);
                controller.setSimulatedDate(txtSystemDate.getText());
                controller.setSelectedProfessional((ProfessionalDTO) cbProfessional.getSelectedItem());
                controller.initController(); 
            }
        });
        
        // --- BOTÓN PA' REGISTRAR PAGOS DE PROFESIONALES ---
        JButton btnEjecutarPagos = new JButton("Register Payments");
        btnEjecutarPagos.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PaymentModel model = new PaymentModel();
                PaymentView view = new PaymentView();
                PaymentController controller = new PaymentController(model, view);
                
                controller.initController();
            }
        });
        
        // --- BOTÓN PA' REGISTRAR PAGOS A PROFESORES ---
        JButton btnEjecutarPagosProfesores = new JButton("Record Teacher Payments");
        btnEjecutarPagosProfesores.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            		TeacherPaymentModel model = new TeacherPaymentModel();
            		TeacherPaymentView view = new TeacherPaymentView();
            		TeacherPaymentController controller = new TeacherPaymentController(model, view);
            		controller.initController();
            }
        });
        
        // --- BOTÓN: CERRAR ACCIÓN FORMATIVA ---
        JButton btnCerrarAccion = new JButton("Close Formative Actions");
        btnCerrarAccion.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String fechaSimulada = txtSystemDate.getText();
                ViewCloseFormativeAction viewClose = new ViewCloseFormativeAction();
                ModelCloseFormativeAction modelClose = new ModelCloseFormativeAction();
                ControllerCloseFormativeAction controllerClose = new ControllerCloseFormativeAction(viewClose, modelClose, fechaSimulada);
                controllerClose.initController();
                viewClose.getFrame().setVisible(true);
            }
        });
        
        JButton btnExecuteSecretaryConsult = new JButton("Consult Money Movements");
        btnExecuteSecretaryConsult.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FinancialConsultingModel model = new FinancialConsultingModel();
                FinancialConsultingView view = new FinancialConsultingView();
                FinancialConsultingController controller = new FinancialConsultingController(model, view);
                controller.setSimulatedDate(txtSystemDate.getText());
                controller.initController(); 
            }
        });

        

JButton btnExecuteTMConsult = new JButton("Consult Income and Expenses");
        btnExecuteTMConsult.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TMConsultingModel model = new TMConsultingModel();
                TMConsultingView view = new TMConsultingView();
                TMConsultingController controller = new TMConsultingController(model, view);
                controller.setSimulatedDate(txtSystemDate.getText());
                controller.initController(); 
            }
        });
        

	    JButton btnPlanFormativeAction = new JButton("Plan Formative Action");
		btnPlanFormativeAction.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
        		ModelPlanFormativeAction model      = new ModelPlanFormativeAction();
        		ViewPlanFormativeAction  view       = new ViewPlanFormativeAction();
        		ControllerPlanFormativeAction ctrl  = new ControllerPlanFormativeAction(model, view);
        		ctrl.setSimulatedDate(txtSystemDate.getText());
        		ctrl.initController();
    		}
		});
        
	    JButton btnMultipleFees = new JButton("Plan Formative Action");
		btnMultipleFees.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
        		ModelPlanMultipleFees model      = new ModelPlanMultipleFees();
        		ViewPlanMultipleFees  view       = new ViewPlanMultipleFees();
        		ControllerPlanMultipleFees ctrl  = new ControllerPlanMultipleFees(model, view);
        		ctrl.setSimulatedDate(txtSystemDate.getText());
        		ctrl.initController();
    		}
		});
        
        JButton btnReopenFA = new JButton("Re-open FA's");
        btnReopenFA.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ModelReopenFormativeAction model = new ModelReopenFormativeAction();
                ViewReopenFormativeAction view = new ViewReopenFormativeAction();
                ControllerReopenFormativeAction controller = new ControllerReopenFormativeAction(model, view);
                controller.setSimulatedDate(txtSystemDate.getText());
                controller.initController();
            }
        });
        
        JButton btnMoneyMovements = new JButton("Register Real Money Movements");
        btnMoneyMovements.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                g54.si26.moneyMovements.MoneyMovementModel model = new g54.si26.moneyMovements.MoneyMovementModel();
                g54.si26.moneyMovements.MoneyMovementView view = new g54.si26.moneyMovements.MoneyMovementView();
                g54.si26.moneyMovements.MoneyMovementController controller = new g54.si26.moneyMovements.MoneyMovementController(model, view);
                controller.setSimulatedDate(txtSystemDate.getText());
                controller.initController();
            }
        });


     // --- BOTÓN PA' VER PAGOS PENDIENTES (NUEVA US) ---
        JButton btnViewPendingPayments = new JButton("View Pending Payments");
        btnViewPendingPayments.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ViewPendingModel model = new ViewPendingModel();
                ViewPendingView view = new ViewPendingView();
                ViewPendingController controller = new ViewPendingController(model, view);
                
                // Asegúrate de que esta línea dice initController()
                controller.initController(); 
            }
        });
        
        

        JButton btnCancelFA = new JButton("Cancel Formative Action");
        btnCancelFA.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ModelCancelFormativeAction model = new ModelCancelFormativeAction();
                g54.si26.cancelFormativeActions.ViewCancelFormativeAction view = new g54.si26.cancelFormativeActions.ViewCancelFormativeAction();
                g54.si26.cancelFormativeActions.ControllerCancelFormativeAction controller = new g54.si26.cancelFormativeActions.ControllerCancelFormativeAction(model, view);
                controller.setSimulatedDate(txtSystemDate.getText());
                controller.initController();
            }
        });
        
        JButton btnConsultFA = new JButton("Consult a list of Formative Actions for Management Purposes");
        btnConsultFA.addActionListener(e -> {
            ModelConsultFormativeActions model = new ModelConsultFormativeActions();
            ViewConsultFormativeActions view = new ViewConsultFormativeActions();
            ControllerConsultFormativeActions controller = new ControllerConsultFormativeActions(model, view);
            controller.setSimulatedDateStr(txtSystemDate.getText());
            controller.initController();
        });

        JButton btnStatusFA = new JButton("Consult FA Registrations");
        btnStatusFA.addActionListener(e -> {
            StatusFAModel model = new StatusFAModel();
            StatusFAView view = new StatusFAView();
            StatusFAController controller = new StatusFAController(model, view);
            controller.setSimulatedDate(txtSystemDate.getText());
            controller.initController();
        });
        
        

        JButton btnCancelEnrollment = new JButton("Cancel Enrollment");
        btnCancelEnrollment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ModelCancelEnrollment model = new ModelCancelEnrollment();
                g54.si26.cancelEnrollment.ViewCancelEnrollment view = new g54.si26.cancelEnrollment.ViewCancelEnrollment();
                g54.si26.cancelEnrollment.ControllerCancelEnrollment controller = new g54.si26.cancelEnrollment.ControllerCancelEnrollment(model, view);
                
                controller.setSimulatedDate(txtSystemDate.getText());
                controller.setSelectedProfessional((g54.si26.DTOs.ProfessionalDTO) cbProfessional.getSelectedItem());
                controller.initController(); 
            }
        });

        
     // --- BOTÓN PA' REGISTRAR FACTURAS DE PROFESORES ---
        JButton btnRecordInvoices = new JButton("Record Teacher Invoices");
        btnRecordInvoices.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TeacherInvoiceModel model = new TeacherInvoiceModel();
                TeacherInvoiceView view = new TeacherInvoiceView();
                TeacherInvoiceController controller = new TeacherInvoiceController(model, view);
                controller.initController();
            }
        });

        leftPanel.add(btnEjecutarPagos);
        leftPanel.add(btnEjecutarPagosProfesores);
        leftPanel.add(btnExecuteSecretaryConsult);
        
        //leftPanel.add(btnPlanFormativeAction);
        leftPanel.add(btnCerrarAccion);
        leftPanel.add(btnReopenFA);
        leftPanel.add(btnViewPendingPayments);
        
        //Aquí va Consult status of a FA US
        rightPanel.add(btnStatusFA);
        rightPanel.add(btnExecuteTMConsult);
        //Aquí va Record Invoices from Teachers US
        rightPanel.add(btnConsultFA);
        rightPanel.add(btnMoneyMovements);
        //Aquí va Start and End Date US
        //Aquí va Cancel Enrolment as Professional US
        rightPanel.add(btnMultipleFees);
        rightPanel.add(btnEjecutarInscripciones);
        rightPanel.add(btnCancelFA);

        //Aqeuí va Register new Teacehr US

        rightPanel.add(btnViewPendingPayments);
        rightPanel.add(btnCancelEnrollment);
        rightPanel.add(btnRecordInvoices);
        

        for (int i = 7; i <= 14; i++) {
            JButton placeholder = new JButton("US " + i + " – (not yet implemented)");
            placeholder.setEnabled(false);
            placeholder.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            rightPanel.add(placeholder);
        }

        // --- BOTONES DE BASE DE DATOS ---
        JButton btnInicializarBaseDeDatos = new JButton("Initialize Blank database");
        btnInicializarBaseDeDatos.addActionListener(new ActionListener() { //NOSONAR códigu autoxeneráu
            public void actionPerformed(ActionEvent e) {
                Database db = new Database();
                db.createDatabase(false);
                // Refrescamos combo tras inicializar
                loadProfessionals();
            }
        });
        bottomPanel.add(btnInicializarBaseDeDatos);
            
        JButton btnCargarDatosIniciales = new JButton("Load Data to DB");
        btnCargarDatosIniciales.addActionListener(new ActionListener() { //NOSONAR códigu autoxeneráu
            public void actionPerformed(ActionEvent e) {
                Database db = new Database();
                db.createDatabase(false);
                db.loadDatabase();
                loadProfessionals();
            }
        });
        bottomPanel.add(btnCargarDatosIniciales);

        loadProfessionals();
    }
    
    

    private void loadProfessionals() {
        try {
            Database db = new Database();
            String sql = "SELECT professional_id AS professionalId, name, surname, phone, email FROM Professional ORDER BY surname, name";
            List<ProfessionalDTO> professionals = db.executeQueryPojo(ProfessionalDTO.class, sql);
            cbProfessional.removeAllItems();
            cbProfessional.addItem(null);
            for (ProfessionalDTO p : professionals) {
                cbProfessional.addItem(p);
            }
        } catch (Exception e) {
            // Si la tabla no existe (ej. primer arranque), limpiamos el combo y silenciamos el error
            cbProfessional.removeAllItems();
            System.out.println("[Aviso] La tabla Professional no existe aún. Inicializa la DB.");
        }
    }

    public JFrame getFrame() { return this.frame; }
    public JTextField getTxtSystemDate() { return txtSystemDate; }
    public JComboBox<ProfessionalDTO> getCbProfessional() { return cbProfessional; }
}
