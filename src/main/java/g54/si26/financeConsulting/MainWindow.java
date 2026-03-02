package g54.si26.financeConsulting;

import javax.swing.*;

import java.awt.*;

public class MainWindow extends JFrame {

    private JTextField txtFechaSistema;
    private JButton btnAbrirConsultaFinanciera;

    public MainWindow() {
        setTitle("Menú Principal - Simulador");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Esta sí cierra toda la app
        setLocationRelativeTo(null);
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 30));

        inicializarComponentes();
        inicializarEventos();
    }

    private void inicializarComponentes() {
        // Campo para simular la fecha de hoy en los tests
        add(new JLabel("Fecha del Sistema (Test):"));
        txtFechaSistema = new JTextField("2023-10-15", 10);
        add(txtFechaSistema);

        // Botón para abrir la Historia de Usuario
        btnAbrirConsultaFinanciera = new JButton("Abrir Reporte Financiero (US)");
        btnAbrirConsultaFinanciera.setPreferredSize(new Dimension(250, 40));
        add(btnAbrirConsultaFinanciera);
    }

    private void inicializarEventos() {
        btnAbrirConsultaFinanciera.addActionListener(e -> {
            // 1. Instanciamos las partes del patrón MVC
            FinancialConsultingView vista = new FinancialConsultingView();
            FinancialConsultingModel modelo = new FinancialConsultingModel();
            
            // 2. Conectamos todo en el controlador
            new FinancialConsultingController(vista, modelo);
            
            // 3. Mostramos la ventana de la US
            // (Opcional: Si en el futuro necesitas que el controlador sepa la fecha 
            // simulada, se la puedes pasar aquí al controlador o al modelo)
            vista.setVisible(true);
        });
    }

    public static void main(String[] args) {
        // Punto de entrada de toda la aplicación
    	System.out.println("Java está ejecutando desde: " + new java.io.File(".").getAbsolutePath());

        SwingUtilities.invokeLater(() -> {
            try {
                // Poner el estilo visual del sistema operativo
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainWindow().setVisible(true);
        });
    }
}