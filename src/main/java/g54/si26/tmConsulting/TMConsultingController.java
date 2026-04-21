package g54.si26.tmConsulting;

import java.awt.Color;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import g54.si26.utils.SwingUtil;
import g54.si26.utils.ApplicationException;

public class TMConsultingController {

    private TMConsultingModel model;
    private TMConsultingView view;
    private String simulatedDateStr; 

    public TMConsultingController(TMConsultingModel m, TMConsultingView v) {
        this.model = m;
        this.view = v;
    }

    public void setSimulatedDate(String dateIso) {
        this.simulatedDateStr = dateIso;
    }

    public void initController() {
        // 1. Initialize the view
        initView();

        // 2. Consult button event
        view.getBtnConsultar().addActionListener(e -> {
            SwingUtil.exceptionWrapper(() -> loadReportData());
        });
    }

    public void initView() {
        String year;
        if (simulatedDateStr != null && simulatedDateStr.length() >= 4) {
            year = simulatedDateStr.substring(0, 4);
        } else {
            year = String.valueOf(java.time.LocalDate.now().getYear()); 
        }
        
        view.getTxtFechaInicio().setText(year + "-01-01");
        view.getTxtFechaFin().setText(year + "-12-31");
        
        SwingUtil.exceptionWrapper(() -> loadReportData());
        
        view.getFrame().setVisible(true);
    }

    private void loadReportData() {
        String startDate = view.getTxtFechaInicio().getText().trim();
        String endDate = view.getTxtFechaFin().getText().trim();
        String statusFilter = (String) view.getCbEstado().getSelectedItem();

        if (startDate.isEmpty() || endDate.isEmpty()) {
            throw new ApplicationException("Start and end dates are mandatory.");
        }

        List<Object[]> reportData = model.getReportData(startDate, endDate, statusFilter, simulatedDateStr);
        DefaultTableModel tableModel = view.getModeloTabla();
        tableModel.setRowCount(0); 

        double globalIncome = 0.0;
        double globalExpenses = 0.0;

        for (Object[] row : reportData) {
            String date = row[0] != null ? row[0].toString() : "";
            String name = row[1] != null ? row[1].toString() : "";
            String status = row[2] != null ? row[2].toString() : "";
            String dateRange = row[10] != null ? row[10].toString() : "";
            
            double fee = Double.parseDouble(row[3].toString());
            int pendingEnrollments = Integer.parseInt(row[4].toString());
            double confIncome = Double.parseDouble(row[5].toString());
            double totalExpenses = Double.parseDouble(row[6].toString());
            double confExpenses = Double.parseDouble(row[7].toString());
            int totalSpots = Integer.parseInt(row[8].toString());
            
            // EXTRAEMOS LOS CONFIRMADOS REALES
            int confirmedCount = Integer.parseInt(row[9].toString());

            boolean isClosed = "CLOSED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status);

            // CÁLCULO DE INGRESOS ESTIMADOS
            // Plazas libres = Totales - las que ya están confirmadas (hayan pagado o no)
            int freeSpots = Math.max(0, totalSpots - confirmedCount);

            // El ingreso estimado asume que todos los confirmados pagarán, más los pendientes que quepan
            int expectedNewEnrollments = Math.min(pendingEnrollments, freeSpots);
            double estIncome = (confirmedCount * fee) + (expectedNewEnrollments * fee);
            
            // Gastos estimados y Balances
            double estExpenses = Math.max(0, totalExpenses - confExpenses);
            double totalCourseIncome = confIncome;
            double totalCourseExpenses = confExpenses;
            
            double estBalance = estIncome - (estExpenses + confExpenses);
            double courseBalance = totalCourseIncome - totalCourseExpenses;

            // Add to global totals
            globalIncome += totalCourseIncome;
            globalExpenses += totalCourseExpenses;

            String strEstIncome = String.format("€%.2f", estIncome);
            String strConfIncome = String.format("€%.2f", confIncome);
            String strEstExpenses = String.format("€%.2f", estExpenses);
            String strConfExpenses = String.format("€%.2f", confExpenses);

            tableModel.addRow(new Object[]{
                dateRange, name, status,
                strEstIncome, strConfIncome, String.format("€%.2f", totalCourseIncome),
                strEstExpenses, strConfExpenses, String.format("€%.2f", totalCourseExpenses), 
                String.format("€%.2f", estBalance), String.format("€%.2f", courseBalance)
            });
        }

        updateGlobalTotals(globalIncome, globalExpenses);
    }

    private void updateGlobalTotals(double totalIncome, double totalExpenses) {
        double balance = totalIncome - totalExpenses;

        view.getLblTotalIngresos().setText(String.format("Global Total Income: €%.2f", totalIncome));
        view.getLblTotalGastos().setText(String.format("Global Total Expenses: €%.2f", totalExpenses));
        view.getLblBalance().setText(String.format("OVERALL BALANCE: €%.2f", balance));

        if (balance >= 0) {
            view.getLblBalance().setForeground(new Color(0, 153, 51));
        } else {
            view.getLblBalance().setForeground(Color.RED);
        }
    }
}