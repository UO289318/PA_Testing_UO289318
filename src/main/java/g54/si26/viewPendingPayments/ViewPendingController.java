package g54.si26.viewPendingPayments;

import java.util.List;
import javax.swing.table.DefaultTableModel;

public class ViewPendingController {
    private ViewPendingModel model;
    private ViewPendingView view;

    public ViewPendingController(ViewPendingModel model, ViewPendingView view) {
        this.model = model;
        this.view = view;
    }

    public void initController() {
        view.getBtnBack().addActionListener(e -> view.getFrame().dispose());

        view.getBtnLoadData().addActionListener(e -> {
            String selectedFilter = (String) view.getCbFilter().getSelectedItem();
            loadPayments(selectedFilter);
        });

        String[] columnNames = {"Professional/Teacher Name", "Amount", "Formative Action", "Reason"};
        DefaultTableModel emptyModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        view.getTabPayments().setModel(emptyModel);

        view.getFrame().setVisible(true);
    }

    private void loadPayments(String filter) {
        List<Object[]> rows = model.getPendingPaymentsData(filter);
        
        String[] columnNames = {"Professional/Teacher Name", "Amount", "Formative Action", "Reason"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Object[] rowData : rows) {
            try {
                double amount = Double.parseDouble(rowData[1].toString());
                // Usamos Locale.US para asegurar que el punto sea el separador decimal
                rowData[1] = String.format(java.util.Locale.US, "€%.2f", amount);
            } catch (Exception e) {
                rowData[1] = "€0.00";
            }
            tableModel.addRow(rowData);
        }
        
        view.getTabPayments().setModel(tableModel);
    }
}