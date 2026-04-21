package g54.si26.tmConsulting;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TMConsultingView {

    private JFrame frame;
    
    // Filter Components
    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JComboBox<String> cbEstado;
    private JButton btnConsultar;
    
    // Table Components
    private JTable tablaCursos;
    private DefaultTableModel modeloTabla;
    
    // Totals Components
    private JLabel lblTotalIngresos;
    private JLabel lblTotalGastos;
    private JLabel lblBalance;
    private JLabel lblEstBalance;

    public TMConsultingView() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Consult the income and expenses of the formative actions");
        frame.setBounds(100, 100, 1100, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));

        // ==========================================
        // NORTH PANEL: Search Filters
        // ==========================================
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        panelFiltros.setBorder(BorderFactory.createTitledBorder(null, "Search Filters", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));

        panelFiltros.add(new JLabel("Start Date (YYYY-MM-DD):"));
        txtFechaInicio = new JTextField(10);
        txtFechaInicio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelFiltros.add(txtFechaInicio);

        panelFiltros.add(new JLabel("End Date (YYYY-MM-DD):"));
        txtFechaFin = new JTextField(10);
        txtFechaFin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelFiltros.add(txtFechaFin);

        panelFiltros.add(new JLabel("Status:"));
        cbEstado = new JComboBox<>(new String[]{"All", "Active", "Closed"});
        cbEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbEstado.setBackground(Color.WHITE);
        panelFiltros.add(cbEstado);

        btnConsultar = new JButton("Consult");
        btnConsultar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelFiltros.add(btnConsultar);

        frame.getContentPane().add(panelFiltros, BorderLayout.NORTH);

        // ==========================================
        // CENTER PANEL: Data Table
        // ==========================================
        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        String[] columnas = {
                "Date", "Course Name", "Status",
                "Est. Income", "Conf. Income", "TOTAL Income",
                "Est. Expenses", "Conf. Expenses", "TOTAL Expenses", "Est. BALANCE", "BALANCE"
        };

        modeloTabla = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaCursos = new JTable(modeloTabla);
        tablaCursos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaCursos.setRowHeight(25);
        tablaCursos.getTableHeader().setReorderingAllowed(false);
        
        // Align amounts to the right
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        for (int i = 3; i < tablaCursos.getColumnCount(); i++) {
            tablaCursos.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tablaCursos);
        scrollPane.setBorder(BorderFactory.createTitledBorder(null, "Course Details", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));
        panelCentro.add(scrollPane, BorderLayout.CENTER);

        frame.getContentPane().add(panelCentro, BorderLayout.CENTER);

        // ==========================================
        // SOUTH PANEL: Global Totals
        // ==========================================
        JPanel panelTotales = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 15));
        panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        Font fontTotales = new Font("Segoe UI", Font.BOLD, 15);
        lblTotalIngresos = new JLabel("Global Total Income: €0.00");
        lblTotalGastos = new JLabel("Global Total Expenses: €0.00");
        lblEstBalance = new JLabel("OVERALL EST. BALANCE: €0.00");
        lblBalance = new JLabel("OVERALL BALANCE: €0.00");

        lblTotalIngresos.setFont(fontTotales);
        lblTotalGastos.setFont(fontTotales);
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 18));

        panelTotales.add(lblTotalIngresos);
        panelTotales.add(lblTotalGastos);
        panelTotales.add(lblBalance);
        
        frame.getContentPane().add(panelTotales, BorderLayout.SOUTH);
    }

    // --- Getters ---
    public JFrame getFrame() { return frame; }
    public JTextField getTxtFechaInicio() { return txtFechaInicio; }
    public JTextField getTxtFechaFin() { return txtFechaFin; }
    public JComboBox<String> getCbEstado() { return cbEstado; }
    public JButton getBtnConsultar() { return btnConsultar; }
    public DefaultTableModel getModeloTabla() { return modeloTabla; }
    public JLabel getLblTotalIngresos() { return lblTotalIngresos; }
    public JLabel getLblTotalGastos() { return lblTotalGastos; }
    public JLabel getLblEstBalance() { return lblEstBalance; }
    public JLabel getLblBalance() { return lblBalance; }
}