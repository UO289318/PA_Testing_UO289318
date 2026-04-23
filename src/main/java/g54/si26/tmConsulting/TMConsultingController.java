package g54.si26.tmConsulting;

import java.awt.Color;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
        initView();

        view.getBtnConsultar().addActionListener(e -> {
            SwingUtil.exceptionWrapper(() -> loadReportData());
        });
    }

    public void initView() {
        String year;
        if (simulatedDateStr != null && simulatedDateStr.length() >= 4) {
            year = simulatedDateStr.substring(0, 4);
        } else {
            year = String.valueOf(LocalDate.now().getYear()); 
        }
        
        view.getTxtFechaInicio().setText(year + "-01-01");
        view.getTxtFechaFin().setText(year + "-12-31");
        
        SwingUtil.exceptionWrapper(() -> loadReportData());
        
        view.getFrame().setVisible(true);
    }

    private void loadReportData() {
        String startDateRaw = view.getTxtFechaInicio().getText().trim();
        String endDateRaw = view.getTxtFechaFin().getText().trim();
        String statusFilter = (String) view.getCbEstado().getSelectedItem();

        if (startDateRaw.isEmpty() || endDateRaw.isEmpty()) {
            throw new ApplicationException("Start and end dates are mandatory.");
        }

        String startDateDB;
        String endDateDB;
        try {
            LocalDate start = LocalDate.parse(startDateRaw);
            LocalDate end = LocalDate.parse(endDateRaw);
            
            if (start.isAfter(end)) {
                throw new ApplicationException("The Start Date cannot be after the End Date.");
            }
            
            startDateDB = start.toString();
            endDateDB = end.toString();
        } catch (DateTimeParseException ex) {
            throw new ApplicationException("Invalid or non-existent date detected. Please use a valid YYYY-MM-DD date.");
        }
        // -----------------------------------

        List<Object[]> reportData = model.getReportData(startDateDB, endDateDB, statusFilter, simulatedDateStr);
        DefaultTableModel tableModel = view.getModeloTabla();
        tableModel.setRowCount(0); 

        double globalIncome = 0.0;
        double globalExpenses = 0.0;

        for (Object[] row : reportData) {
            String dateRange = row[10] != null ? row[10].toString() : "";
            String name = row[1] != null ? row[1].toString() : "";
            String status = row[2] != null ? row[2].toString() : "";
            
            double fee = Double.parseDouble(row[3].toString());
            int pendingEnrollments = Integer.parseInt(row[4].toString());
            double confIncome = Double.parseDouble(row[5].toString());
            
            double totalRemuneration = Double.parseDouble(row[6].toString());
            double confExpenses = Double.parseDouble(row[7].toString());
            int totalSpots = Integer.parseInt(row[8].toString());
            
            int confirmedCount = Integer.parseInt(row[9].toString());

            // 1. CÁLCULO DE INGRESOS ESTIMADOS (Est. Income)
            int freeSpots = Math.max(0, totalSpots - confirmedCount);
            int expectedNewEnrollments = Math.min(pendingEnrollments, freeSpots);
            
            double expectedFromConfirmed = Math.max(confIncome, confirmedCount * fee);
            double estIncome = expectedFromConfirmed + (expectedNewEnrollments * fee);
            
            // 2. CÁLCULO DE GASTOS ESTIMADOS (Est. Expenses)
            double estExpenses = Math.max(confExpenses, totalRemuneration);
            
            // 3. BALANCES
            double estBalance = estIncome - estExpenses;
            double courseBalance = confIncome - confExpenses; 

            globalIncome += confIncome;
            globalExpenses += confExpenses;

            String strEstIncome = String.format(java.util.Locale.US, "€%.2f", estIncome);
            String strConfIncome = String.format(java.util.Locale.US, "€%.2f", confIncome);
            String strEstExpenses = String.format(java.util.Locale.US, "€%.2f", estExpenses);
            String strConfExpenses = String.format(java.util.Locale.US, "€%.2f", confExpenses);

            tableModel.addRow(new Object[]{
                dateRange, name, status,
                strEstIncome, strConfIncome, 
                strEstExpenses, strConfExpenses, 
                String.format(java.util.Locale.US, "€%.2f", estBalance), 
                String.format(java.util.Locale.US, "€%.2f", courseBalance)
            });
        }

        updateGlobalTotals(globalIncome, globalExpenses);
    }

    private void updateGlobalTotals(double totalIncome, double totalExpenses) {
        double balance = totalIncome - totalExpenses;

        view.getLblTotalIngresos().setText(String.format(java.util.Locale.US, "Total Income: €%.2f", totalIncome));
        view.getLblTotalGastos().setText(String.format(java.util.Locale.US, "Total Expenses: €%.2f", totalExpenses));
        view.getLblBalance().setText(String.format(java.util.Locale.US, "TOTAL BALANCE: €%.2f", balance));

        if (balance >= 0) {
            view.getLblBalance().setForeground(new Color(0, 153, 51));
        } else {
            view.getLblBalance().setForeground(Color.RED);
        }
    }
}