package g54.si26.cancelFormativeActions;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ViewCancelFormativeAction {
    private JFrame frame;
    private JTable tableCourses;
    private JTextField txtCourseCompletion;
    private JTable tableTeachers;
    private JButton btnCancel;

    public ViewCancelFormativeAction() {
        frame = new JFrame("Cancel Formative Action");
        frame.setBounds(100, 100, 950, 650);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));

        // Top: Courses Table
        tableCourses = new JTable();
        JScrollPane scrollCourses = new JScrollPane(tableCourses);
        scrollCourses.setBorder(new TitledBorder("Select Formative Action to Cancel"));
        frame.getContentPane().add(scrollCourses, BorderLayout.CENTER);

        // Bottom: Inputs
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setPreferredSize(new Dimension(950, 300));

        // Course completion input
        JPanel completionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        completionPanel.add(new JLabel("General Course Completion (%):"));
        txtCourseCompletion = new JTextField("0", 5);
        completionPanel.add(txtCourseCompletion);
        
        JLabel lblHint = new JLabel("  (Default for all teachers)");
        lblHint.setFont(new Font("Tahoma", Font.ITALIC, 11));
        completionPanel.add(lblHint);
        
        bottomPanel.add(completionPanel, BorderLayout.NORTH);

        // Teachers Table
        tableTeachers = new JTable();
        JScrollPane scrollTeachers = new JScrollPane(tableTeachers);
        scrollTeachers.setBorder(new TitledBorder("Teacher Completion (Double click % to edit individually)"));
        bottomPanel.add(scrollTeachers, BorderLayout.CENTER);

        // Cancel Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCancel = new JButton("Confirm Cancellation");
        btnCancel.setBackground(new Color(200, 0, 0));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("Tahoma", Font.BOLD, 12));
        buttonPanel.add(btnCancel);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

    public JFrame getFrame() { return frame; }
    public JTable getTableCourses() { return tableCourses; }
    public JTextField getTxtCourseCompletion() { return txtCourseCompletion; }
    public JTable getTableTeachers() { return tableTeachers; }
    public JButton getBtnCancel() { return btnCancel; }
}
