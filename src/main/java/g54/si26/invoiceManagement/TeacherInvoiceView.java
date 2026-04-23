package g54.si26.invoiceManagement;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import g54.si26.DTOs.TeacherInvoiceDTO;

public class TeacherInvoiceView {

    private JFrame frame;
    
    // Inputs (Ahora el Neto y el Porcentaje de IVA se introducen a mano)
    private JComboBox<TeacherInvoiceDTO> cbTeacherCourse;
    private JTextField txtDate;
    private JTextField txtNet;
    private JTextField txtVat;
    
    // Auto-calculated outputs (Ahora el Total y la cantidad de IVA se calculan solos)
    private JTextField txtVatAmount; 
    private JTextField txtTotal; 
    private JTextField txtSystemCommitment;
    // Messages
    private JLabel lblWarningIcon;
    private JLabel lblMessage;
    private JPanel panelMessage;

    // Buttons
    private JButton btnValidate;
    private JButton btnRegister;
    private JButton btnRequestRectifying;
    private JButton btnUpdateCommitment;

    public TeacherInvoiceView() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("COIIPA - Teacher Invoice Management");
        frame.setBounds(100, 100, 850, 450); 
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));

        JPanel panelCenter = new JPanel(new GridLayout(1, 2, 15, 0));
        panelCenter.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        // -- Panel Izquierdo: Detalles --
        JPanel panelDetails = new JPanel(new GridLayout(6, 2, 5, 15)); 
        panelDetails.setBorder(BorderFactory.createTitledBorder("Invoice Details"));

        panelDetails.add(new JLabel("Teacher Name:"));
        cbTeacherCourse = new JComboBox<>();
        cbTeacherCourse.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TeacherInvoiceDTO) {
                    TeacherInvoiceDTO dto = (TeacherInvoiceDTO) value;
                    setText(dto.getTeacherName() + " (" + dto.getCourseName() + ")");
                }
                return this;
            }
        });
        panelDetails.add(cbTeacherCourse);

        panelDetails.add(new JLabel("Date (YYYY-MM-DD):"));
        txtDate = new JTextField();
        panelDetails.add(txtDate);

        // INPUT: El usuario mete el Neto
        panelDetails.add(new JLabel("Net Amount (€):"));
        txtNet = new JTextField();
        panelDetails.add(txtNet);

        // INPUT: El usuario mete el porcentaje de IVA
        panelDetails.add(new JLabel("VAT (%):")); 
        txtVat = new JTextField();
        panelDetails.add(txtVat);

        // OUTPUT: El sistema calcula el IVA en euros
        panelDetails.add(new JLabel("Calculated VAT Amount (€):"));
        txtVatAmount = new JTextField();
        txtVatAmount.setEditable(false);
        txtVatAmount.setBackground(Color.WHITE);
        txtVatAmount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelDetails.add(txtVatAmount);

        // OUTPUT: El sistema calcula el Total sumando Neto + IVA
        panelDetails.add(new JLabel("Calculated Total Amount (€):"));
        txtTotal = new JTextField();
        txtTotal.setEditable(false);
        txtTotal.setBackground(Color.WHITE);
        txtTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelDetails.add(txtTotal);

        panelCenter.add(panelDetails);

        // -- Panel Derecho: Verificación --
        JPanel panelVerification = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 20));
        panelVerification.setBorder(BorderFactory.createTitledBorder("Verification"));

        JPanel pnlCommitment = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlCommitment.add(new JLabel("System Initial Payment Commitment (Net €):")); 
        txtSystemCommitment = new JTextField(10);
        txtSystemCommitment.setEditable(false);
        txtSystemCommitment.setBackground(Color.WHITE);
        pnlCommitment.add(txtSystemCommitment);
        
        panelVerification.add(pnlCommitment);

        panelMessage = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblWarningIcon = new JLabel("!"); // Cambiado para que no salga el cuadrado roto en tu Windows
        lblWarningIcon.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWarningIcon.setForeground(Color.RED);
        
        lblMessage = new JLabel("<html><body style='width: 250px'>...</body></html>");
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        panelMessage.add(lblWarningIcon);
        panelMessage.add(lblMessage);
        panelMessage.setVisible(false);
        
        panelVerification.add(panelMessage);
        panelCenter.add(panelVerification);

        frame.getContentPane().add(panelCenter, BorderLayout.CENTER);

        // -- SOUTH: Botones --
        JPanel panelSouth = new JPanel(new GridLayout(2, 1, 5, 5));
        panelSouth.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JPanel panelBtnTop = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnValidate = new JButton("Validate and Compare");
        panelBtnTop.add(btnValidate);

        JPanel panelBtnBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnRegister = new JButton("Register Invoice");
        btnRegister.setEnabled(false);
        
        btnRequestRectifying = new JButton("Request Rectifying Invoice");
        btnRequestRectifying.setEnabled(false);
        
        btnUpdateCommitment = new JButton("Update Initial Commitment");
        btnUpdateCommitment.setEnabled(false);

        panelBtnBottom.add(btnRegister);
        panelBtnBottom.add(btnRequestRectifying);
        panelBtnBottom.add(btnUpdateCommitment);

        panelSouth.add(panelBtnTop);
        panelSouth.add(panelBtnBottom);

        frame.getContentPane().add(panelSouth, BorderLayout.SOUTH);
    }

    // --- Getters ---
    public JFrame getFrame() { return frame; }
    public JComboBox<TeacherInvoiceDTO> getCbTeacherCourse() { return cbTeacherCourse; }
    public JTextField getTxtDate() { return txtDate; }
    public JTextField getTxtNet() { return txtNet; }
    public JTextField getTxtVat() { return txtVat; }
    public JTextField getTxtTotal() { return txtTotal; }
    public JTextField getTxtVatAmount() { return txtVatAmount; } 
    public JTextField getTxtSystemCommitment() { return txtSystemCommitment; }
    
    public JLabel getLblWarningIcon() { return lblWarningIcon; }
    public JLabel getLblMessage() { return lblMessage; }
    public JPanel getPanelMessage() { return panelMessage; }

    public JButton getBtnValidate() { return btnValidate; }
    public JButton getBtnRegister() { return btnRegister; }
    public JButton getBtnRequestRectifying() { return btnRequestRectifying; }
    public JButton getBtnUpdateCommitment() { return btnUpdateCommitment; }
}