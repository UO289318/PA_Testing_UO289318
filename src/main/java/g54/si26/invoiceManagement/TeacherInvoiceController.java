package g54.si26.invoiceManagement;

import java.awt.Color;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
    private String simulatedDateStr; 

    public TeacherInvoiceController(TeacherInvoiceModel m, TeacherInvoiceView v) {
        this.model = m;
        this.view = v;
    }

    public void setSimulatedDate(String date) {
        this.simulatedDateStr = date;
    }

    public void initController() {
        loadComboBox();
        setupAutoCalculate();

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

    // Ahora escucha los campos Net y VAT para calcular el Total
    private void setupAutoCalculate() {
        DocumentListener calcListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { calculateBreakdown(); }
            public void removeUpdate(DocumentEvent e) { calculateBreakdown(); }
            public void changedUpdate(DocumentEvent e) { calculateBreakdown(); }
        };

        view.getTxtNet().getDocument().addDocumentListener(calcListener);
        view.getTxtVat().getDocument().addDocumentListener(calcListener);
    }

    // Calcula el Total y la cantidad de IVA a partir del Neto y el Porcentaje
    private void calculateBreakdown() {
        try {
            double net = Double.parseDouble(view.getTxtNet().getText().replace(",", "."));
            double vatPct = Double.parseDouble(view.getTxtVat().getText().replace(",", "."));
            
            // Fórmula: Cantidad IVA = Neto * (%IVA/100) | Total = Neto + IVA
            double vatAmount = net * (vatPct / 100.0);
            double total = net + vatAmount;
            
            view.getTxtVatAmount().setText(String.format(java.util.Locale.US, "%.2f", vatAmount));
            view.getTxtTotal().setText(String.format(java.util.Locale.US, "%.2f", total));
        } catch (NumberFormatException e) {
            // Si borran, limpian los desgloses
            view.getTxtVatAmount().setText(""); 
            view.getTxtTotal().setText(""); 
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
        // Total y VatAmount se limpian solos gracias al DocumentListener
        resetValidation();
        loadComboBox(); 
    }

    private void validateInvoice() {
        TeacherInvoiceDTO selected = (TeacherInvoiceDTO) view.getCbTeacherCourse().getSelectedItem();
        if (selected == null) throw new ApplicationException("Please select a teacher/course.");

        try {
            // Validamos contra el Neto que el usuario ha introducido
            double inputNet = Double.parseDouble(view.getTxtNet().getText().replace(",", "."));
            double systemCommitment = selected.getTotalAmount(); 

            view.getPanelMessage().setVisible(true);

            if (Math.abs(inputNet - systemCommitment) <= 0.01) {
                view.getLblWarningIcon().setText("✓");
                view.getLblWarningIcon().setForeground(new Color(0, 153, 51));
                view.getLblMessage().setText("<html>Net Amount matches perfectly.<br>Ready to register.</html>");
                
                view.getBtnRegister().setEnabled(true);
                view.getBtnRequestRectifying().setEnabled(false);
                view.getBtnUpdateCommitment().setEnabled(false);
            } else {
                view.getLblWarningIcon().setText("X"); // Usamos "X" para asegurar compatibilidad en Windows
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

        String dateDB;
        try {
            LocalDate validDate = LocalDate.parse(dateRaw);
            LocalDate simDate = LocalDate.parse(simulatedDateStr.substring(0, 10));
            
            // Permitimos facturas pasadas o de hoy, pero no del futuro
            if (validDate.isAfter(simDate)) {
                throw new ApplicationException("The invoice date cannot be in the future (after " + simDate + ").");
            }
            dateDB = validDate.toString(); 
        } catch (DateTimeParseException ex) {
            throw new ApplicationException("Invalid or non-existent date. Please use a valid YYYY-MM-DD date.");
        }

        // Leemos el Neto y el IVA
        double net = Double.parseDouble(view.getTxtNet().getText().replace(",", "."));
        double vatPct = Double.parseDouble(view.getTxtVat().getText().replace(",", "."));
        
        // Recalculamos para la base de datos
        double vatAmount = net * (vatPct / 100.0);
        double total = net + vatAmount;

        int[] ids = model.getIds(selected.getTeacherName(), selected.getCourseName());
        int teacherId = ids[0];
        int actionId = ids[1];

        if (teacherId == -1 || actionId == -1) {
            throw new ApplicationException("Error: Could not link the selected teacher/course to the database.");
        }

        if (updateCommitmentFirst) {
            model.updateCommitment(teacherId, actionId, net);
        }

        model.registerInvoice(teacherId, actionId, dateDB, net, vatAmount, total);

        JOptionPane.showMessageDialog(view.getFrame(), "Invoice registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        resetForm();
    }
}