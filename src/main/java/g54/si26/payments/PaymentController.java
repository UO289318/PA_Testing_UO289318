package g54.si26.payments;

import java.util.List;
import javax.swing.table.TableModel;

import g54.si26.DTOs.EnrollmentRecordDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.SwingUtil;

/**
 * Controller for managing payments and enrollment confirmations.
 * Following MVC pattern: connects View and Model.
 */
public class PaymentController {
    private PaymentModel model;
    private PaymentView view;

    public PaymentController(PaymentModel m, PaymentView v) {
        this.model = m;
        this.view = v;
    }

    /**
     * Initializes the controller: installs event handlers and shows the view.
     */
    public void initController() {
        // Load initial data
        updateEnrollmentGrid();

        // Handle Register Payment button click
        view.getBtnRegisterPayment().addActionListener(e -> SwingUtil.exceptionWrapper(() -> safeRegisterPayment()));
        
        // Handle selection in the grid to pre-fill the amount
        view.getTabEnrollments().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectedEnrollment();
            }
        });
        
        view.getFrame().setVisible(true);
    }

    private void updateSelectedEnrollment() {
        int selectedRow = view.getTabEnrollments().getSelectedRow();
        if (selectedRow != -1) {
            // Fee is in column index 3
            Object feeValue = view.getTabEnrollments().getValueAt(selectedRow, 3);
            view.setAmount(String.valueOf(feeValue));
        }
    }

    private void safeRegisterPayment() {
        int selectedRow = view.getTabEnrollments().getSelectedRow();
        if (selectedRow == -1) {
            throw new ApplicationException("Please select an enrollment from the list.");
        }

        int inscriptionId = (int) view.getTabEnrollments().getValueAt(selectedRow, 0);
        double amount;
        try {
            amount = Double.parseDouble(view.getAmount());
        } catch (NumberFormatException e) {
            throw new ApplicationException("Invalid amount format.");
        }
        
        String paymentDateStr = view.getPaymentDate();
        if (paymentDateStr == null || paymentDateStr.trim().isEmpty()) {
            throw new ApplicationException("Payment date is required (YYYY-MM-DD).");
        }

        // Call the Model to register the payment
        model.registerPayment(inscriptionId, amount, paymentDateStr);
        
        // Refresh grid and clear form on success
        updateEnrollmentGrid();
        view.setAmount("");
        view.setPaymentDate("");
        javax.swing.JOptionPane.showMessageDialog(view.getFrame(), "Payment registered successfully. Enrollment confirmed.");
    }

    /**
     * Updates the grid by asking the Model for data.
     */
    public void updateEnrollmentGrid() {
        List<EnrollmentRecordDTO> enrollments = model.getPendingEnrollments();
        // Columns: Inscription ID, Course, Professional, Fee, Registration Date
        TableModel tmodel = SwingUtil.getTableModelFromPojos(enrollments, new String[] {"inscriptionId", "courseName", "professionalName", "fee", "registrationDate"});
        view.getTabEnrollments().setModel(tmodel);
        
        // Table layout logic
        SwingUtil.autoAdjustColumns(view.getTabEnrollments());
        view.getTabEnrollments().setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
    }
}
