package g54.si26.consultFormativeActionsSecretary;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ViewConsultFormativeActions {

    private JFrame frame;
    
    // Top Panel Buttons
    private JButton btnBack;
    private JButton btnRefresh;
    private JButton btnStatusFA;
    private JButton btnExecuteTMConsult;

    // Filters Section
    private JTextField txtDateFilter;
    private JComboBox<String> cbStatusFilter;
    private JButton btnSearch;

    // Grid Section
    private JTable tblFormativeActions;

    // Further Information Section (Read-Only)
    private JPanel pnlDetails;
    private JTextArea txtObjectives;
    private JTextArea txtMainContents;
    private JTextField txtLocation;
    private JTextField txtTeachers;
    private JTextField txtTotalRegisters;
    private JTable tblCommunityFees;

    // CODIGO DE COLORES Y ESTILOS
    private static final Color COLOR_LIGHT_BLUE = new Color(227, 242, 253);   
    private static final Color COLOR_LIGHT_GRAY = new Color(240, 244, 248);   
    private static final Color COLOR_BG  = UIManager.getColor("Panel.background");
    private static final Color COLOR_SECTION_BG = COLOR_BG;    
    private static final Color COLOR_UPDATE = new Color(0, 85, 180);       
    private static final Color COLOR_SEPARATOR = new Color(180, 205, 230);    
    
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 12);

    public ViewConsultFormativeActions() {
        initialize();
    }

    private void initialize() {
        // Title
        frame = new JFrame("Consult a list of Formative Actions for Management");
        frame.setBounds(100, 100, 1000, 650);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(COLOR_BG);

        // TOP PANEL: Back & Refresh Buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(COLOR_BG);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_SEPARATOR)); 
        
        btnBack = createStyledButton("Back", COLOR_LIGHT_BLUE, COLOR_UPDATE, 80, 25);
        btnRefresh = createStyledButton("Refresh (Clear Filters)", COLOR_LIGHT_GRAY, Color.DARK_GRAY, 160, 25);
        //btnStatusFA = createStyledButton("Consult FA Registrations", COLOR_LIGHT_GRAY, Color.DARK_GRAY, 200, 25);
        topPanel.add(btnBack);
        topPanel.add(btnRefresh);
        //topPanel.add(btnStatusFA);
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        // Main Window
        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));
        centerContainer.setBackground(COLOR_BG);
        centerContainer.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        frame.getContentPane().add(centerContainer, BorderLayout.CENTER);

        // SECTION 1: FILTERS 
        JPanel p1 = createSection("1. Filters", new FlowLayout(FlowLayout.LEFT, 15, 15));
        
        JLabel lblDate = new JLabel("Dates (yyyy-MM-dd):");
        lblDate.setFont(FONT_REGULAR);
        p1.add(lblDate);
        
        txtDateFilter = new JTextField();
        styleTextField(txtDateFilter);
        txtDateFilter.setPreferredSize(new Dimension(150, 25));
        p1.add(txtDateFilter);
        
        JLabel lblState = new JLabel("  State:");
        lblState.setFont(FONT_REGULAR);
        p1.add(lblState);

        cbStatusFilter = new JComboBox<>(new String[]{
            "ACTIVE (Default)", "ALL", "Upcoming", "Enrolment open", 
            "In progress", "Finished", "CLOSED", "Cancelled"
        });
        cbStatusFilter.setFont(FONT_REGULAR);
        cbStatusFilter.setBackground(Color.WHITE);
        cbStatusFilter.setPreferredSize(new Dimension(180, 25));
        p1.add(cbStatusFilter);

        btnSearch = createStyledButton("Search", COLOR_UPDATE, Color.WHITE, 100, 25);
        p1.add(btnSearch);
        
        centerContainer.add(p1, BorderLayout.NORTH);

        // SECTION 2: GRID (Table)
        JPanel p2 = createSection("2. Formative Actions List", new BorderLayout(5, 5));
        
        JPanel pnlHint = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlHint.setOpaque(false); 
        JLabel lblGridHint = new JLabel("(i) Select a row for Further Information");
        lblGridHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblGridHint.setForeground(new Color(100, 120, 140));
        pnlHint.add(lblGridHint);
        p2.add(pnlHint, BorderLayout.NORTH); 
        
        String[] columnNames = {"ID", "Name", "Status", "Enrolment Period", "Total Places", "Places Left", "Reserved Places", "Confirmed Enrolments", "Date", "Conf. Income (€)", "Conf. Expenses (€)", "Balance (€)"};
        tblFormativeActions = new JTable(new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        styleTable(tblFormativeActions);
        
        // Hide ID for review
        tblFormativeActions.getColumnModel().getColumn(0).setMinWidth(0);
        tblFormativeActions.getColumnModel().getColumn(0).setMaxWidth(0);
        tblFormativeActions.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        JScrollPane spActions = new JScrollPane(tblFormativeActions);
        spActions.getViewport().setBackground(Color.WHITE);
        spActions.setBorder(BorderFactory.createLineBorder(COLOR_SEPARATOR));
        p2.add(spActions, BorderLayout.CENTER);
        
        // Placement of the grid
        centerContainer.add(p2, BorderLayout.CENTER);

        // SECTION 3: FURTHER INFORMATION (Details)
        pnlDetails = createSection("3. Further Information ", new BorderLayout(10, 10));
        
        //Left Side
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Objectives
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.05; gbc.weighty = 0.5; gbc.fill = GridBagConstraints.BOTH;
        JLabel lblObj = new JLabel("Objectives:");
        lblObj.setFont(FONT_REGULAR);
        formPanel.add(lblObj, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.95; 
        txtObjectives = createReadOnlyTextArea(3); 
        formPanel.add(new JScrollPane(txtObjectives), gbc);

        // Main Contents
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.05;
        JLabel lblContents = new JLabel("Main Contents:");
        lblContents.setFont(FONT_REGULAR);
        formPanel.add(lblContents, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.95; 
        txtMainContents = createReadOnlyTextArea(4); 
        formPanel.add(new JScrollPane(txtMainContents), gbc);

        // Location
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.05; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel lblLocation = new JLabel("Place/Location:");
        lblLocation.setFont(FONT_REGULAR);
        formPanel.add(lblLocation, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.95; 
        txtLocation = new JTextField();
        styleReadOnlyField(txtLocation);
        formPanel.add(txtLocation, gbc);

        // Yeachers
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.05; 
        JLabel lblTeachers = new JLabel("Teacher(s):");
        lblTeachers.setFont(FONT_REGULAR);
        formPanel.add(lblTeachers, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.95; 
        txtTeachers = new JTextField();
        styleReadOnlyField(txtTeachers);
        formPanel.add(txtTeachers, gbc);
        
        //Total Inscriptions
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.05; 
        JLabel lblRegisters = new JLabel("Total Registers:");
        lblRegisters.setFont(FONT_REGULAR);
        lblRegisters.setVisible(false);
        formPanel.add(lblRegisters, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.95; 
        txtTotalRegisters = new JTextField();
        styleReadOnlyField(txtTotalRegisters);
        txtTotalRegisters.setVisible(false);
        formPanel.add(txtTotalRegisters, gbc);
        
        pnlDetails.add(formPanel, BorderLayout.CENTER);
        
        btnStatusFA = createStyledButton("Consult FA Registrations", COLOR_UPDATE, Color.WHITE, 200, 30);
        JPanel pnlStatusAction = new JPanel(new BorderLayout(0, 5));
        pnlStatusAction.setOpaque(false);
        
        //JLabel lblStatusInfo = new JLabel("Check enrolled professionals:");
        //lblStatusInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        //lblStatusInfo.setForeground(Color.DARK_GRAY);
        //lblStatusInfo.setHorizontalAlignment(SwingConstants.RIGHT);
        
        //pnlStatusAction.add(lblStatusInfo, BorderLayout.NORTH); 
        //pnlStatusAction.add(btnStatusFA, BorderLayout.SOUTH);   
        
        JPanel pnlBottomRightWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlBottomRightWrapper.setOpaque(false);
        pnlBottomRightWrapper.add(pnlStatusAction);
        
        pnlDetails.add(pnlBottomRightWrapper, BorderLayout.SOUTH);
        // details section
        centerContainer.add(pnlDetails, BorderLayout.SOUTH);
        pnlDetails.setVisible(false); 
        
        JPanel pnlFees = new JPanel(new BorderLayout(0, 15));
        pnlFees.setOpaque(false);
        pnlFees.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10), "Community Fees", TitledBorder.LEFT, TitledBorder.TOP, FONT_BOLD, COLOR_UPDATE));
        pnlFees.setPreferredSize(new Dimension(320, 300)); 

        tblCommunityFees = new JTable(new DefaultTableModel(new String[]{"ID", "Community", "Fee (€)"}, 0){
            @Override public boolean isCellEditable(int r, int c){ return false; }
        });
        styleTable(tblCommunityFees);
        
        // Ocultar ID d la tabla d fees
        tblCommunityFees.getColumnModel().getColumn(0).setMinWidth(0);
        tblCommunityFees.getColumnModel().getColumn(0).setMaxWidth(0);
        tblCommunityFees.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        JScrollPane spFees = new JScrollPane(tblCommunityFees);
        spFees.getViewport().setBackground(Color.WHITE);
        spFees.setBorder(BorderFactory.createLineBorder(COLOR_SEPARATOR));
        spFees.setPreferredSize(new Dimension(300, 100));
        pnlFees.add(spFees, BorderLayout.CENTER);
        
        JPanel pnlActionButtons = new JPanel(new GridLayout(5, 1, 0, 3)); 
        pnlActionButtons.setOpaque(false);
        
        // Botón 1: Registros
        JLabel lblStatusInfo = new JLabel("Check enrolled professionals:");
        lblStatusInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblStatusInfo.setForeground(Color.DARK_GRAY);
        btnStatusFA = createStyledButton("Consult FA Registrations", COLOR_UPDATE, Color.WHITE, 200, 30);
        
        // Botón 2: Finanzas
        JLabel lblFinanceInfo = new JLabel("Check financial status:");
        lblFinanceInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFinanceInfo.setForeground(Color.DARK_GRAY);
        btnExecuteTMConsult = createStyledButton("Consult Income and Expenses", COLOR_UPDATE, Color.WHITE, 200, 30);

        pnlActionButtons.add(lblStatusInfo);
        pnlActionButtons.add(btnStatusFA);
        pnlActionButtons.add(new JLabel(" ")); 
        
        pnlActionButtons.add(lblFinanceInfo);
        pnlActionButtons.add(btnExecuteTMConsult);
        pnlFees.add(pnlActionButtons, BorderLayout.SOUTH);
        pnlDetails.add(pnlFees, BorderLayout.EAST);


        centerContainer.add(pnlDetails, BorderLayout.SOUTH);
        pnlDetails.setVisible(false);
    }

    // ================================
    //  Style helpers
    // =====================================

    private javax.swing.border.Border createRoundedBorder(Color color, int thickness, int radius) {
        return new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new java.awt.BasicStroke(thickness));
                int offset = thickness / 2;
                g2.drawRoundRect(x + offset, y + offset, width - thickness - 1, height - thickness - 1, radius, radius);
                g2.dispose();
            }
            @Override public Insets getBorderInsets(Component c) { return new Insets(8, 12, 8, 12); }
            @Override public Insets getBorderInsets(Component c, Insets insets) { insets.set(8, 12, 8, 12); return insets; }
        };
    }

    private JPanel createSection(String title, LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_SECTION_BG); 
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
                g2.dispose();
            }
        };
        p.setOpaque(false); 
        p.setBorder(BorderFactory.createTitledBorder(createRoundedBorder(COLOR_UPDATE, 1, 16), title, TitledBorder.LEFT, TitledBorder.TOP, FONT_BOLD, COLOR_UPDATE));
        return p;
    }

    private void styleTable(JTable table) {
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(230, 240, 250)); 
        table.setSelectionBackground(new Color(200, 225, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setRowHeight(24);
        table.getTableHeader().setBackground(new Color(240, 248, 255)); 
        table.getTableHeader().setForeground(COLOR_UPDATE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_SEPARATOR));
    }

    private void styleTextField(JTextField t) {
        t.setFont(FONT_REGULAR);
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 210)),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
    }

    private void styleReadOnlyField(JTextField t) {
        t.setFont(FONT_REGULAR);
        t.setEditable(false);
        t.setBackground(COLOR_LIGHT_GRAY);
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 210)),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
    }

    private JTextArea createReadOnlyTextArea(int rows) {
        JTextArea t = new JTextArea(rows, 20);
        t.setFont(FONT_REGULAR);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setEditable(false);
        t.setBackground(COLOR_LIGHT_GRAY);
        t.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        return t;
    }

    private JButton createStyledButton(String text, Color bg, Color fg, int w, int h) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(w, h)); 
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(FONT_BOLD);
        b.setFocusPainted(false);
        Color borderColor = bg.equals(COLOR_BG) || bg.equals(COLOR_LIGHT_BLUE) || bg.equals(COLOR_LIGHT_GRAY) 
                            ? new Color(200, 200, 200) : bg.darker();
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            BorderFactory.createEmptyBorder(2, 15, 2, 15) 
        ));
        return b;
    }

    // Details panel
    public void setDetailsPanelVisible(boolean visible) {
        pnlDetails.setVisible(visible);
        frame.revalidate();
        frame.repaint();
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ===================
    //  GETTERS & SETTERS 
    // ====================

    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public JButton getBtnBack() {
        return btnBack;
    }

    public void setBtnBack(JButton btnBack) {
        this.btnBack = btnBack;
    }

    public JButton getBtnRefresh() {
        return btnRefresh;
    }

    public void setBtnRefresh(JButton btnRefresh) {
        this.btnRefresh = btnRefresh;
    }

    public JTextField getTxtDateFilter() {
        return txtDateFilter;
    }

    public void setTxtDateFilter(JTextField txtDateFilter) {
        this.txtDateFilter = txtDateFilter;
    }

    public JComboBox<String> getCbStatusFilter() {
        return cbStatusFilter;
    }

    public void setCbStatusFilter(JComboBox<String> cbStatusFilter) {
        this.cbStatusFilter = cbStatusFilter;
    }

    public JButton getBtnSearch() {
        return btnSearch;
    }

    public void setBtnSearch(JButton btnSearch) {
        this.btnSearch = btnSearch;
    }

    public JTable getTblFormativeActions() {
        return tblFormativeActions;
    }

    public void setTblFormativeActions(JTable tblFormativeActions) {
        this.tblFormativeActions = tblFormativeActions;
    }

    public JPanel getPnlDetails() {
        return pnlDetails;
    }

    public void setPnlDetails(JPanel pnlDetails) {
        this.pnlDetails = pnlDetails;
    }

    public JTextArea getTxtObjectives() {
        return txtObjectives;
    }

    public void setTxtObjectives(JTextArea txtObjectives) {
        this.txtObjectives = txtObjectives;
    }

    public JTextArea getTxtMainContents() {
        return txtMainContents;
    }

    public void setTxtMainContents(JTextArea txtMainContents) {
        this.txtMainContents = txtMainContents;
    }

    public JTextField getTxtLocation() {
        return txtLocation;
    }

    public void setTxtLocation(JTextField txtLocation) {
        this.txtLocation = txtLocation;
    }

    public JTextField getTxtTeachers() {
        return txtTeachers;
    }

    public void setTxtTeachers(JTextField txtTeachers) {
        this.txtTeachers = txtTeachers;
    }
    public JTable getTblCommunityFees() {
        return tblCommunityFees;
    }

    public void setTblCommunityFees(JTable tblCommunityFees) {
        this.tblCommunityFees = tblCommunityFees;
    }
    
    public JTextField getTxtTotalRegisters() {
        return txtTotalRegisters;
    }

    public void setTxtTotalRegisters(JTextField txtTotalRegisters) {
        this.txtTotalRegisters = txtTotalRegisters;
    }
    
    public JButton getBtnStatusFA() {
        return btnStatusFA;
    }
    
    public JButton getBtnExecuteTMConsult() {
        return btnExecuteTMConsult;
    }
    
}