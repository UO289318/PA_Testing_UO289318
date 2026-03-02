package g54.si26.financeConsulting;

import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.util.List;

public class FinancialConsultingController {

    private FinancialConsultingView vista;
    private FinancialConsultingModel modelo;

    public FinancialConsultingController(FinancialConsultingView vista, FinancialConsultingModel modelo) {
        this.vista = vista;
        this.modelo = modelo;

        inicializarEventos();
        // Cargar datos por defecto al iniciar
        ejecutarConsulta(); 
    }

    private void inicializarEventos() {
        vista.getBtnConsultar().addActionListener(e -> ejecutarConsulta());
    }

    private void ejecutarConsulta() {
        String fechaInicio = vista.getTxtFechaInicio().getText();
        String fechaFin = vista.getTxtFechaFin().getText();
        String estado = (String) vista.getCbEstado().getSelectedItem();

        // 1. Pedir los datos al Modelo
        List<Object[]> datos = modelo.obtenerReporteFinanciero(fechaInicio, fechaFin, estado);

        // 2. Llenar la tabla de la Vista
        DefaultTableModel modeloTabla = vista.getModeloTabla();
        modeloTabla.setRowCount(0); // Limpiar tabla anterior

        for (Object[] fila : datos) {
            modeloTabla.addRow(fila);
        }

        // 3. Calcular y mostrar los totales globales
        calcularTotalesGlobales(datos);
    }

    private void calcularTotalesGlobales(List<Object[]> datos) {
        double totalIngresosGlobal = 0;
        double totalGastosGlobal = 0;

        for (Object[] fila : datos) {
            // En nuestra tabla, el Total Ingresos está en el índice 5 y Gastos en el 8
            totalIngresosGlobal += parsearMonto((String) fila[5]);
            totalGastosGlobal += parsearMonto((String) fila[8]);
        }

        double balanceGlobal = totalIngresosGlobal - totalGastosGlobal;

        vista.getLblTotalIngresos().setText("Total Ingresos Global: $" + totalIngresosGlobal);
        vista.getLblTotalGastos().setText("Total Gastos Global: $" + totalGastosGlobal);
        vista.getLblBalance().setText("BALANCE GENERAL: $" + balanceGlobal);

        // Cambiar color del balance
        if (balanceGlobal >= 0) {
            vista.getLblBalance().setForeground(new Color(0, 153, 51)); // Verde
        } else {
            vista.getLblBalance().setForeground(Color.RED);
        }
    }

    // Método auxiliar para convertir "$5000.0" a número (5000.0)
    private double parsearMonto(String montoStr) {
        if (montoStr == null || montoStr.trim().isEmpty() || montoStr.equals("-")) return 0;
        try {
            return Double.parseDouble(montoStr.replace("$", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}