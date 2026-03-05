package g54.si26.teacherpayments;

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

/**
 * View for the "Record Teacher Payments" user story.
 */
public class TeacherPaymentView {

    private JFrame frame;
    private JTable tabInvoices;
    private JTextField txtAmount;
    private JTextField txtTransferDate;
    private JButton btnRegisterTransfer;

    public TeacherPaymentView() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Record Teacher Payments - Secretary");
        frame.setName("TeacherPaymentView");
        frame.setBounds(100, 100, 900, 600);
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new MigLayout("", "[grow]", "[grow][grow]"));

        Font mainFont = new Font("Arial", Font.PLAIN, 14);

        // Top Half: List of Pending Invoices
        JPanel pnlList = new JPanel(new MigLayout("", "[grow]", "[grow]"));
        pnlList.setBorder(new TitledBorder(null, "Pending Teacher Invoices", TitledBorder.LEADING, TitledBorder.TOP, mainFont));
        
        tabInvoices = new JTable();
        tabInvoices.setName("tabInvoices");
        tabInvoices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabInvoices.setDefaultEditor(Object.class, null);
        tabInvoices.setFont(mainFont);
        tabInvoices.setRowHeight(25);
        tabInvoices.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(tabInvoices);
        pnlList.add(scrollPane, "grow");
        
        frame.getContentPane().add(pnlList, "cell 0 0,grow");

        // Bottom Half: Register New Transfer Form
        JPanel pnlForm = new JPanel(new MigLayout("", "[][grow]", "[][][]"));
        pnlForm.setBorder(new TitledBorder(null, "Record Bank Transfer", TitledBorder.LEADING, TitledBorder.TOP, mainFont));

        JLabel lblAmount = new JLabel("Amount Transferred:");
        lblAmount.setFont(mainFont);
        pnlForm.add(lblAmount, "cell 0 0,alignx trailing");
        
        txtAmount = new JTextField();
        txtAmount.setName("txtAmount");
        txtAmount.setFont(mainFont);
        pnlForm.add(txtAmount, "cell 1 0,growx");
        txtAmount.setColumns(10);

        JLabel lblDate = new JLabel("Transfer Date (YYYY-MM-DD):");
        lblDate.setFont(mainFont);
        pnlForm.add(lblDate, "cell 0 1,alignx trailing");
        
        txtTransferDate = new JTextField();
        txtTransferDate.setName("txtTransferDate");
        txtTransferDate.setFont(mainFont);
        pnlForm.add(txtTransferDate, "cell 1 1,growx");
        txtTransferDate.setColumns(10);

        btnRegisterTransfer = new JButton("Record Transfer");
        btnRegisterTransfer.setName("btnRegisterTransfer");
        btnRegisterTransfer.setFont(mainFont);
        pnlForm.add(btnRegisterTransfer, "cell 1 2,alignx trailing");

        frame.getContentPane().add(pnlForm, "cell 0 1,grow");
    }

    public JFrame getFrame() { return frame; }
    public JTable getTabInvoices() { return tabInvoices; }
    public String getAmount() { return txtAmount.getText(); }
    public void setAmount(String amount) { this.txtAmount.setText(amount); }
    public String getTransferDate() { return txtTransferDate.getText(); }
    public void setTransferDate(String date) { this.txtTransferDate.setText(date); }
    public JButton getBtnRegisterTransfer() { return btnRegisterTransfer; }
}
