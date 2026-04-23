package g54.si26.registerTeacher;

import javax.swing.*;
import java.awt.*;
import net.miginfocom.swing.MigLayout;

public class RegisterTeacherView {

    private JFrame frame;
    private JTextField txtName;
    private JTextField txtFiscalId;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JButton btnSave;
    private JButton btnCancel;

    public RegisterTeacherView() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Register New Teacher");
        frame.setBounds(100, 100, 400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new MigLayout("", "[label][grow]", "[][][][][grow][]"));

        JLabel lblName = new JLabel("Name:");
        frame.getContentPane().add(lblName, "cell 0 0,alignx trailing");
        txtName = new JTextField();
        frame.getContentPane().add(txtName, "cell 1 0,growx");

        JLabel lblFiscalId = new JLabel("Fiscal ID:");
        frame.getContentPane().add(lblFiscalId, "cell 0 1,alignx trailing");
        txtFiscalId = new JTextField();
        frame.getContentPane().add(txtFiscalId, "cell 1 1,growx");

        JLabel lblEmail = new JLabel("Email:");
        frame.getContentPane().add(lblEmail, "cell 0 2,alignx trailing");
        txtEmail = new JTextField();
        frame.getContentPane().add(txtEmail, "cell 1 2,growx");

        JLabel lblPhone = new JLabel("Phone:");
        frame.getContentPane().add(lblPhone, "cell 0 3,alignx trailing");
        txtPhone = new JTextField();
        frame.getContentPane().add(txtPhone, "cell 1 3,growx");

        btnSave = new JButton("Save");
        frame.getContentPane().add(btnSave, "flowx,cell 1 5,alignx right");

        btnCancel = new JButton("Cancel");
        frame.getContentPane().add(btnCancel, "cell 1 5,alignx right");
    }

    public JFrame getFrame() { return frame; }
    public String getName() { return txtName.getText(); }
    public String getFiscalId() { return txtFiscalId.getText(); }
    public String getEmail() { return txtEmail.getText(); }
    public String getPhone() { return txtPhone.getText(); }
    public JButton getBtnSave() { return btnSave; }
    public JButton getBtnCancel() { return btnCancel; }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
