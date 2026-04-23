package g54.si26.financeConsulting;

import java.awt.Color;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.utils.SwingUtil;
import g54.si26.utils.ApplicationException;

public class FinancialConsultingController {

    private FinancialConsultingModel model;
    private FinancialConsultingView view;
    private String simulatedDateStr;

    public FinancialConsultingController(FinancialConsultingModel m, FinancialConsultingView v) {
        this.model = m;
        this.view = v;
    }

    public void setSimulatedDate(String dateIso) {
        this.simulatedDateStr = dateIso;
    }

    public void initController() {
        // 1. Load initial view
        this.initView();

        // 2. ComboBox events (Filters)
        view.getCbStatusFilter().addActionListener(e -> {
            String filter = (String) view.getCbStatusFilter().getSelectedItem();
            reloadCoursesCombo(filter);
        });
        // 3. "Consult" button event
        view.getBtnConsultar().addActionListener(e -> {
            SwingUtil.exceptionWrapper(() -> {
                FormativeActionDTO selectedAction = (FormativeActionDTO) view.getCbAccionesFormativas().getSelectedItem();
                if (selectedAction == null) {
                    throw new ApplicationException("Please, select a valid Formative Action.");
                }
                loadCourseDetails(selectedAction.getActionId());
            });
        });
    }

    public void initView() {
    		String defaultFilter = (String) view.getCbStatusFilter().getSelectedItem();
        reloadCoursesCombo(defaultFilter); // Initial load
        view.getFrame().setVisible(true);
    }

    // Reloads the dropdown depending on the selected filter
    private void reloadCoursesCombo(String filter) {
        view.getCbAccionesFormativas().removeAllItems();
        List<FormativeActionDTO> actions = model.getFormativeActionsByStatus(filter, simulatedDateStr);
        for (FormativeActionDTO a : actions) {
            view.getCbAccionesFormativas().addItem(a);
        }
        view.resetForm(); // Clear the bottom screen when changing the filter
    }

    private void loadCourseDetails(int actionId) {
        // 1. Load basic data
        Object[] basicData = model.getCourseBasicData(actionId, simulatedDateStr);

        view.getTxtNombre().setText(basicData[0] != null ? basicData[0].toString() : "-");
        view.getTxtEstado().setText(basicData[1] != null ? basicData[1].toString() : "-");
        
        String startPeriod = basicData[2] != null ? basicData[2].toString() : "";
        String endPeriod = basicData[3] != null ? basicData[3].toString() : "";
        view.getTxtPeriodo().setText(startPeriod + " to " + endPeriod);
        
        view.getTxtFecha().setText(basicData[4] != null ? basicData[4].toString() : "-");
        view.getTxtPlazasTotales().setText(basicData[5] != null ? basicData[5].toString() : "0");
        view.getTxtPlazasLibres().setText(basicData[6] != null ? basicData[6].toString() : "0");

        // Check if the enrollment period is open
        if (simulatedDateStr != null && !startPeriod.isEmpty() && !endPeriod.isEmpty()) {
            boolean isOpen = (simulatedDateStr.compareTo(startPeriod) >= 0 && simulatedDateStr.compareTo(endPeriod) <= 0);
            view.getLblMatriculaAbierta().setVisible(isOpen);
        }

        // 2. Load Movements and Totals
        List<Object[]> movements = model.getMovements(actionId);
        DefaultTableModel tableModel = view.getModeloTabla();
        tableModel.setRowCount(0);

        double totalIncomes = 0.0;
        double totalExpenses = 0.0;

        for (Object[] row : movements) {
            String date = row[0].toString();
            String concept = row[1].toString();
            
            double amount = Math.abs(Double.parseDouble(row[2].toString()));
            int isIncome = Integer.parseInt(row[3].toString());

            String qtyStr = (isIncome == 1 ? "+ €" : "- €") + amount;
            tableModel.addRow(new Object[]{date, concept, qtyStr});

            if (isIncome == 1) {
                totalIncomes += amount;
            } else {
                totalExpenses += amount;
            }
        }

        double balance = totalIncomes - totalExpenses;

        // 3. Update UI
        view.getLblTotalIngresos().setText("Confirmed Income: €" + totalIncomes);
        view.getLblTotalGastos().setText("Confirmed Expenses: €" + totalExpenses);
        view.getLblBalance().setText("Course Confirmed Balance: €" + balance);

        if (balance >= 0) {
            view.getLblBalance().setForeground(new Color(0, 153, 51));
        } else {
            view.getLblBalance().setForeground(Color.RED);
        }
    }
}