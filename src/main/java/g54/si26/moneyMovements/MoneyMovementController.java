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
                tableModel.addRow(new Object[]{e.getInscriptionId(), e.getCourseName(), e.getProfessionalName(), String.format("%.2f", e.getFee()), e.getNetBalance(), e.getState()});
            }
        } else if (view.getRdInvoices().isSelected()) {
            List<TeacherInvoiceDTO> list = model.getAllInvoices();
            String[] columns = {"ID", "Course", "Teacher", "Total Amount", "Net Balance", "Status"};
            tableModel = new DefaultTableModel(columns, 0);
            for (TeacherInvoiceDTO i : list) {
                tableModel.addRow(new Object[]{i.getInvoiceId(), i.getCourseName(), i.getTeacherName(), String.format("%.2f", i.getTotalAmount()), i.getNetBalance(), i.getStatus()});
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
        
        // Hide ID and Net Balance
        hideColumn(view.getTableMain(), 0); // ID
        if (!view.getRdHistory().isSelected()) {
            hideColumn(view.getTableMain(), 4); // Net Balance
        }
    }

    private void updatePendingTable() {
        DefaultTableModel tableModel;
        if (view.getRdHistory().isSelected()) {
            view.getTablePending().setModel(new DefaultTableModel());
            return;
        }

        if (view.getRdEnrollments().isSelected()) {
            List<EnrollmentRecordDTO> list = model.getEnrollmentsPendingCompensation();
            String[] columns = {"ID", "Professional", "Course", "Amount"};
            tableModel = new DefaultTableModel(columns, 0);
            for (EnrollmentRecordDTO e : list) {
                tableModel.addRow(new Object[]{e.getInscriptionId(), e.getProfessionalName(), e.getCourseName(), String.format("%.2f", e.getNetBalance())});
            }
        } else {
            List<TeacherInvoiceDTO> list = model.getInvoicesPendingCompensation();
            String[] columns = {"ID", "Teacher", "Course", "Amount"};
            tableModel = new DefaultTableModel(columns, 0);
            for (TeacherInvoiceDTO i : list) {
                tableModel.addRow(new Object[]{i.getInvoiceId(), i.getTeacherName(), i.getCourseName(), String.format("%.2f", i.getNetBalance())});
            }
        }
        view.getTablePending().setModel(tableModel);
        hideColumn(view.getTablePending(), 0); // ID
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
        hideColumn(view.getTableHistory(), 0); // ID
    }

    private void hideColumn(javax.swing.JTable table, int index) {
        if (table.getColumnCount() > index) {
            table.getColumnModel().getColumn(index).setMinWidth(0);
            table.getColumnModel().getColumn(index).setMaxWidth(0);
            table.getColumnModel().getColumn(index).setPreferredWidth(0);
        }
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

        List<String> errors = new java.util.ArrayList<>();
        List<String> warnings = new java.util.ArrayList<>();

        try {
            double amount = 0;
            String amountStr = view.getTxtAmount().getText().trim().replace(",", ".");
            if (amountStr.isEmpty()) {
                errors.add("Amount is required.");
            } else {
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException nfe) {
                    errors.add("Amount must be a valid number.");
                }
            }
            
            if (Math.abs(amount) < 0.001) {
                errors.add("Amount cannot be zero.");
            }

            String dateStr = view.getTxtDate().getText().trim();
            int id = (int) view.getTableMain().getValueAt(row, 0);

            // DATE VALIDATION
            LocalDate moveDate = null;
            if (dateStr.isEmpty()) {
                errors.add("Date is required.");
            } else {
                try {
                    moveDate = LocalDate.parse(dateStr, FORMATTER);
                    if (simulatedDate != null && !simulatedDate.isEmpty()) {
                        LocalDate sysDate = LocalDate.parse(simulatedDate, FORMATTER);
                        if (moveDate.isAfter(sysDate)) {
                            errors.add("Movement date (" + dateStr + ") cannot be in the future (System date: " + simulatedDate + ").");
                        }
                    }
                } catch (Exception ex) {
                    errors.add("Invalid date format. Use YYYY-MM-DD.");
                }
            }

            if (view.getRdEnrollments().isSelected()) {
                List<EnrollmentRecordDTO> all = model.getAllEnrollments();
                EnrollmentRecordDTO selected = all.stream().filter(e -> e.getInscriptionId() == id).findFirst().orElse(null);
                
                if (selected != null) {
                    if (amount > 0) {
                        // Positive payment logic
                        if (moveDate != null) {
                            String rDateStr = selected.getRegistrationDate();
                            if (rDateStr != null) {
                                if (rDateStr.length() > 10) rDateStr = rDateStr.substring(0, 10);
                                LocalDate regDate = LocalDate.parse(rDateStr, FORMATTER);
                                
                                if (moveDate.isBefore(regDate)) {
                                    errors.add("Movement date cannot be before inscription date (" + rDateStr + ").");
                                }
                                
                                if (Util.isAfterTwoWorkingDays(regDate, moveDate)) {
                                     errors.add("Movement is more than 2 working days after inscription date (" + rDateStr + ").");
                                }
                            }
                        }
                        
                        // Overpayment warning: TotalPaid + amount > Fee
                        if (selected.getNetBalance() + amount > selected.getFee() + 0.001) {
                            warnings.add("This movement will result in an overpayment for the professional.");
                        }
                    } else if (amount < 0) {
                        // Negative movement (compensation)
                        double overpayment = selected.getNetBalance() - selected.getFee();
                        if (overpayment <= 0.001) {
                            errors.add("Negative movements are only allowed for compensation (when an overpayment exists). Current overpayment is 0.00.");
                        } else {
                            double absAmount = Math.abs(amount);
                            if (absAmount > overpayment + 0.001) {
                                errors.add(String.format("Compensation movement (%.2f) cannot be greater than the overpaid amount (%.2f).", absAmount, overpayment));
                            } else if (absAmount < overpayment - 0.001) {
                                warnings.add(String.format("This compensation movement (%.2f) is lower than the required amount to fully compensate (%.2f).", absAmount, overpayment));
                            }
                        }
                    }
                } else {
                    errors.add("Selected enrollment not found in database.");
                }
            } else {
                // Invoices validation
                List<TeacherInvoiceDTO> all = model.getAllInvoices();
                TeacherInvoiceDTO selected = all.stream().filter(i -> i.getInvoiceId() == id).findFirst().orElse(null);
                
                if (selected != null) {
                    double currentNetPaid = selected.getNetBalance(); // Sum of movements (usually negative for expenses)
                    double totalInvoice = selected.getTotalAmount();
                    
                    if (amount > 0) {
                        // Positive movement for an invoice (income/refund from teacher)
                        if (Math.abs(currentNetPaid) <= totalInvoice + 0.001) {
                            errors.add("Cannot register a positive movement (income) for an invoice unless it has been overpaid to the teacher.");
                        }
                    }
                    
                    // Overcharge warning (Expenses are negative): |currentNetPaid + amount| > totalInvoice
                    if (Math.abs(currentNetPaid + amount) > totalInvoice + 0.001) {
                        warnings.add("This movement will result in an overcharge (payment exceeding the invoice amount).");
                    }
                } else {
                    errors.add("Selected invoice not found in database.");
                }
            }

            // Display errors if any
            if (!errors.isEmpty()) {
                StringBuilder sb = new StringBuilder("The following errors were detected:\n\n");
                for (String err : errors) sb.append(" - ").append(err).append("\n");
                if (!warnings.isEmpty()) {
                    sb.append("\nAlso, the following warnings were detected:\n\n");
                    for (String warn : warnings) sb.append(" - ").append(warn).append("\n");
                }
                JOptionPane.showMessageDialog(view.getFrame(), sb.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Display warnings if no errors
            if (!warnings.isEmpty()) {
                StringBuilder sb = new StringBuilder("The following warnings were detected:\n\n");
                for (String warn : warnings) sb.append(" - ").append(warn).append("\n");
                sb.append("\nDo you want to proceed anyway?");
                int choice = JOptionPane.showConfirmDialog(view.getFrame(), sb.toString(), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) return;
            }

            model.registerMovement(view.getRdEnrollments().isSelected() ? id : null, 
                                   view.getRdInvoices().isSelected() ? id : null, 
                                   amount, dateStr, "EXECUTED");

            updateTables();
            JOptionPane.showMessageDialog(view.getFrame(), "Movement registered successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view.getFrame(), "Unexpected error: " + e.getMessage());
        }
    }
}
