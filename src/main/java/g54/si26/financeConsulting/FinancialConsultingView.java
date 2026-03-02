package g54.si26.financeConsulting;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class FinancialConsultingView extends JFrame {

    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JComboBox<String> cbEstado;
    private JButton btnConsultar;
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalIngresos;
    private JLabel lblTotalGastos;
    private JLabel lblBalance;

    public FinancialConsultingView() {
        setTitle("Reporte Financiero de Acciones Formativas");
        setSize(1100, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // --- Panel Norte: Filtros ---
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros de Consulta"));

        panelFiltros.add(new JLabel("Fecha Inicio (AAAA-MM-DD):"));
        txtFechaInicio = new JTextField("2023-01-01", 10);
        panelFiltros.add(txtFechaInicio);

        panelFiltros.add(new JLabel("Fecha Fin (AAAA-MM-DD):"));
        txtFechaFin = new JTextField("2023-12-31", 10);
        panelFiltros.add(txtFechaFin);

        panelFiltros.add(new JLabel("Estado:"));
        cbEstado = new JComboBox<>(new String[]{"Todos", "Activo", "Cerrado"});
        panelFiltros.add(cbEstado);

        btnConsultar = new JButton("Consultar");
        panelFiltros.add(btnConsultar);
        add(panelFiltros, BorderLayout.NORTH);

        // --- Panel Central: Tabla ---
        String[] columnas = {
                "Fecha", "Nombre del Curso", "Estado",
                "Ing. Estimados", "Ing. Confirmados", "TOTAL Ingresos",
                "Gas. Estimados", "Gas. Confirmados", "TOTAL Gastos", "BALANCE"
        };

        // Inicializamos el modelo sin datos (con 0 filas)
        modeloTabla = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modeloTabla);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Detalle de Cursos"));
        add(scrollPane, BorderLayout.CENTER);

        // --- Panel Sur: Totales ---
        JPanel panelTotales = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 15));
        panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        Font fontTotales = new Font("SansSerif", Font.BOLD, 16);
        lblTotalIngresos = new JLabel("Total Ingresos Global: $0");
        lblTotalGastos = new JLabel("Total Gastos Global: $0");
        lblBalance = new JLabel("BALANCE GENERAL: $0");

        lblTotalIngresos.setFont(fontTotales);
        lblTotalGastos.setFont(fontTotales);
        lblBalance.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblBalance.setForeground(new Color(0, 128, 0));

        panelTotales.add(lblTotalIngresos);
        panelTotales.add(lblTotalGastos);
        panelTotales.add(lblBalance);
        add(panelTotales, BorderLayout.SOUTH);
    }

    // --- GETTERS ---
    public JTextField getTxtFechaInicio() { return txtFechaInicio; }
    public JTextField getTxtFechaFin() { return txtFechaFin; }
    public JComboBox<String> getCbEstado() { return cbEstado; }
    public JButton getBtnConsultar() { return btnConsultar; }
    public DefaultTableModel getModeloTabla() { return modeloTabla; }
    public JLabel getLblTotalIngresos() { return lblTotalIngresos; }
    public JLabel getLblTotalGastos() { return lblTotalGastos; }
    public JLabel getLblBalance() { return lblBalance; }
    
}

