package g54.si26.cancelEnrollment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import g54.si26.utils.Util;

public class ControllerCancelEnrollment {

    private ModelCancelEnrollment model;
    private ViewCancelEnrollment view;
    
    private String simulatedDateStr;
    private int professionalId;
    
    private double currentCalculatedRefund = 0.0;
    private int selectedInscriptionId = -1;

    public ControllerCancelEnrollment(ModelCancelEnrollment model, ViewCancelEnrollment view) {
        this.model = model;
        this.view = view;
    }

    public void setSimulatedDate(String simulatedDate) {
        this.simulatedDateStr = simulatedDate;
    }
    
    public void setProfessionalId(int professionalId) {
        this.professionalId = professionalId;
    }

    public void initController() {
        view.getTxtCurrentDate().setText(simulatedDateStr);

        // Listeners
        view.getTableEnrollments().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateDetailsPanel();
        });

        view.getCbReason().addActionListener(e -> {
            String selected = (String) view.getCbReason().getSelectedItem();
            view.getTxtDetails().setEnabled("Other (Optional: type details below)".equals(selected));
        });

        view.getBtnCancelSelected().addActionListener(e -> cancelEnrollmentAction());
        view.getBtnBack().addActionListener(e -> view.getFrame().dispose());

        updateEnrollmentsTable();
        view.getFrame().setVisible(true);
    }

    private void updateEnrollmentsTable() {
        List<Object[]> enrollments = model.getActiveEnrollments(professionalId);
        String[] columns = {"Inscription ID", "Course Name", "Start Date", "End Date", "Fee Applied", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Object[] row : enrollments) {
            tableModel.addRow(new Object[]{
                row[0], row[1], row[2], row[3], 
                String.format("%.2f", Double.parseDouble(row[4].toString())), row[5]
            });
        }
        view.getTableEnrollments().setModel(tableModel);
        clearDetails();
    }

    private void updateDetailsPanel() {
        int row = view.getTableEnrollments().getSelectedRow();
        if (row == -1) {
            clearDetails();
            return;
        }

        selectedInscriptionId = Integer.parseInt(view.getTableEnrollments().getValueAt(row, 0).toString());
        String courseName = view.getTableEnrollments().getValueAt(row, 1).toString();
        String startDateStr = view.getTableEnrollments().getValueAt(row, 2).toString();
        double feePaid = Double.parseDouble(view.getTableEnrollments().getValueAt(row, 4).toString().replace(",", "."));

        // Convertir fechas para calcular los días
        Date simDateUtil = Util.isoStringToDate(simulatedDateStr);
        LocalDate simDate = simDateUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        
        long daysRemaining = ChronoUnit.DAYS.between(simDate, startDate);
        if (daysRemaining < 0) daysRemaining = 0; 

        view.getTxtSelectedCourse().setText(selectedInscriptionId + " - " + courseName);
        view.getTxtStartDate().setText(startDateStr);
        view.getTxtDaysRemaining().setText(daysRemaining + " (Calendar Days)");

        String policyTier = "";
        double percentage = 0.0;

        if (daysRemaining >= 7) {
            policyTier = "7+ Days (100% Refund)";
            percentage = 1.0;
        } else if (daysRemaining >= 3 && daysRemaining <= 6) {
            policyTier = "3-6 Days (50% Refund)";
            percentage = 0.5;
        } else {
            policyTier = "0-2 Days (No Refund)";
            percentage = 0.0;
        }

        currentCalculatedRefund = feePaid * percentage;

        view.getLblTotalFeePaid().setText(String.format("€%.2f", feePaid));
        view.getLblAppliedPolicy().setText(policyTier);
        view.getLblTotalRefundDue().setText(String.format("€%.2f", currentCalculatedRefund));
        view.getBtnCancelSelected().setEnabled(true);
    }

    private void clearDetails() {
        selectedInscriptionId = -1;
        view.getTxtSelectedCourse().setText("");
        view.getTxtStartDate().setText("");
        view.getTxtDaysRemaining().setText("");
        view.getLblTotalFeePaid().setText("€0.00");
        view.getLblAppliedPolicy().setText("-");
        view.getLblTotalRefundDue().setText("€0.00");
        view.getCbReason().setSelectedIndex(0);
        view.getTxtDetails().setText("");
        view.getBtnCancelSelected().setEnabled(false);
    }

    private void cancelEnrollmentAction() {
        if (selectedInscriptionId == -1) return;

        if (view.getCbReason().getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select a reason for withdrawal.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String message = String.format("Are you sure you want to cancel your enrollment?\n\nPotential Refund: €%.2f", currentCalculatedRefund);
        int confirm = JOptionPane.showConfirmDialog(view.getFrame(), message, "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String reason = view.getCbReason().getSelectedItem().toString();
                if (reason.startsWith("Other")) {
                    reason = view.getTxtDetails().getText();
                }

                Date simDate = Util.isoStringToDate(simulatedDateStr);
                model.cancelEnrollment(selectedInscriptionId, simDate, currentCalculatedRefund, reason);
                
                JOptionPane.showMessageDialog(view.getFrame(), 
                    String.format("Enrollment cancelled successfully.\nRefund amount processed: €%.2f", currentCalculatedRefund), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                updateEnrollmentsTable(); 
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view.getFrame(), "Error processing cancellation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}