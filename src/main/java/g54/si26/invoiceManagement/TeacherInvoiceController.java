package g54.si26.invoiceManagement;

import java.awt.Color;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import g54.si26.DTOs.TeacherInvoiceDTO;
import g54.si26.utils.SwingUtil;
import g54.si26.utils.ApplicationException;

public class TeacherInvoiceController {

    private TeacherInvoiceModel model;
    private TeacherInvoiceView view;
    private String simulatedDate; // Simulated system date

    public TeacherInvoiceController(TeacherInvoiceModel m, TeacherInvoiceView v) {
        this.model = m;
        this.view = v;
    }

    public void setSimulatedDate(String date) {
        this.simulatedDate = date;
    }

    public void initController() {
        loadComboBox();
        setupAutoCalculate(); // Inicializamos el cálculo automático

        view.getCbTeacherCourse().addActionListener(e -> {
            resetValidation();
            TeacherInvoiceDTO selected = (TeacherInvoiceDTO) view.getCbTeacherCourse().getSelectedItem();
            if (selected != null) {
                view.getTxtSystemCommitment().setText(String.format(java.util.Locale.US, "%.2f", selected.getTotalAmount()));
            }
        });

        view.getBtnValidate().addActionListener(e -> SwingUtil.exceptionWrapper(() -> validateInvoice()));
        view.getBtnRegister().addActionListener(e -> SwingUtil.exceptionWrapper(() -> processInvoiceRegistration(false)));
        view.getBtnUpdateCommitment().addActionListener(e -> SwingUtil.exceptionWrapper(() -> processInvoiceRegistration(true)));

        view.getBtnRequestRectifying().addActionListener(e -> {
            JOptionPane.showMessageDialog(view.getFrame(), 
                "An email request will be sent to the teacher to emit a rectifying invoice.", 
                "Rectifying Requested", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        });

        view.getFrame().setVisible(true);
    }

    // Método para calcular el total automáticamente cuando se escribe en Net o VAT
    private void setupAutoCalculate() {
        DocumentListener calcListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { calculateTotal(); }
            public void removeUpdate(DocumentEvent e) { calculateTotal(); }
            public void changedUpdate(DocumentEvent e) { calculateTotal(); }
        };

        view.getTxtNet().getDocument().addDocumentListener(calcListener);
        view.getTxtVat().getDocument().addDocumentListener(calcListener);
    }

    private void calculateTotal() {
        try {
            double net = Double.parseDouble(view.getTxtNet().getText().replace(",", "."));
            double vatPct = Double.parseDouble(view.getTxtVat().getText().replace(",", "."));
            double vatAmount = net * (vatPct / 100.0);
            double total = net + vatAmount;
            
            view.getTxtTotal().setText(String.format(java.util.Locale.US, "%.2f", total));
        } catch (NumberFormatException e) {
            view.getTxtTotal().setText(""); // Si borran o ponen texto inválido, se vacía
        }
    }

    private void loadComboBox() {
        view.getCbTeacherCourse().removeAllItems();
        List<TeacherInvoiceDTO> pending = model.getPendingInvoicesList();
        for (TeacherInvoiceDTO dto : pending) {
            view.getCbTeacherCourse().addItem(dto);
        }
    }

    private void resetValidation() {
        view.getPanelMessage().setVisible(false);
        view.getBtnRegister().setEnabled(false);
        view.getBtnRequestRectifying().setEnabled(false);
        view.getBtnUpdateCommitment().setEnabled(false);
    }

    private void resetForm() {
        view.getTxtDate().setText("");
        view.getTxtNet().setText("");
        view.getTxtVat().setText("");
        // El total se vacía solo gracias al DocumentListener
        resetValidation();
        loadComboBox(); 
    }

