package g54.si26.moneyMovements;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class MoneyMovementView {
    private JFrame frame;
    private JRadioButton rdEnrollments, rdInvoices, rdHistory;
    private JTable tableMain, tableHistory, tablePending;
    private JTextField txtAmount, txtDate;
    private JButton btnRegister;

    public MoneyMovementView() {
        frame = new JFrame("Register Money Movements");
        frame.setBounds(100, 100, 1000, 750);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(5, 5));

        // Top: Selection
        JPanel topPanel = new JPanel();
        rdEnrollments = new JRadioButton("Enrollments", true);
        rdInvoices = new JRadioButton("Teacher Invoices");
        rdHistory = new JRadioButton("Full Movement History");
        ButtonGroup group = new ButtonGroup();
        group.add(rdEnrollments); group.add(rdInvoices); group.add(rdHistory);
        topPanel.add(rdEnrollments); topPanel.add(rdInvoices); topPanel.add(rdHistory);
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        // Center: Main Tables (Split)
        JSplitPane splitMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitMain.setDividerLocation(300);

        tableMain = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JScrollPane scrollMain = new JScrollPane(tableMain);
        scrollMain.setBorder(new TitledBorder("Registrations / Invoices / Movement History"));
        splitMain.setTopComponent(scrollMain);


        JPanel bottomTablesPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        
        tableHistory = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JScrollPane scrollHistory = new JScrollPane(tableHistory);
        scrollHistory.setBorder(new TitledBorder("Details / Movement History (Selected)"));
        
        tablePending = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JScrollPane scrollPending = new JScrollPane(tablePending);
        scrollPending.setBorder(new TitledBorder("Records Pending Compensation"));

        bottomTablesPanel.add(scrollHistory);
        bottomTablesPanel.add(scrollPending);
        splitMain.setBottomComponent(bottomTablesPanel);

        frame.getContentPane().add(splitMain, BorderLayout.CENTER);

        // Bottom: Form
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        formPanel.setBorder(new TitledBorder("Register New Movement (Select Enrollment/Invoice first)"));
        
        formPanel.add(new JLabel("Amount:"));
        txtAmount = new JTextField(10);
        formPanel.add(txtAmount);

        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        txtDate = new JTextField(10);
        txtDate.setText(java.time.LocalDate.now().toString());
        formPanel.add(txtDate);

        btnRegister = new JButton("Register Movement");
        formPanel.add(btnRegister);

        frame.getContentPane().add(formPanel, BorderLayout.SOUTH);
    }

    public JFrame getFrame() { return frame; }
    public JRadioButton getRdEnrollments() { return rdEnrollments; }
    public JRadioButton getRdInvoices() { return rdInvoices; }
    public JRadioButton getRdHistory() { return rdHistory; }
    public JTable getTableMain() { return tableMain; }
    public JTable getTableHistory() { return tableHistory; }
    public JTable getTablePending() { return tablePending; }
    public JTextField getTxtAmount() { return txtAmount; }
    public JTextField getTxtDate() { return txtDate; }
    public JButton getBtnRegister() { return btnRegister; }
}
