package g54.si26.cancelEnrollment;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ViewCancelEnrollment {

    private JFrame frame;
    private JTable tableEnrollments;

    // Campos de detalle
    private JTextField txtSelectedCourse;
    private JTextField txtCurrentDate;
    private JTextField txtStartDate;
    private JTextField txtDaysRemaining;

    // Desglose de reembolso
    private JLabel lblTotalFeePaid;
    private JLabel lblAppliedPolicy;
    private JLabel lblTotalRefundDue;

    // Formulario de motivo
    private JComboBox<String> cbReason;
    private JTextField txtDetails;

    // Botones
    private JButton btnBack;
    private JButton btnCancelSelected;

    public ViewCancelEnrollment() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Formative Action Cancellation");
        frame.setBounds(100, 100, 850, 650);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));

        JPanel panelNorth = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelNorth.add(new JLabel("Select an active enrollment below to initiate a cancellation and view potential refund amount."));
        frame.getContentPane().add(panelNorth, BorderLayout.NORTH);

        tableEnrollments = new JTable();
        tableEnrollments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableEnrollments.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(tableEnrollments);
        scrollPane.setBorder(BorderFactory.createTitledBorder(null, "My Active Enrollments", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel panelSouth = new JPanel(new BorderLayout(10, 10));
        panelSouth.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelDetails = new JPanel(new GridBagLayout());
        panelDetails.setBorder(BorderFactory.createTitledBorder(null, "Cancellation & Refund Details", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0; panelDetails.add(new JLabel("Selected Course:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; txtSelectedCourse = new JTextField(); txtSelectedCourse.setEditable(false); panelDetails.add(txtSelectedCourse, gbc);
        gbc.gridx = 2; gbc.weightx = 0; panelDetails.add(new JLabel("Days Remaining:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; txtDaysRemaining = new JTextField(); txtDaysRemaining.setEditable(false); panelDetails.add(txtDaysRemaining, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; panelDetails.add(new JLabel("Current Date:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; txtCurrentDate = new JTextField(); txtCurrentDate.setEditable(false); panelDetails.add(txtCurrentDate, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; panelDetails.add(new JLabel("Course Start Date:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; txtStartDate = new JTextField(); txtStartDate.setEditable(false); panelDetails.add(txtStartDate, gbc);

        JPanel panelBreakdown = new JPanel(new GridBagLayout());
        panelBreakdown.setBorder(BorderFactory.createTitledBorder(null, "Breakdown of Refund Calculation", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));
        GridBagConstraints gbcd = new GridBagConstraints();
        gbcd.fill = GridBagConstraints.HORIZONTAL; gbcd.insets = new Insets(5, 5, 5, 5);
        gbcd.anchor = GridBagConstraints.WEST;

        gbcd.gridx = 0; gbcd.gridy = 0; panelBreakdown.add(new JLabel("Total Fee Paid:"), gbcd);
        gbcd.gridx = 1; lblTotalFeePaid = new JLabel("€0.00"); lblTotalFeePaid.setFont(new Font("Segoe UI", Font.BOLD, 12)); panelBreakdown.add(lblTotalFeePaid, gbcd);

        gbcd.gridx = 0; gbcd.gridy = 1; panelBreakdown.add(new JLabel("Applied Policy Tier:"), gbcd);
        gbcd.gridx = 1; lblAppliedPolicy = new JLabel("-"); lblAppliedPolicy.setFont(new Font("Segoe UI", Font.BOLD, 12)); panelBreakdown.add(lblAppliedPolicy, gbcd);

        gbcd.gridx = 0; gbcd.gridy = 2; gbcd.gridwidth = 2; 
        JSeparator sep = new JSeparator(); panelBreakdown.add(sep, gbcd);

        gbcd.gridx = 0; gbcd.gridy = 3; gbcd.gridwidth = 1; 
        JLabel lblRefundText = new JLabel("Total Refund Due:"); lblRefundText.setFont(new Font("Segoe UI", Font.BOLD, 14)); panelBreakdown.add(lblRefundText, gbcd);
        gbcd.gridx = 1; lblTotalRefundDue = new JLabel("€0.00"); lblTotalRefundDue.setFont(new Font("Segoe UI", Font.BOLD, 14)); panelBreakdown.add(lblTotalRefundDue, gbcd);

        JPanel panelReason = new JPanel(new GridBagLayout());
        GridBagConstraints gbcr = new GridBagConstraints();
        gbcr.fill = GridBagConstraints.HORIZONTAL; gbcr.insets = new Insets(5, 5, 5, 5);

        gbcr.gridx = 0; gbcr.gridy = 0; panelReason.add(new JLabel("Reason for Withdrawal:"), gbcr);
        gbcr.gridx = 1; gbcr.weightx = 1.0;
        String[] reasons = {"Select an option...", "Work Scheduling Conflict", "Personal Reasons", "Financial Issues", "Course Not Relevant", "Other (Optional: type details below)"};
        cbReason = new JComboBox<>(reasons); panelReason.add(cbReason, gbcr);

        gbcr.gridx = 0; gbcr.gridy = 1; gbcr.weightx = 0; panelReason.add(new JLabel("Details (Optional, for 'Other'):"), gbcr);
        gbcr.gridx = 1; gbcr.weightx = 1.0; txtDetails = new JTextField(); txtDetails.setEnabled(false); panelReason.add(txtDetails, gbcr);

        JPanel centerBottom = new JPanel(new BorderLayout(5, 5));
        centerBottom.add(panelDetails, BorderLayout.NORTH);
        centerBottom.add(panelBreakdown, BorderLayout.CENTER);
        centerBottom.add(panelReason, BorderLayout.SOUTH);
        panelSouth.add(centerBottom, BorderLayout.CENTER);

        JPanel panelButtons = new JPanel(new BorderLayout());
        btnBack = new JButton("<- Back");
        btnCancelSelected = new JButton("Cancel Selected Enrollment ✓");
        btnCancelSelected.setBackground(new Color(230, 180, 180));
        btnCancelSelected.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelSelected.setEnabled(false); 

        panelButtons.add(btnBack, BorderLayout.WEST);
        JPanel centerBtnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerBtnWrapper.add(btnCancelSelected);
        panelButtons.add(centerBtnWrapper, BorderLayout.CENTER);

        panelSouth.add(panelButtons, BorderLayout.SOUTH);

        frame.getContentPane().add(panelSouth, BorderLayout.SOUTH);
    }

    public JFrame getFrame() { return frame; }
    public JTable getTableEnrollments() { return tableEnrollments; }
    public JTextField getTxtSelectedCourse() { return txtSelectedCourse; }
    public JTextField getTxtCurrentDate() { return txtCurrentDate; }
    public JTextField getTxtStartDate() { return txtStartDate; }
    public JTextField getTxtDaysRemaining() { return txtDaysRemaining; }
    public JLabel getLblTotalFeePaid() { return lblTotalFeePaid; }
    public JLabel getLblAppliedPolicy() { return lblAppliedPolicy; }
    public JLabel getLblTotalRefundDue() { return lblTotalRefundDue; }
    public JComboBox<String> getCbReason() { return cbReason; }
    public JTextField getTxtDetails() { return txtDetails; }
    public JButton getBtnBack() { return btnBack; }
    public JButton getBtnCancelSelected() { return btnCancelSelected; }
}