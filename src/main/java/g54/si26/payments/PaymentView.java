package g54.si26.payments;

import javax.swing.JFrame;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.Dimension;

/**
 * View for the "Register Payments" user story.
 * Separates the UI definition from the controller logic.
 */
public class PaymentView {

    private JFrame frame;
    private JTable tabEnrollments;
    private JTextField txtAmount;
    private JTextField txtPaymentDate;
    private JButton btnRegisterPayment;

    public PaymentView() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Register Payments - Secretary");
        frame.setName("PaymentView");
        frame.setBounds(100, 100, 900, 600);
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new MigLayout("", "[grow]", "[grow][grow]"));

        Font mainFont = new Font("Arial", Font.PLAIN, 14);

        // Top Half: List of Inscriptions
        JPanel pnlList = new JPanel(new MigLayout("", "[grow]", "[grow]"));
        pnlList.setBorder(new TitledBorder(null, "Pending Enrollments (Active Courses)", TitledBorder.LEADING, TitledBorder.TOP, mainFont));
        
        tabEnrollments = new JTable();
        tabEnrollments.setName("tabEnrollments");
        tabEnrollments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabEnrollments.setDefaultEditor(Object.class, null); // Read-only
        tabEnrollments.setFont(mainFont);
        tabEnrollments.setRowHeight(25);
        // Force the table to use all horizontal space
        tabEnrollments.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(tabEnrollments);
        pnlList.add(scrollPane, "grow");
        
        frame.getContentPane().add(pnlList, "cell 0 0,grow");

        // Bottom Half: Register New Payment Form
        JPanel pnlForm = new JPanel(new MigLayout("", "[][grow]", "[][][]"));
        pnlForm.setBorder(new TitledBorder(null, "Register New Payment", TitledBorder.LEADING, TitledBorder.TOP, mainFont));

        JLabel lblAmount = new JLabel("Amount Paid:");
        lblAmount.setFont(mainFont);
        pnlForm.add(lblAmount, "cell 0 0,alignx trailing");
        
        txtAmount = new JTextField();
        txtAmount.setName("txtAmount");
        txtAmount.setFont(mainFont);
        pnlForm.add(txtAmount, "cell 1 0,growx");
        txtAmount.setColumns(10);

        JLabel lblDate = new JLabel("Payment Date (YYYY-MM-DD):");
        lblDate.setFont(mainFont);
        pnlForm.add(lblDate, "cell 0 1,alignx trailing");
        
        txtPaymentDate = new JTextField();
        txtPaymentDate.setName("txtPaymentDate");
        txtPaymentDate.setFont(mainFont);
        pnlForm.add(txtPaymentDate, "cell 1 1,growx");
        txtPaymentDate.setColumns(10);

        btnRegisterPayment = new JButton("Register Payment");
        btnRegisterPayment.setName("btnRegisterPayment");
        btnRegisterPayment.setFont(mainFont);
        pnlForm.add(btnRegisterPayment, "cell 1 2,alignx trailing");

        frame.getContentPane().add(pnlForm, "cell 0 1,grow");
    }

    // Getters and Setters for Controller access
    public JFrame getFrame() { return frame; }
    public JTable getTabEnrollments() { return tabEnrollments; }
    
    public String getAmount() { return txtAmount.getText(); }
    public void setAmount(String amount) { this.txtAmount.setText(amount); }
    
    public String getPaymentDate() { return txtPaymentDate.getText(); }
    public void setPaymentDate(String date) { this.txtPaymentDate.setText(date); }
    
    public JButton getBtnRegisterPayment() { return btnRegisterPayment; }
}
