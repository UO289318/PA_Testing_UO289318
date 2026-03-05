package g54.si26.utils;

import java.awt.EventQueue;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;


import g54.si26.inscriptions.InscriptionsController;
import g54.si26.inscriptions.InscriptionsModel;
import g54.si26.inscriptions.InscriptionsView;
import g54.si26.closeFormativeActions.ControllerCloseFormativeAction;
import g54.si26.closeFormativeActions.ModelCloseFormativeAction;
import g54.si26.closeFormativeActions.ViewCloseFormativeAction;
import g54.si26.financeConsulting.*;
import g54.si26.tmConsulting.*;
import g54.si26.payments.PaymentController;
import g54.si26.payments.PaymentModel;
import g54.si26.payments.PaymentView;
import g54.si26.closeFormativeActions.*;


import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Puntu d'entrada principal qu'inclúi botones pa la execución de les pantayes 
 * de les aplicaciones d'exemplu y aiciones d'inicialización de la base de datos.
 */
public class SwingMain {

    private JFrame frame;
    private JTextField txtSystemDate;

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
        frame.setTitle("Menú Principal - G54 SI26");
        frame.setBounds(100, 100, 450, 400); 
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        
        // --- FECHA SIMULADA GLOBAL ---
        frame.getContentPane().add(new JLabel("Fecha Simulada del Sistema (ISO):"));
        txtSystemDate = new JTextField(Util.dateToIsoString(new Date()));
        frame.getContentPane().add(txtSystemDate);
        
        // --- BOTÓN P'ARRINCAR LA HISTORIA D'USUARIU: INSCRIPCIONES ---
        JButton btnEjecutarInscripciones = new JButton("Abrir Inscripción de Profesionales");
        btnEjecutarInscripciones.addActionListener(new ActionListener() { //NOSONAR códigu autoxeneráu
            public void actionPerformed(ActionEvent e) {
                InscriptionsModel model = new InscriptionsModel();
                InscriptionsView view = new InscriptionsView();
                InscriptionsController controller = new InscriptionsController(model, view);
                controller.setSimulatedDate(txtSystemDate.getText());
                controller.initController(); 
            }
        });
        frame.getContentPane().add(btnEjecutarInscripciones);
        
        // --- BOTÓN PA' REGISTRAR PAGOS DE PROFESIONALES ---
        JButton btnEjecutarPagos = new JButton("Registrar Pagos de Profesionales");
        btnEjecutarPagos.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PaymentModel model = new PaymentModel();
                PaymentView view = new PaymentView();
                PaymentController controller = new PaymentController(model, view);
                controller.initController();
            }
        });
        frame.getContentPane().add(btnEjecutarPagos);

        // --- BOTÓN PA' REGISTRAR PAGOS A PROFESORES ---
        JButton btnEjecutarPagosProfesores = new JButton("Registrar Pagos a Profesores");
        btnEjecutarPagosProfesores.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                g54.si26.teacherpayments.TeacherPaymentModel model = new g54.si26.teacherpayments.TeacherPaymentModel();
                g54.si26.teacherpayments.TeacherPaymentView view = new g54.si26.teacherpayments.TeacherPaymentView();
                g54.si26.teacherpayments.TeacherPaymentController controller = new g54.si26.teacherpayments.TeacherPaymentController(model, view);
                controller.initController();
            }
        });
        frame.getContentPane().add(btnEjecutarPagosProfesores);

        // --- BOTÓN: CERRAR ACCIÓN FORMATIVA ---
        JButton btnCerrarAccion = new JButton("Cerrar Acción Formativa");
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
        frame.getContentPane().add(btnCerrarAccion);

        
        // --- BOTONES DE BASE DE DATOS ---
        JButton btnInicializarBaseDeDatos = new JButton("Inicializar Base de Datos en Blanco");
        btnInicializarBaseDeDatos.addActionListener(new ActionListener() { //NOSONAR códigu autoxeneráu
            public void actionPerformed(ActionEvent e) {
                Database db = new Database();
                db.createDatabase(false);
            }
        });
        frame.getContentPane().add(btnInicializarBaseDeDatos);
            
        JButton btnCargarDatosIniciales = new JButton("Cargar Datos Iniciales pa Pruebes");
        btnCargarDatosIniciales.addActionListener(new ActionListener() { //NOSONAR códigu autoxeneráu
            public void actionPerformed(ActionEvent e) {
                Database db = new Database();
                db.createDatabase(false);
                db.loadDatabase();
            }
        });
        frame.getContentPane().add(btnCargarDatosIniciales);
        
        
        JButton btnExecuteSecretaryConsult = new JButton("Secretary Consult");
        btnExecuteSecretaryConsult.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Equí instanciamos el nuesu MVC nuevu
                FinancialConsultingModel model = new FinancialConsultingModel();
                FinancialConsultingView view = new FinancialConsultingView();
                FinancialConsultingController controller = new FinancialConsultingController(model, view);
                
                // Le pasamos la fecha global al controlador antes de arrancar
                controller.setSimulatedDate(txtSystemDate.getText());
                
                // Arrincamos los listeners del controlador
                controller.initController(); 
            }
        });
        
        JButton btnExecuteTMConsult = new JButton("Training Manager Consult");
        btnExecuteTMConsult.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Equí instanciamos el nuesu MVC nuevu
                TMConsultingModel model = new TMConsultingModel();
                TMConsultingView view = new TMConsultingView();
                TMConsultingController controller = new TMConsultingController(model, view);
                
                // Le pasamos la fecha global al controlador antes de arrancar
                controller.setSimulatedDate(txtSystemDate.getText());
                
                // Arrincamos los listeners del controlador
                controller.initController(); 
            }
        });
        frame.getContentPane().add(btnExecuteTMConsult);
        frame.getContentPane().add(btnEjecutarInscripciones);
        frame.getContentPane().add(btnExecuteSecretaryConsult);
        
    }

    public JFrame getFrame() { return this.frame; }
}
