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
        DefaultTableModel emptyModel = new DefaultTableModel(columnNames, 0);
        view.getTabPayments().setModel(emptyModel);

        view.getFrame().setVisible(true);
    }

    private void loadPayments(String filter) {
        // Obtenemos las filas ya calculadas desde el modelo
        List<Object[]> rows = model.getPendingPaymentsData(filter);
        
        String[] columnNames = {"Professional/Teacher Name", "Amount", "Formative Action", "Reason"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        // Añadimos directamente las filas a la tabla
        for (Object[] rowData : rows) {
            tableModel.addRow(rowData);
        }
        
        view.getTabPayments().setModel(tableModel);
    }
}