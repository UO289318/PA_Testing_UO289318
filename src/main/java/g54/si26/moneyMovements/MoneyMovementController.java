package g54.si26.moneyMovements;

import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import g54.si26.DTOs.EnrollmentRecordDTO;
import g54.si26.DTOs.TeacherInvoiceDTO;
import g54.si26.DTOs.MoneyMovementDTO;
import g54.si26.utils.Util;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MoneyMovementController {
    private MoneyMovementModel model;
    private MoneyMovementView view;
    private String simulatedDate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public MoneyMovementController(MoneyMovementModel model, MoneyMovementView view) {
        this.model = model;
        this.view = view;
    }

    public void setSimulatedDate(String simulatedDate) {
        this.simulatedDate = simulatedDate;
    }

    public void initController() {
        view.getRdEnrollments().addActionListener(e -> updateTables());
        view.getRdInvoices().addActionListener(e -> updateTables());
        view.getRdHistory().addActionListener(e -> updateTables());
        
        view.getTableMain().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateHistoryTable();
        });

        view.getBtnRegister().addActionListener(e -> registerMovement());

        updateTables();
        view.getFrame().setVisible(true);
    }

    private void updateTables() {
        updateMainTable();
        updatePendingTable();
        updateHistoryTable();
    }

    private void updateMainTable() {
        DefaultTableModel tableModel;
        if (view.getRdEnrollments().isSelected()) {
            List<EnrollmentRecordDTO> list = model.getAllEnrollments();
            String[] columns = {"ID", "Course", "Professional", "Fee", "Net Balance", "State"};
            tableModel = new DefaultTableModel(columns, 0);
            for (EnrollmentRecordDTO e : list) {
                tableModel.addRow(new Object[]{e.getInscriptionId(), e.getCourseName(), e.getProfessionalName(), e.getFee(), e.getNetBalance(), e.getState()});
            }
        } else if (view.getRdInvoices().isSelected()) {
            List<TeacherInvoiceDTO> list = model.getAllInvoices();
            String[] columns = {"ID", "Course", "Teacher", "Total Amount", "Net Balance", "Status"};
            tableModel = new DefaultTableModel(columns, 0);
            for (TeacherInvoiceDTO i : list) {
                tableModel.addRow(new Object[]{i.getInvoiceId(), i.getCourseName(), i.getTeacherName(), i.getTotalAmount(), i.getNetBalance(), i.getStatus()});
            }
        } else {
            // Full History
            List<MoneyMovementDTO> list = model.getAllMovements();
            String[] columns = {"ID", "Amount", "Date", "Status", "Related To"};
            tableModel = new DefaultTableModel(columns, 0);
            for (MoneyMovementDTO m : list) {
                tableModel.addRow(new Object[]{m.getMovementId(), m.getAmount(), m.getMovementDate(), m.getStatus(), m.getRelatedTo()});
            }
        }
        view.getTableMain().setModel(tableModel);
    }

    private void updatePendingTable() {
        DefaultTableModel tableModel;
        if (view.getRdHistory().isSelected()) {
            view.getTablePending().setModel(new DefaultTableModel());
            return;
        }

        if (view.getRdEnrollments().isSelected()) {
            List<EnrollmentRecordDTO> list = model.getEnrollmentsPendingCompensation();
            String[] columns = {"ID", "Professional", "Net Balance", "Fee"};
            tableModel = new DefaultTableModel(columns, 0);
            for (EnrollmentRecordDTO e : list) {
                tableModel.addRow(new Object[]{e.getInscriptionId(), e.getProfessionalName(), e.getNetBalance(), e.getFee()});
            }
        } else {
            List<TeacherInvoiceDTO> list = model.getInvoicesPendingCompensation();
            String[] columns = {"ID", "Teacher", "Net Balance", "Total Amount"};
            tableModel = new DefaultTableModel(columns, 0);
            for (TeacherInvoiceDTO i : list) {
                tableModel.addRow(new Object[]{i.getInvoiceId(), i.getTeacherName(), i.getNetBalance(), i.getTotalAmount()});
            }
        }
        view.getTablePending().setModel(tableModel);
    }

    private void updateHistoryTable() {
        int row = view.getTableMain().getSelectedRow();
        if (row == -1) {
            view.getTableHistory().setModel(new DefaultTableModel());
            return;
        }

        if (view.getRdHistory().isSelected()) {
            view.getTableHistory().setModel(new DefaultTableModel());
            return;
        }

        int id = (int) view.getTableMain().getValueAt(row, 0);
        List<MoneyMovementDTO> list;
        if (view.getRdEnrollments().isSelected()) list = model.getMovementsByInscription(id);
        else list = model.getMovementsByInvoice(id);

        String[] columns = {"ID", "Amount", "Date", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        for (MoneyMovementDTO m : list) {
            tableModel.addRow(new Object[]{m.getMovementId(), m.getAmount(), m.getMovementDate(), m.getStatus()});
        }
        view.getTableHistory().setModel(tableModel);
    }

    private void registerMovement() {
        if (view.getRdHistory().isSelected()) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select the 'Enrollments' or 'Invoices' tab to register a new movement.");
            return;
        }

        int row = view.getTableMain().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select a record from the top table.");
            return;
        }

        try {
            double amount = Double.parseDouble(view.getTxtAmount().getText());
            String dateStr = view.getTxtDate().getText();
            int id = (int) view.getTableMain().getValueAt(row, 0);

            // DATE VALIDATION
            LocalDate moveDate = LocalDate.parse(dateStr, FORMATTER);
            LocalDate sysDate = LocalDate.parse(simulatedDate, FORMATTER);

            if (moveDate.isAfter(sysDate)) {
                JOptionPane.showMessageDialog(view.getFrame(), "Error: Movement date (" + dateStr + ") cannot be in the future (System date: " + simulatedDate + ").");
                return;
            }

            if (view.getRdEnrollments().isSelected()) {
                // Find registration date in DTO
                List<EnrollmentRecordDTO> all = model.getAllEnrollments();
                EnrollmentRecordDTO selected = all.stream().filter(e -> e.getInscriptionId() == id).findFirst().orElse(null);
                
                if (selected != null && amount > 0) {
                    String rDateStr = selected.getRegistrationDate();
                    if (rDateStr.length() > 10) rDateStr = rDateStr.substring(0, 10);
                    LocalDate regDate = LocalDate.parse(rDateStr, FORMATTER);
                    
                    if (moveDate.isBefore(regDate)) {
                        JOptionPane.showMessageDialog(view.getFrame(), "Error: Movement date cannot be before inscription date (" + rDateStr + ").");
                        return;
                    }
                    
                    if (Util.isAfterTwoWorkingDays(regDate, moveDate)) {
                         JOptionPane.showMessageDialog(view.getFrame(), "Error: Movement is more than 2 working days after inscription date (" + rDateStr + ").");
                         return;
                    }
                }

                model.registerMovement(id, null, amount, dateStr, "EXECUTED");
            } else {
                // Invoices validation
                double netBalance = (double) view.getTableMain().getValueAt(row, 4);
                double totalAmount = (double) view.getTableMain().getValueAt(row, 3);
                
                if (amount > 0) {
                    if (Math.abs(netBalance) <= totalAmount + 0.001) {
                        JOptionPane.showMessageDialog(view.getFrame(), "Error: Cannot register a positive movement (income) for an invoice unless it has been overpaid.");
                        return;
                    }
                }
                
                model.registerMovement(null, id, amount, dateStr, "EXECUTED");
            }

            updateTables();
            JOptionPane.showMessageDialog(view.getFrame(), "Movement registered successfully.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view.getFrame(), "Error: " + e.getMessage());
        }
    }
}
