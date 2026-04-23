package g54.si26.financeConsulting;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import g54.si26.DTOs.FormativeActionDTO;

public class FinancialConsultingView {

    private JFrame frame;
    
    // Filter and Selection Components
    private JComboBox<String> cbStatusFilter;
    private JComboBox<FormativeActionDTO> cbAccionesFormativas;
    private JButton btnConsultar;
    
    // Basic Details Components
    private JLabel txtNombre, txtEstado, txtPeriodo, txtFecha, txtPlazasTotales, txtPlazasLibres;
    private JLabel lblMatriculaAbierta;
    
    // Table and Totals Components
    private JTable tablaMovimientos;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalIngresos, lblTotalGastos, lblBalance;

    public FinancialConsultingView() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Consult money movements of the formative actions");
        frame.setBounds(100, 100, 950, 650);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));

        // ==========================================
        // NORTH PANEL: Filters and Selection
        // ==========================================
        JPanel panelNorte = new JPanel(new BorderLayout());
        
        // 1. Filters Panel (The requested "Grid")
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        panelFiltros.setBorder(BorderFactory.createTitledBorder(null, "Filter by Status", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));
        
        cbStatusFilter = new JComboBox<>(new String[]{
                "ACTIVE (Default)", "ALL", "Upcoming", "Enrolment open", "In progress", "Finished", "CLOSED", "Cancelled"
            });
        cbStatusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbStatusFilter.setBackground(Color.WHITE);
        panelFiltros.add(cbStatusFilter);
        panelNorte.add(panelFiltros, BorderLayout.NORTH);

        // 2. Selection Panel (Combo + Consult Button)
        JPanel panelSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        panelSeleccion.setBorder(BorderFactory.createTitledBorder(null, "Select Formative Action", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));
        panelSeleccion.add(new JLabel("Select:"));
        
        cbAccionesFormativas = new JComboBox<>();
        cbAccionesFormativas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbAccionesFormativas.setBackground(Color.WHITE);
        cbAccionesFormativas.setPreferredSize(new Dimension(500, 25));
        cbAccionesFormativas.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof FormativeActionDTO) {
                    FormativeActionDTO dto = (FormativeActionDTO) value;
                    setText(dto.getName());
                }
                return this;
            }
        });
        panelSeleccion.add(cbAccionesFormativas);
        
        btnConsultar = new JButton("Consult");
        btnConsultar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelSeleccion.add(btnConsultar);
        
        panelNorte.add(panelSeleccion, BorderLayout.CENTER);
        frame.getContentPane().add(panelNorte, BorderLayout.NORTH);

        // ==========================================
        // CENTER PANEL: Details and Table
        // ==========================================
        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Basic Details
        JPanel panelDetalles = new JPanel(new GridLayout(3, 4, 10, 10));
        panelDetalles.setBorder(BorderFactory.createTitledBorder(null, "Basic Details of the Formative Action", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));

        panelDetalles.add(new JLabel("<html><b>Name:</b></html>"));
        txtNombre = new JLabel("-"); panelDetalles.add(txtNombre);

        panelDetalles.add(new JLabel("<html><b>Status:</b></html>"));
        txtEstado = new JLabel("-"); panelDetalles.add(txtEstado);

        panelDetalles.add(new JLabel("<html><b>Enrollment Period:</b></html>"));
        txtPeriodo = new JLabel("-"); panelDetalles.add(txtPeriodo);

        panelDetalles.add(new JLabel("<html><b>Start Date:</b></html>"));
        txtFecha = new JLabel("-"); panelDetalles.add(txtFecha);

        panelDetalles.add(new JLabel("<html><b>Total Places:</b></html>"));
        txtPlazasTotales = new JLabel("-"); panelDetalles.add(txtPlazasTotales);

        panelDetalles.add(new JLabel("<html><b>Places Left:</b></html>"));
        txtPlazasLibres = new JLabel("-"); panelDetalles.add(txtPlazasLibres);

        // Enrollment Open Label
        lblMatriculaAbierta = new JLabel("ENROLLMENT PERIOD OPEN!");
        lblMatriculaAbierta.setForeground(new Color(0, 153, 51));
        lblMatriculaAbierta.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMatriculaAbierta.setHorizontalAlignment(SwingConstants.CENTER);
        lblMatriculaAbierta.setVisible(false);
        
        JPanel panelAviso = new JPanel(new BorderLayout());
        panelAviso.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panelAviso.add(lblMatriculaAbierta, BorderLayout.CENTER);
        
        JPanel panelDetallesContenedor = new JPanel(new BorderLayout());
        panelDetallesContenedor.add(panelDetalles, BorderLayout.CENTER);
        panelDetallesContenedor.add(panelAviso, BorderLayout.SOUTH);
        panelCentro.add(panelDetallesContenedor, BorderLayout.NORTH);

        // Table
        String[] columnas = {"Date", "Concept (Detail)", "Amount"};
        modeloTabla = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaMovimientos = new JTable(modeloTabla);
        tablaMovimientos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaMovimientos.setRowHeight(25);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tablaMovimientos.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(tablaMovimientos);
        scrollPane.setBorder(BorderFactory.createTitledBorder(null, "Movement History", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));
        panelCentro.add(scrollPane, BorderLayout.CENTER);

        frame.getContentPane().add(panelCentro, BorderLayout.CENTER);

        // ==========================================
        // SOUTH PANEL: Totals
        // ==========================================
        JPanel panelTotales = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 15));
        panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        Font fontTotales = new Font("Segoe UI", Font.BOLD, 16);
        lblTotalIngresos = new JLabel("Total Income: €0.00");
        lblTotalGastos = new JLabel("Total Expenses: €0.00");
        lblBalance = new JLabel("Course Balance: €0.00");

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
    public JComboBox<FormativeActionDTO> getCbAccionesFormativas() { return cbAccionesFormativas; }
    public JButton getBtnConsultar() { return btnConsultar; }
    
    public JLabel getTxtNombre() { return txtNombre; }
    public JLabel getTxtEstado() { return txtEstado; }
    public JLabel getTxtPeriodo() { return txtPeriodo; }
    public JLabel getTxtFecha() { return txtFecha; }
    public JLabel getTxtPlazasTotales() { return txtPlazasTotales; }
    public JLabel getTxtPlazasLibres() { return txtPlazasLibres; }
    public JLabel getLblMatriculaAbierta() { return lblMatriculaAbierta; }
    public DefaultTableModel getModeloTabla() { return modeloTabla; }
    public JLabel getLblTotalIngresos() { return lblTotalIngresos; }
    public JLabel getLblTotalGastos() { return lblTotalGastos; }
    public JLabel getLblBalance() { return lblBalance; }
    public JComboBox<String> getCbStatusFilter() { return cbStatusFilter; }

    public void resetForm() {
        txtNombre.setText("-"); txtEstado.setText("-"); txtPeriodo.setText("-");
        txtFecha.setText("-"); txtPlazasTotales.setText("-"); txtPlazasLibres.setText("-");
        lblMatriculaAbierta.setVisible(false);
        modeloTabla.setRowCount(0);
        lblTotalIngresos.setText("Confirmed Income: €0.00");
        lblTotalGastos.setText("Confirmed Expenses: €0.00");
        lblBalance.setText("Confirmed Balance: €0.00");
        lblBalance.setForeground(Color.BLACK);
    }
}