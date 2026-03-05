package g54.si26.closeFormativeActions;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.JOptionPane;
import java.awt.Font;
import java.awt.Color;

public class ViewCloseFormativeAction {

    	private JFrame frame;
    	private JTable tabCourses;
    	private JTable tabValidation;
    	private JButton btnCloseAction;
    	private JButton btnBack;

    /**
     * Create the application.
     */
    public ViewCloseFormativeAction() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        	frame = new JFrame();
        	frame.setTitle("Close Formative Actions");
        	frame.setBounds(100, 100, 800, 550);
        	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        	frame.getContentPane().setLayout(new BorderLayout(10, 10));

        	JPanel topPanel = new JPanel();
        	topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        	frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        	//Back button
        	btnBack = new JButton("Back");
        	btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        	btnBack.setBackground(new Color(230, 230, 230));
        	btnBack.setForeground(Color.BLACK);
        	btnBack.setName("btnBack");
        	topPanel.add(btnBack);
        	
        	JSplitPane splitPane = new JSplitPane();
        	splitPane.setResizeWeight(0.6);
        	splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        	frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        	
        	JPanel panel = new JPanel();
        	panel.setBorder(new TitledBorder(null, "1. Select Formative Action", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), null));
        	splitPane.setLeftComponent(panel);
        	panel.setLayout(new BorderLayout(0, 0));
        	
        	JScrollPane scrollPane = new JScrollPane();
        	panel.add(scrollPane, BorderLayout.CENTER);
        	
        	tabCourses = new JTable();
        	tabCourses.setRowHeight(24);
        	tabCourses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        	tabCourses.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        	tabCourses.setDefaultEditor(Object.class, null);
        	scrollPane.setViewportView(tabCourses);

        	JPanel panel_1 = new JPanel();
        	panel_1.setBorder(new TitledBorder(null, "2. System Validation Status", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), new Color(0, 120, 215)));
        	splitPane.setRightComponent(panel_1);
        	panel_1.setLayout(new BorderLayout(0, 0));
        	
        	JScrollPane scrollPane_1 = new JScrollPane();
        	panel_1.add(scrollPane_1, BorderLayout.CENTER);
        	
        	tabValidation = new JTable();
        	tabValidation.setRowHeight(24);
        	tabValidation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        	tabValidation.setDefaultEditor(Object.class, null);
        	scrollPane_1.setViewportView(tabValidation);
        	
        	JPanel bottomPanel = new JPanel();
        	bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        	frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        	
        	btnCloseAction = new JButton("CLOSE FORMATIVE ACTION");
        	btnCloseAction.setEnabled(false);
        	btnCloseAction.setForeground(Color.WHITE);
        	btnCloseAction.setBackground(new Color(220, 53, 69));
        	btnCloseAction.setFont(new Font("Segoe UI", Font.BOLD, 13));
        	bottomPanel.add(btnCloseAction);
    }	

    	public JFrame getFrame() {
        return frame;
    }
    
    public JTable getTabCourses() {
        return tabCourses;
    }
    
    public JTable getTabValidation() {
        return tabValidation;
    }
    
    public JButton getBtnCloseAction() {
        return btnCloseAction;
    }
    
    public JButton getBtnBack() {
        return btnBack;
    }

    public boolean showWarningConfirmation() {
    		int choice = JOptionPane.showConfirmDialog(frame, "There are active warnings in the System Validation.\nDo you want to proceed and close the Formative Action anyway?", "Closure Warnings", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    public void showSuccessMessage() {
        JOptionPane.showMessageDialog(frame, "Formative Action successfully closed.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void showError(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}