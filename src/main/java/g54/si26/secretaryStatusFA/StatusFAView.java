package g54.si26.secretaryStatusFA;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StatusFAView {

    private JFrame frame;
    private JTable tblFA;
    private JTable tblRegistrations;
    
    // Global Data
    private JTextField txtSystemDate;

    // Details Fields
    private JTextField txtName;
    private JTextField txtStatus;
    private JTextField txtEnrolmentPeriod;
    private JTextField txtActionDate;
    private JTextField txtTotalSpots;
    private JTextField txtPlacesLeft;
    private JLabel lblEnrolmentOpen;

    // Financial Metrics
    private JTextField txtConfirmedIncome;
    private JTextField txtEstimatedExpenses;
    private JTextField txtConfirmedExpenses;

    public StatusFAView() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Consult Formative Action Status");
        frame.setBounds(100, 100, 900, 800);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));

        // --- GLOBAL: System Date (Read Only) ---
        JPanel pnlGlobal = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlGlobal.add(new JLabel("System Date: "));
        txtSystemDate = createReadOnlyField();
        txtSystemDate.setPreferredSize(new Dimension(100, 20));
        pnlGlobal.add(txtSystemDate);
        frame.getContentPane().add(pnlGlobal, BorderLayout.NORTH);

        // --- TOP: List of Formative Actions ---
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBorder(new TitledBorder("1. Select Formative Action"));
        
        String[] faCols = {"ID", "Name", "Status", "Start Date", "End Date"};
        tblFA = new JTable(new DefaultTableModel(faCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        });
        // Hide ID col
        tblFA.getColumnModel().getColumn(0).setMinWidth(0);
        tblFA.getColumnModel().getColumn(0).setMaxWidth(0);
        tblFA.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        pnlTop.add(new JScrollPane(tblFA), BorderLayout.CENTER);
        
        // Wrapper for NORTH components
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.add(pnlGlobal, BorderLayout.NORTH);
        pnlNorth.add(pnlTop, BorderLayout.CENTER);
        frame.getContentPane().add(pnlNorth, BorderLayout.NORTH);

        // --- CENTER: Details & Registrations (Side by Side) ---
        JPanel pnlCenter = new JPanel(new GridLayout(1, 2, 10, 10));
        frame.getContentPane().add(pnlCenter, BorderLayout.CENTER);

        // Sub-panel: Basic Details (LEFT)
        JPanel pnlDetails = new JPanel(new GridBagLayout());
        pnlDetails.setBorder(new TitledBorder("2. Formative Action Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Anchor to top-left

        addDetailField(pnlDetails, gbc, 0, "Name:", txtName = createReadOnlyField());
        addDetailField(pnlDetails, gbc, 1, "Status:", txtStatus = createReadOnlyField());
        addDetailField(pnlDetails, gbc, 2, "Enrolment Period:", txtEnrolmentPeriod = createReadOnlyField());
        addDetailField(pnlDetails, gbc, 3, "Action Date:", txtActionDate = createReadOnlyField());
        addDetailField(pnlDetails, gbc, 4, "Total Places:", txtTotalSpots = createReadOnlyField());
        addDetailField(pnlDetails, gbc, 5, "Places Left:", txtPlacesLeft = createReadOnlyField());

        lblEnrolmentOpen = new JLabel("ENROLMENT PERIOD OPEN");
        lblEnrolmentOpen.setForeground(new Color(0, 150, 0));
        lblEnrolmentOpen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEnrolmentOpen.setVisible(false);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.weighty = 1.0; // Push everything up
        pnlDetails.add(lblEnrolmentOpen, gbc);

        pnlCenter.add(pnlDetails);

        // Sub-panel: Registrations List (RIGHT)
        JPanel pnlRegistrations = new JPanel(new BorderLayout());
        pnlRegistrations.setBorder(new TitledBorder("3. List of Registrations"));
        
        String[] regCols = {"Name", "Email", "Date", "Fee (€)", "Status"};
        tblRegistrations = new JTable(new DefaultTableModel(regCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        });
        pnlRegistrations.add(new JScrollPane(tblRegistrations), BorderLayout.CENTER);
        pnlCenter.add(pnlRegistrations);

        // --- BOTTOM: Financial Metrics ---
        JPanel pnlFinancials = new JPanel(new GridLayout(1, 3, 10, 10));
        pnlFinancials.setBorder(new TitledBorder("4. Financial Metrics"));

        pnlFinancials.add(createFinancialBox("Confirmed Income", txtConfirmedIncome = createReadOnlyField()));
        pnlFinancials.add(createFinancialBox("Estimated Expenses", txtEstimatedExpenses = createReadOnlyField()));
        pnlFinancials.add(createFinancialBox("Confirmed Expenses", txtConfirmedExpenses = createReadOnlyField()));

        frame.getContentPane().add(pnlFinancials, BorderLayout.SOUTH);
    }

    private void addDetailField(JPanel pnl, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        pnl.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnl.add(field, gbc);
        gbc.weightx = 0;
    }

    private JPanel createFinancialBox(String title, JTextField field) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(title), BorderLayout.NORTH);
        field.setHorizontalAlignment(JTextField.RIGHT);
        field.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JTextField createReadOnlyField() {
        JTextField f = new JTextField();
        f.setEditable(false);
        f.setBackground(new Color(245, 245, 245));
        return f;
    }

    // Getters
    public JFrame getFrame() { return frame; }
    public JTextField getTxtSystemDate() { return txtSystemDate; }
    public JTable getTblFA() { return tblFA; }
    public JTable getTblRegistrations() { return tblRegistrations; }
    public JTextField getTxtName() { return txtName; }
    public JTextField getTxtStatus() { return txtStatus; }
    public JTextField getTxtEnrolmentPeriod() { return txtEnrolmentPeriod; }
    public JTextField getTxtActionDate() { return txtActionDate; }
    public JTextField getTxtTotalSpots() { return txtTotalSpots; }
    public JTextField getTxtPlacesLeft() { return txtPlacesLeft; }
    public JLabel getLblEnrolmentOpen() { return lblEnrolmentOpen; }
    public JTextField getTxtConfirmedIncome() { return txtConfirmedIncome; }
    public JTextField getTxtEstimatedExpenses() { return txtEstimatedExpenses; }
    public JTextField getTxtConfirmedExpenses() { return txtConfirmedExpenses; }
}
