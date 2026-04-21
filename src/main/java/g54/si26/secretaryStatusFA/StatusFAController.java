package g54.si26.secretaryStatusFA;

import g54.si26.secretaryStatusFA.dto.FARegistrationDTO;
import g54.si26.secretaryStatusFA.dto.FAStatusDTO;
import g54.si26.utils.Util;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class StatusFAController {

    private final StatusFAModel model;
    private final StatusFAView view;
    private String simulatedDateStr;

    public StatusFAController(StatusFAModel model, StatusFAView view) {
        this.model = model;
        this.view = view;
    }

    public void setSimulatedDate(String date) {
        this.simulatedDateStr = date;
    }

    public void initController() {
        view.getTxtSystemDate().setText(simulatedDateStr);
        loadFormativeActions();
        
        view.getTblFA().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = view.getTblFA().getSelectedRow();
                if (row != -1) {
                    int actionId = (int) view.getTblFA().getValueAt(row, 0);
                    updateDetails(actionId);
                }
            }
        });

        view.getFrame().setVisible(true);
    }

    private void loadFormativeActions() {
        List<FAStatusDTO> actions = model.getFormativeActions(simulatedDateStr);
        DefaultTableModel tm = (DefaultTableModel) view.getTblFA().getModel();
        tm.setRowCount(0);
        for (FAStatusDTO fa : actions) {
            tm.addRow(new Object[]{
                fa.getActionId(),
                fa.getName(),
                fa.getStatus(),
                fa.getStartDate(),
                fa.getEndDate()
            });
        }
    }

    private void updateDetails(int actionId) {
        FAStatusDTO fa = model.getFADetail(actionId);
        if (fa != null) {
            view.getTxtName().setText(fa.getName());
            view.getTxtStatus().setText(fa.getStatus());
            view.getTxtEnrolmentPeriod().setText(fa.getInscriptionPeriodStart() + " to " + fa.getInscriptionPeriodEnd());
            view.getTxtActionDate().setText(fa.getStartDate() + " to " + fa.getEndDate());
            view.getTxtTotalSpots().setText(String.valueOf(fa.getTotalSpots()));
            view.getTxtPlacesLeft().setText(String.valueOf(fa.getPlacesLeft()));

            // Enrolment Open Label
            boolean isOpen = isEnrolmentOpen(fa.getInscriptionPeriodStart(), fa.getInscriptionPeriodEnd());
            view.getLblEnrolmentOpen().setVisible(isOpen);

            // Financials
            view.getTxtConfirmedIncome().setText(String.format("%.2f €", fa.getConfirmedIncome()));
            view.getTxtEstimatedExpenses().setText(String.format("%.2f €", fa.getEstimatedExpenses()));
            view.getTxtConfirmedExpenses().setText(String.format("%.2f €", fa.getConfirmedExpenses()));

            // Registrations
            updateRegistrations(actionId);
        }
    }

    private void updateRegistrations(int actionId) {
        List<FARegistrationDTO> registrations = model.getFARegistrations(actionId);
        DefaultTableModel tm = (DefaultTableModel) view.getTblRegistrations().getModel();
        tm.setRowCount(0);
        for (FARegistrationDTO reg : registrations) {
            tm.addRow(new Object[]{
                reg.getProfessionalName(),
                reg.getProfessionalEmail(),
                reg.getRegistrationDate(),
                String.format("%.2f", reg.getFee()),
                reg.getState()
            });
        }
    }

    private boolean isEnrolmentOpen(String start, String end) {
        if (simulatedDateStr == null || start == null || end == null) return false;
        try {
            java.util.Date current = Util.isoStringToDate(simulatedDateStr);
            java.util.Date startDate = Util.isoStringToDate(start);
            java.util.Date endDate = Util.isoStringToDate(end);
            return !current.before(startDate) && !current.after(endDate);
        } catch (Exception e) {
            return false;
        }
    }
}
