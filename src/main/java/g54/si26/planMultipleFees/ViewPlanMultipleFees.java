package g54.si26.planMultipleFees;

import g54.si26.DTOs.TeacherDTO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ViewPlanMultipleFees {

    private JFrame frame;
    
    // Componentes de las Secciones
    private JTextField txtCourseName, txtSpots, txtStartDate, txtEndDate, txtLocation, txtRemuneration;
    private JTextArea txtObjectives, txtMainContents;
    private JTextField txtEnrolStart, txtEnrolEnd;
    private JSpinner spnDuration;
    private JCheckBox chkOnline;
    private JComboBox<TeacherDTO> cbTeacher;
    private JTable tblTeachers;
    private JButton btnAddTeacher, btnUpdateTeacher, btnRemoveTeacher;
    private JLabel lblTeacherEditHint; 
    private JLabel lblHintCourseName;
    private JLabel lblHintSpots;
    private JLabel lblHintStartDate;
    private JLabel lblHintEndDate;
    private JLabel lblHintEnrolment;
    private JLabel lblHintTeacher;
    private JLabel lblHintCommunity;
    private JTable tblCommunities;      
    private JButton btnDeleteCommunity, btnAddCommunity, btnUpdateComm; 
    private JTextField txtEditCommName, txtEditCommFee;  
    private JCheckBox chkSingleFee;     

    private JButton btnFillDebug, btnBack, btnClear, btnSave;

    private static final Color COLOR_DELETE = new Color(220, 53, 69); 
    private static final Color COLOR_LIGHT_BLUE = new Color(227, 242, 253);   
    private static final Color COLOR_LIGHT_GRAY = new Color(240, 244, 248);   
    private static final Color COLOR_BG  = UIManager.getColor("Panel.background");
    private static final Color COLOR_SECTION_BG = COLOR_BG;   
    private static final Color COLOR_SUBSECTION_BG = COLOR_BG;    
    private static final Color COLOR_UPDATE = new Color(0, 85, 180);       
    private static final Color COLOR_INSERT = new Color(255, 140, 50);     
    private static final Color COLOR_CLEAR = new Color(255, 200, 0);    
    private static final Color COLOR_SEPARATOR = new Color(180, 205, 230);    
    
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 12);

    public ViewPlanMultipleFees() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Plan New Formative Action - Multi Fee Edition");
        frame.setBounds(100, 100, 950, 650);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().setBackground(COLOR_BG);

        // TOP PANEL (Back Button)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(COLOR_BG);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_SEPARATOR)); 
        
        btnBack = createStyledButton("Back", COLOR_LIGHT_BLUE, COLOR_UPDATE, 80, 25);
        topPanel.add(btnBack);
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        // MAIN PANEL 
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(COLOR_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.gridx = 0; mainGbc.gridy = 0; mainGbc.weightx = 1.0; 
        mainGbc.fill = GridBagConstraints.HORIZONTAL;
        mainGbc.insets = new Insets(0, 0, 15, 0);

        // SECTION 1: GENERAL INFO
        JPanel p1 = createSection("1. General Information", new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); gbc.anchor = GridBagConstraints.NORTHWEST; gbc.fill = GridBagConstraints.BOTH;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.05; p1.add(createLabel("Course Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.95;
        txtCourseName = createTextField(); 
        lblHintCourseName = createMandatoryHint("(i) Mandatory Field");
        p1.add(wrapWithHint(txtCourseName, lblHintCourseName), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.05; p1.add(createLabel("Objectives:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.95; txtObjectives = createTextArea(3); p1.add(new JScrollPane(txtObjectives), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.05; p1.add(createLabel("Main Contents:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.95; txtMainContents = createTextArea(3); p1.add(new JScrollPane(txtMainContents), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.05; p1.add(createLabel("Places:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.95; gbc.fill = GridBagConstraints.NONE; 
        txtSpots = createTextField(); txtSpots.setPreferredSize(new Dimension(100, 25)); 
        lblHintSpots = createMandatoryHint("(i) Mandatory Field");
        p1.add(wrapWithHint(txtSpots, lblHintSpots), gbc);
        
        mainPanel.add(p1, mainGbc); mainGbc.gridy++;

        // SECTION 2: SCHEDULE
        JPanel p2 = createSection("2. Location & Schedule", new GridBagLayout());
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.05; p2.add(createLabel("Start (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.45; 
        txtStartDate = createTextField(); 
        lblHintStartDate = createMandatoryHint("(i) Mandatory Field");
        p2.add(wrapWithHint(txtStartDate, lblHintStartDate), gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.05; p2.add(createLabel("End (yyyy-MM-dd):"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.45; 
        txtEndDate = createTextField(); 
        lblHintEndDate = createMandatoryHint("(i) Mandatory Field");
        p2.add(wrapWithHint(txtEndDate, lblHintEndDate), gbc);
      
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.05; p2.add(createLabel("Duration (Hrs):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.45; spnDuration = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1)); spnDuration.setFont(FONT_REGULAR); p2.add(spnDuration, gbc);
        gbc.gridx = 2; gbc.weightx = 0.05; p2.add(createLabel("Location:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.45; txtLocation = createTextField(); p2.add(txtLocation, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; chkOnline = new JCheckBox("Held Online"); chkOnline.setBackground(COLOR_BG); chkOnline.setFont(FONT_REGULAR); p2.add(chkOnline, gbc);
        
        mainPanel.add(p2, mainGbc); mainGbc.gridy++;

        // SECTION 3: ENROLMENT PERIOD
        JPanel p3 = createSection("3. Enrolment Period", new GridBagLayout());
        gbc.gridwidth = 1;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.05; p3.add(createLabel("Start (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.45; 
        txtEnrolStart = createTextField(); 
        lblHintEnrolment = createMandatoryHint("(i) Mandatory Fields");
        p3.add(wrapWithHint(txtEnrolStart, lblHintEnrolment), gbc);
        gbc.gridx = 2; gbc.weightx = 0.05; p3.add(createLabel("End (yyyy-MM-dd):"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.45; txtEnrolEnd = createTextField(); p3.add(txtEnrolEnd, gbc);
        
        mainPanel.add(p3, mainGbc); mainGbc.gridy++;

        // SECTION 4: TEACHERS
        JPanel p4 = createSection("4. Teachers", new BorderLayout(15, 10));
        p4.setBorder(BorderFactory.createCompoundBorder(p4.getBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        JPanel pnlTeacherForm = new JPanel(new GridBagLayout());
        pnlTeacherForm.setOpaque(false);
        GridBagConstraints gbcT = new GridBagConstraints();
        gbcT.insets = new Insets(5, 5, 5, 5); gbcT.anchor = GridBagConstraints.WEST; gbcT.fill = GridBagConstraints.HORIZONTAL;
        
        gbcT.gridx = 0; gbcT.gridy = 0; pnlTeacherForm.add(createLabel("Teacher:"), gbcT);
        gbcT.gridx = 1; cbTeacher = new JComboBox<>(); cbTeacher.setFont(FONT_REGULAR); cbTeacher.setBackground(Color.WHITE); pnlTeacherForm.add(cbTeacher, gbcT);
        
        gbcT.gridx = 0; gbcT.gridy = 1; pnlTeacherForm.add(createLabel("Remuneration (€):"), gbcT);
        gbcT.gridx = 1; txtRemuneration = createTextField(); pnlTeacherForm.add(txtRemuneration, gbcT);
        
        JPanel pnlTeacherBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlTeacherBtns.setOpaque(false);
        btnAddTeacher = createStyledButton("Add", COLOR_INSERT, Color.WHITE, 80, 25);
        btnUpdateTeacher = createStyledButton("Update", COLOR_UPDATE, Color.WHITE, 80, 25); btnUpdateTeacher.setVisible(false);
        btnRemoveTeacher = createStyledButton("Remove", COLOR_DELETE, Color.WHITE, 90, 25);
        pnlTeacherBtns.add(btnAddTeacher); pnlTeacherBtns.add(btnUpdateTeacher); pnlTeacherBtns.add(btnRemoveTeacher);
        
        gbcT.gridx = 0; gbcT.gridy = 2; gbcT.gridwidth = 2; pnlTeacherForm.add(pnlTeacherBtns, gbcT);
        p4.add(pnlTeacherForm, BorderLayout.WEST);
        
        JPanel pnlTeacherTable = new JPanel(new BorderLayout(0, 5));
        pnlTeacherTable.setOpaque(false);
        lblTeacherEditHint = new JLabel("(i) Select a teacher from the grid to update remuneration");
        lblTeacherEditHint.setFont(new Font("Segoe UI", Font.ITALIC, 11)); lblTeacherEditHint.setForeground(new Color(100, 120, 140)); lblTeacherEditHint.setVisible(false);
        pnlTeacherTable.add(lblTeacherEditHint, BorderLayout.NORTH);
        
        tblTeachers = new JTable(new DefaultTableModel(new String[]{"ID", "Name", "Remuneration (€)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        styleTable(tblTeachers);
        tblTeachers.getColumnModel().getColumn(0).setMinWidth(0); tblTeachers.getColumnModel().getColumn(0).setMaxWidth(0); tblTeachers.getColumnModel().getColumn(0).setPreferredWidth(0);
        JScrollPane spTeachers = new JScrollPane(tblTeachers);
        spTeachers.setPreferredSize(new Dimension(400, 120));
        spTeachers.getViewport().setBackground(Color.WHITE);
        lblHintTeacher = createMandatoryHint("(i) At least one teacher is Mandatory");
        JPanel wrapperTeacher = wrapWithHint(spTeachers, lblHintTeacher);
        pnlTeacherTable.add(wrapperTeacher, BorderLayout.CENTER);
        p4.add(pnlTeacherTable, BorderLayout.CENTER);
        
        mainPanel.add(p4, mainGbc); mainGbc.gridy++;

        // SECTION 5: FEES PER COMMUNITY
        JPanel p5 = createSection("5. Fees per Community Management", new GridLayout(1, 2, 15, 0));
        p5.setBorder(BorderFactory.createCompoundBorder(p5.getBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        JPanel subList = createSubSection("Community Repository", new BorderLayout(0, 10));
        tblCommunities = new JTable(new DefaultTableModel(new String[]{"ID", "Community Name", "Fee (€)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        styleTable(tblCommunities); 
        tblCommunities.getColumnModel().getColumn(0).setMinWidth(0); tblCommunities.getColumnModel().getColumn(0).setMaxWidth(0); tblCommunities.getColumnModel().getColumn(0).setPreferredWidth(0);
        JScrollPane spComm = new JScrollPane(tblCommunities); 
        spComm.setPreferredSize(new Dimension(0, 120)); 
        spComm.getViewport().setBackground(Color.WHITE);
        
        lblHintCommunity = createMandatoryHint("(i) At least one Fee is mandatory");
        subList.add(wrapWithHint(spComm, lblHintCommunity), BorderLayout.CENTER);
        btnDeleteCommunity = createStyledButton("Delete Selected Community", COLOR_DELETE, Color.WHITE, 200, 25);
        JPanel pnlDelComm = new JPanel(new FlowLayout(FlowLayout.LEFT)); pnlDelComm.setOpaque(false); pnlDelComm.add(btnDeleteCommunity);
        subList.add(pnlDelComm, BorderLayout.SOUTH);
        p5.add(subList);

        JPanel subEdit = createSubSection("Edit / Set Fee", new GridBagLayout());
        GridBagConstraints gbcC = new GridBagConstraints(); gbcC.insets = new Insets(5, 5, 5, 5); gbcC.anchor = GridBagConstraints.WEST; gbcC.fill = GridBagConstraints.HORIZONTAL;
        
        gbcC.gridx = 0; gbcC.gridy = 0; gbcC.weightx = 0.05; subEdit.add(createLabel("Name:"), gbcC);
        gbcC.gridx = 1; gbcC.weightx = 0.95; txtEditCommName = createTextField(); subEdit.add(txtEditCommName, gbcC);
        
        gbcC.gridx = 0; gbcC.gridy = 1; gbcC.weightx = 0.05; subEdit.add(createLabel("Fee (€):"), gbcC);
        gbcC.gridx = 1; gbcC.weightx = 0.95; txtEditCommFee = createTextField(); subEdit.add(txtEditCommFee, gbcC);
        
        JPanel pnlCommBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); pnlCommBtns.setOpaque(false);
        btnUpdateComm = createStyledButton("Update Selected", COLOR_UPDATE, Color.WHITE, 140, 25);
        btnAddCommunity = createStyledButton("+ Add New", COLOR_INSERT, Color.WHITE, 120, 25);
        pnlCommBtns.add(btnUpdateComm); pnlCommBtns.add(btnAddCommunity);
        gbcC.gridx = 0; gbcC.gridy = 2; gbcC.gridwidth = 2; subEdit.add(pnlCommBtns, gbcC);
        
        gbcC.gridx = 0; gbcC.gridy = 3; gbcC.gridwidth = 2; subEdit.add(new JSeparator(), gbcC);
        
        gbcC.gridx = 0; gbcC.gridy = 4; gbcC.gridwidth = 2; 
        chkSingleFee = new JCheckBox("Apply this fee to all table rows"); chkSingleFee.setBackground(COLOR_BG); chkSingleFee.setFont(FONT_REGULAR);
        subEdit.add(chkSingleFee, gbcC);
        p5.add(subEdit);

        mainPanel.add(p5, mainGbc); mainGbc.gridy++;
        
        // Layout
        mainGbc.weighty = 1.0; mainGbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(new JPanel(){{setOpaque(false);}}, mainGbc);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // BOTTOM TOOLBAR
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        toolbar.setBackground(COLOR_BG);
        toolbar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_SEPARATOR));
        
        btnFillDebug = createStyledButton("Fill Debug Data", COLOR_CLEAR, Color.DARK_GRAY, 150, 25);
        btnClear = createStyledButton("Clear", COLOR_LIGHT_BLUE, COLOR_UPDATE, 100, 25);
        btnSave = createStyledButton("Save Action", COLOR_INSERT, Color.WHITE, 150, 25);
        btnFillDebug.setVisible(false);
        toolbar.add(btnFillDebug); 
        toolbar.add(btnClear); 
        toolbar.add(btnSave);
        frame.getContentPane().add(toolbar, BorderLayout.SOUTH);
    }

    // ===================
    //  HELPERS D ESTILO
    // ===================

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
            @Override public Insets getBorderInsets(Component c) { return new Insets(4, 10, 4, 10); }
            @Override public Insets getBorderInsets(Component c, Insets insets) { insets.set(4, 10, 4, 10); return insets; }
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

    private JPanel createSubSection(String title, LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_SUBSECTION_BG); 
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                g2.dispose();
            }
        };
        p.setOpaque(false); 
        p.setBorder(BorderFactory.createTitledBorder(createRoundedBorder(new Color(230, 200, 150), 1, 12), title, TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.ITALIC, 11), COLOR_UPDATE));
        return p;
    }

    private void styleTable(JTable table) {
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(230, 240, 250)); 
        table.setSelectionBackground(new Color(200, 225, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setRowHeight(22);
        table.getTableHeader().setBackground(new Color(240, 248, 255)); 
        table.getTableHeader().setForeground(COLOR_UPDATE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_SEPARATOR));
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_REGULAR);
        return l;
    }

    private JTextField createTextField() {
        JTextField t = new JTextField();
        t.setFont(FONT_REGULAR);
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 210)),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        return t;
    }

    private JTextArea createTextArea(int rows) {
        JTextArea t = new JTextArea(rows, 20);
        t.setFont(FONT_REGULAR);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
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
    
    private JLabel createMandatoryHint(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        l.setForeground(new Color(220, 80, 80));
        return l;
    }

    private JPanel wrapWithHint(JComponent mainComponent, JLabel hintLabel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(mainComponent, BorderLayout.CENTER);
        p.add(hintLabel, BorderLayout.SOUTH);
        return p;
    }

    // ------------------
    //  FEEDBACK SECTION
    //------------------
    public void showSuccess(String courseName) {
        JOptionPane.showMessageDialog(frame, "Formative action \"" + courseName + "\" has been planned successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showValidationSummary(java.util.List<String> errors, java.util.List<String> warnings) {
        StringBuilder sb = new StringBuilder("The Formative Action could not be saved.\nPlease review the following issues:\n\n");
        if (!errors.isEmpty()) {
            sb.append("BLOCKING ERRORS\n");
            errors.forEach(e -> sb.append("  -  ").append(e).append("\n"));
        }
        if (!warnings.isEmpty()) {
            sb.append("\nWARNINGS\n");
            warnings.forEach(w -> sb.append("  -  ").append(w).append("\n"));
        }
        JOptionPane.showMessageDialog(frame, sb.toString(), "Validation Summary", JOptionPane.ERROR_MESSAGE);
    }

    //--------------
    // Getter section
    //------------------

    public JFrame getFrame() { return frame; }
    public JTable getTblCommunities() { return tblCommunities; }
    public JButton getBtnAddCommunity() { return btnAddCommunity; }
    public JButton getBtnDeleteCommunity() { return btnDeleteCommunity; }
    public JButton getBtnUpdateComm() { return btnUpdateComm; }
    public JTextField getTxtEditCommName() { return txtEditCommName; }
    public JTextField getTxtEditCommFee() { return txtEditCommFee; }
    public JCheckBox getChkSingleFee() { return chkSingleFee; }
    public JButton getBtnSave() { return btnSave; }
    public JButton getBtnClear() { return btnClear; }
    public JButton getBtnBack() { return btnBack; }
    public JButton getBtnFillDebug() { return btnFillDebug; }
    public JCheckBox getChkOnline() { return chkOnline; }
    public JComboBox<TeacherDTO> getCbTeacher() { return cbTeacher; }
    public JButton getBtnAddTeacher() { return btnAddTeacher; }
    public JButton getBtnUpdateTeacher() { return btnUpdateTeacher; }
    public JButton getBtnRemoveTeacher() { return btnRemoveTeacher; }
    public JTable getTblTeachers() { return tblTeachers; }
    public JSpinner getSpnDuration() { return spnDuration; }
    public JLabel getLblTeacherEditHint() { return lblTeacherEditHint; }
    public JTextField getTxtCourseNameField() { return txtCourseName; }
    public JTextField getTxtLocationField() { return txtLocation; }
    public JTextField getTxtRemunerationField() { return txtRemuneration; }
    public JTextArea getTxtObjectivesField() { return txtObjectives; }
    public JTextArea getTxtMainContentsField() { return txtMainContents; }
    public JTextField getTxtSpotsField() { return txtSpots; }
    public JTextField getTxtStartDateField() { return txtStartDate; }
    public JTextField getTxtEndDateField() { return txtEndDate; }
    public JTextField getTxtEnrolStartField() { return txtEnrolStart; }
    public JTextField getTxtEnrolEndField() { return txtEnrolEnd; }

    public String getTxtCourseName() { return txtCourseName.getText().trim(); }
    public String getTxtObjectives() { return txtObjectives.getText().trim(); }
    public String getTxtMainContents() { return txtMainContents.getText().trim(); }
    public String getTxtSpots() { return txtSpots.getText().trim(); }
    public String getTxtStartDate() { return txtStartDate.getText().trim(); }
    public String getTxtEndDate() { return txtEndDate.getText().trim(); }
    public String getTxtLocation() { return txtLocation.getText().trim(); }
    public String getTxtEnrolStart() { return txtEnrolStart.getText().trim(); }
    public String getTxtEnrolEnd() { return txtEnrolEnd.getText().trim(); }
    public JLabel getLblHintCourseName() { return lblHintCourseName; }
    public JLabel getLblHintSpots() { return lblHintSpots; }
    public JLabel getLblHintStartDate() { return lblHintStartDate; }
    public JLabel getLblHintEndDate() { return lblHintEndDate; }
    public JLabel getLblHintEnrolment() { return lblHintEnrolment; }
    public JLabel getLblHintTeacher() { return lblHintTeacher; }
    public JLabel getLblHintCommunity() { return lblHintCommunity; }
}