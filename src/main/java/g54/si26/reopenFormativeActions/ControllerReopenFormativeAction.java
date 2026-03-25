package g54.si26.reopenFormativeActions;

import java.util.List;
import javax.swing.table.DefaultTableModel;
import g54.si26.DTOs.FormativeActionDTO;

public class ControllerReopenFormativeAction {
    private ModelReopenFormativeAction model;
    private ViewReopenFormativeAction view;

    public ControllerReopenFormativeAction(ModelReopenFormativeAction model, ViewReopenFormativeAction view) {
        this.model = model;
        this.view = view;
    }

    public void initController() {
        view.getBtnBack().addActionListener(e -> view.getFrame().dispose());

        view.getBtnReopenAction().addActionListener(e -> {
            int selectedRow = view.getTabClosedActions().getSelectedRow();
            if (selectedRow != -1) {
                int actionId = (int) view.getTabClosedActions().getModel().getValueAt(selectedRow, 0);
                if (model.reopenFormativeAction(actionId)) {
                    view.showSuccessMessage();
                    loadClosedActions();
                } else {
                    view.showError("Failed to re-open the Formative Action.");
                }
            }
        });

        view.getTabClosedActions().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                view.getBtnReopenAction().setEnabled(view.getTabClosedActions().getSelectedRow() != -1);
            }
        });

        loadClosedActions();
        view.getFrame().setVisible(true);
    }

    private void loadClosedActions() {
        List<FormativeActionDTO> actions = model.getClosedFormativeActions();
        String[] columnNames = {"ID", "Name", "Start Date", "End Date", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        for (FormativeActionDTO action : actions) {
            Object[] row = {
                action.getActionId(),
                action.getName(),
                action.getStartDate(),
                action.getEndDate(),
                action.getStatus()
            };
            tableModel.addRow(row);
        }
        view.getTabClosedActions().setModel(tableModel);
        view.getBtnReopenAction().setEnabled(false);
    }
}
