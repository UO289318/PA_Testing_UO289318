package g54.si26.teacherpayments;

import java.util.List;
import javax.swing.table.TableModel;

import g54.si26.DTOs.TeacherInvoiceDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.SwingUtil;

/**
 * Controller for managing teacher payments.
 */
public class TeacherPaymentController {
    private TeacherPaymentModel model;
    private TeacherPaymentView view;

    public TeacherPaymentController(TeacherPaymentModel m, TeacherPaymentView v) {
        this.model = m;
        this.view = v;
    }

    public void initController() {
        updateInvoiceGrid();

        view.getBtnRegisterTransfer().addActionListener(e -> SwingUtil.exceptionWrapper(() -> safeRegisterTransfer()));
        
        view.getTabInvoices().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectedInvoice();
            }
        });
        
        view.getFrame().setVisible(true);
    }

    private void updateSelectedInvoice() {
        int selectedRow = view.getTabInvoices().getSelectedRow();
        if (selectedRow != -1) {
            // Pending amount calculation: Total (index 3) - Paid (index 5)
            double total = (double) view.getTabInvoices().getValueAt(selectedRow, 3);
            double paid = (double) view.getTabInvoices().getValueAt(selectedRow, 5);
            view.setAmount(String.valueOf(total - paid));
        }
    }

    private void safeRegisterTransfer() {
        int selectedRow = view.getTabInvoices().getSelectedRow();
        if (selectedRow == -1) {
            throw new ApplicationException("Please select an invoice from the list.");
        }

        int invoiceId = (int) view.getTabInvoices().getValueAt(selectedRow, 0);
        double amount;
        try {
            amount = Double.parseDouble(view.getAmount());
        } catch (NumberFormatException e) {
            throw new ApplicationException("Invalid amount format.");
        }
        
        String transferDateStr = view.getTransferDate();
        if (transferDateStr == null || transferDateStr.trim().isEmpty()) {
            throw new ApplicationException("Transfer date is required (YYYY-MM-DD).");
        }

        model.registerTeacherPayment(invoiceId, amount, transferDateStr);
        
        updateInvoiceGrid();
        view.setAmount("");
        view.setTransferDate("");
        javax.swing.JOptionPane.showMessageDialog(view.getFrame(), "Transfer recorded successfully.");
    }

    public void updateInvoiceGrid() {
        List<TeacherInvoiceDTO> invoices = model.getPendingInvoices();
        String[] columnProperties = {"invoiceId", "teacherName", "courseName", "totalAmount", "invoiceDate", "amountPaid"};
        TableModel tmodel = SwingUtil.getTableModelFromPojos(invoices, columnProperties);
        view.getTabInvoices().setModel(tmodel);
        
        SwingUtil.autoAdjustColumns(view.getTabInvoices());
        view.getTabInvoices().setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
    }
}