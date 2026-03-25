package g54.si26.reopenFormativeActions;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ViewReopenFormativeAction {
    private JFrame frame;
    private JTable tabClosedActions;
    private JButton btnReopenAction;
    private JButton btnBack;

    public ViewReopenFormativeAction() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Re-open Formative Actions");
        frame.setBounds(100, 100, 700, 450);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        btnBack = new JButton("Back");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        topPanel.add(btnBack);

        JPanel centerPanel = new JPanel();
        centerPanel.setBorder(new TitledBorder(null, "Select Closed Formative Action", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), null));
        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);
        centerPanel.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane = new JScrollPane();
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        tabClosedActions = new JTable();
        tabClosedActions.setRowHeight(24);
        tabClosedActions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabClosedActions.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabClosedActions.setDefaultEditor(Object.class, null);
        scrollPane.setViewportView(tabClosedActions);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        btnReopenAction = new JButton("RE-OPEN FORMATIVE ACTION");
        btnReopenAction.setEnabled(false);
        btnReopenAction.setForeground(Color.WHITE);
        btnReopenAction.setBackground(new Color(40, 167, 69)); 
        btnReopenAction.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bottomPanel.add(btnReopenAction);
    }

    public JFrame getFrame() { return frame; }
    public JTable getTabClosedActions() { return tabClosedActions; }
    public JButton getBtnReopenAction() { return btnReopenAction; }
    public JButton getBtnBack() { return btnBack; }

    public void showSuccessMessage() {
        JOptionPane.showMessageDialog(frame, "Formative Action successfully re-opened.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
