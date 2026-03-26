package g54.si26.viewPendingPayments;

import java.util.List;
import javax.swing.table.DefaultTableModel;
import g54.si26.DTOs.MoneyMovementDTO;

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
        DefaultTableModel emptyModel = new DefaultTableModel(columnNames, 0);
        view.getTabPayments().setModel(emptyModel);

        view.getFrame().setVisible(true);
    }

    private void loadPayments(String filter) {
        List<MoneyMovementDTO> movements = model.getPendingPayments(filter);
        
        String[] columnNames = {"Professional/Teacher Name", "Amount", "Formative Action", "Reason"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        for (MoneyMovementDTO movement : movements) {
            Object[] rowData = model.getPaymentDetailsForView(movement.getMovementId());
            if (rowData != null) {
                tableModel.addRow(rowData);
            }
        }
        
        view.getTabPayments().setModel(tableModel);
    }
}