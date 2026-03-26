package g54.si26.viewPendingPayments;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ViewPendingView {
    private JFrame frame;
    private JComboBox<String> cbFilter;
    private JButton btnLoadData;
    private JTable tabPayments;
    private JButton btnBack;

    public ViewPendingView() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Consult Pending Payments");
        frame.setBounds(100, 100, 800, 450);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        JLabel lblFilter = new JLabel("Filter by type:");
        lblFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        topPanel.add(lblFilter);

        cbFilter = new JComboBox<>(new String[]{"ALL", "Refund", "Compensation"});
        cbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        topPanel.add(cbFilter);

        btnLoadData = new JButton("Show Data");
        btnLoadData.setFont(new Font("Segoe UI", Font.BOLD, 12));
        topPanel.add(btnLoadData);

        JPanel centerPanel = new JPanel();
        centerPanel.setBorder(new TitledBorder(null, "Pending Payments List", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), null));
        centerPanel.setLayout(new BorderLayout(0, 0));
        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane();
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        tabPayments = new JTable();
        tabPayments.setRowHeight(24);
        tabPayments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabPayments.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabPayments.setDefaultEditor(Object.class, null); 
        scrollPane.setViewportView(tabPayments);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        btnBack = new JButton("Back");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bottomPanel.add(btnBack);
        
        frame.setLocationRelativeTo(null);
    }

    public JFrame getFrame() { return frame; }
    public JComboBox<String> getCbFilter() { return cbFilter; }
    public JButton getBtnLoadData() { return btnLoadData; }
    public JTable getTabPayments() { return tabPayments; }
    public JButton getBtnBack() { return btnBack; }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}