    private void validateInvoice() {
        TeacherInvoiceDTO selected = (TeacherInvoiceDTO) view.getCbTeacherCourse().getSelectedItem();
        if (selected == null) throw new ApplicationException("Please select a teacher/course.");

        try {
            // CAMBIO: Ahora validamos usando el Net Amount
            double inputNet = Double.parseDouble(view.getTxtNet().getText().replace(",", "."));
            double systemCommitment = selected.getTotalAmount(); 

            view.getPanelMessage().setVisible(true);

            // Tolerancia de 1 céntimo 
            if (Math.abs(inputNet - systemCommitment) <= 0.01) {
                view.getLblWarningIcon().setText("✓");
                view.getLblWarningIcon().setForeground(new Color(0, 153, 51));
                view.getLblMessage().setText("<html>Net Amount matches perfectly.<br>Ready to register.</html>");
                
                view.getBtnRegister().setEnabled(true);
                view.getBtnRequestRectifying().setEnabled(false);
                view.getBtnUpdateCommitment().setEnabled(false);
            } else {
                view.getLblWarningIcon().setText("⚠");
                view.getLblWarningIcon().setForeground(Color.RED);
                view.getLblMessage().setText(String.format(
                    java.util.Locale.US,
                    "<html><b>Difference Detected!</b> (Net: %.2f != Sys: %.2f)<br>Please resolve the discrepancy.</html>", 
                    inputNet, systemCommitment));
                
                view.getBtnRegister().setEnabled(false);
                view.getBtnRequestRectifying().setEnabled(true);
                view.getBtnUpdateCommitment().setEnabled(true);
            }
        } catch (NumberFormatException ex) {
            throw new ApplicationException("Please enter valid numeric formats for Net Amount and VAT.");
        }
    }

    private void processInvoiceRegistration(boolean updateCommitmentFirst) {
        TeacherInvoiceDTO selected = (TeacherInvoiceDTO) view.getCbTeacherCourse().getSelectedItem();
        if (selected == null) return;

        String dateRaw = view.getTxtDate().getText().trim();
        if (dateRaw.isEmpty()) {
            throw new ApplicationException("Date is required.");
        }

        // Validate format YYYY-MM-DD strictly (4 digits year, 2 month, 2 day)
        if (!dateRaw.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ApplicationException("Invalid date format. Use YYYY-MM-DD (e.g. 2026-05-01).");
        }

        try {
            java.util.Date invoiceDate = g54.si26.utils.Util.isoStringToDate(dateRaw);
            if (this.simulatedDate != null && !this.simulatedDate.isEmpty()) {
                java.util.Date sysDate = g54.si26.utils.Util.isoStringToDate(this.simulatedDate);
                if (invoiceDate.after(sysDate)) {
                    throw new ApplicationException("Invoice date cannot be in the future (Simulated system date: " + this.simulatedDate + ").");
                }
            }
        } catch (ApplicationException ae) {
            throw ae;
        } catch (Exception ex) {
            throw new ApplicationException("Invalid date format or values. Use YYYY-MM-DD (e.g. 2026-05-01).");
        }
        String dateDB = dateRaw;

        double net = Double.parseDouble(view.getTxtNet().getText().replace(",", "."));
        double vatPct = Double.parseDouble(view.getTxtVat().getText().replace(",", "."));
        
        // Calculamos la cantidad en Euros del IVA y el Total
        double vatAmount = net * (vatPct / 100.0);
        double total = net + vatAmount;

        int[] ids = model.getIds(selected.getTeacherName(), selected.getCourseName());
        int teacherId = ids[0];
        int actionId = ids[1];

        if (teacherId == -1 || actionId == -1) {
            throw new ApplicationException("Error: Could not link the selected teacher/course to the database.");
        }

        if (updateCommitmentFirst) {
            // Actualizamos el compromiso con la cantidad NETA
            model.updateCommitment(teacherId, actionId, net);
        }

        // Guardamos en la base de datos la cantidad monetaria de IVA para mantener consistencia 
        model.registerInvoice(teacherId, actionId, dateDB, net, vatAmount, total);

        JOptionPane.showMessageDialog(view.getFrame(), "Invoice registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        resetForm();
    }
}