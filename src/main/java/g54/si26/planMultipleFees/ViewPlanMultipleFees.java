package g54.si26.planMultipleFees;

import g54.si26.DTOs.TeacherDTO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


public class ViewPlanMultipleFees {

    private JFrame frame;
    
    // Componentes d las Secciones 1-3
    private JTextField txtCourseName, txtSpots, txtStartDate, txtEndDate, txtLocation, txtRemuneration;
    private JTextArea txtObjectives, txtMainContents;
    private JTextField txtEnrolStart, txtEnrolEnd;
    private JSpinner spnDuration;
    private JCheckBox chkOnline;
    private JComboBox<TeacherDTO> cbTeacher;
    private JTable tblTeachers;
    private JButton btnAddTeacher, btnUpdateTeacher, btnRemoveTeacher;
    private JLabel lblTeacherEditHint; 

    // Componentes d la Sección 4 (Comunidades y Tasas Múltiples)
    private JTable tblCommunities;      
    private JButton btnDeleteCommunity, btnAddCommunity, btnUpdateComm; 
    private JTextField txtEditCommName, txtEditCommFee;  
    private JCheckBox chkSingleFee;     

    // Botones Globales (Toolbar d abajo)
    private JButton btnFillDebug, btnBack, btnClear, btnSave;

    //CODIGOD COLORES
                    
    private static final Color COLOR_DELETE = new Color(220, 53, 69);     
    private static final Color COLOR_LIGHT_BLUE = new Color(227, 242, 253);   
    private static final Color COLOR_LIGHT_GRAY = new Color(240, 244, 248);   
    private static final Color COLOR_BG  = Color.WHITE;
    private static final Color COLOR_SECTION_BG = new Color(225, 240, 255);    
    private static final Color COLOR_SUBSECTION_BG = new Color(255, 252, 235);    
    private static final Color COLOR_UPDATE = new Color(0, 85, 180);       
    private static final Color COLOR_INSERT = new Color(255, 140, 50);     
    private static final Color COLOR_CLEAR = new Color(255, 200, 0);    
    private static final Color COLOR_SEPARATOR = new Color(180, 205, 230);    
    
    
    
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 12);

    public ViewPlanMultipleFees() {
    		initialize();
    	}

    //MAIN WINDOW
    private void initialize() {
    		//Titulo y ventana
        frame = new JFrame("Plan New Formative Action - Multi Fee Edition");
        frame.setBounds(100, 100, 900, 950);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().setBackground(COLOR_BG);

        //Main panel
        JPanel mainPanel = new JPanel(null);
        mainPanel.setPreferredSize(new Dimension(860, 900));
        mainPanel.setBackground(COLOR_BG);
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // BACK BUTTON y PANEL
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(COLOR_BG);
        topPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_SEPARATOR)); 
        
        btnBack = createStyledButton("Back", COLOR_LIGHT_BLUE, COLOR_UPDATE, 0, 0, 80, 25);
        topPanel.add(btnBack);
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        // SECTION 1: GENERAL INFO
        JPanel p1 = createSection("1. General Information", 10, 10, 840, 200);
        addLabel(p1, "Course Name:", 15, 30, 120);
        txtCourseName = addTextField(p1, 140, 30, 680);
        addLabel(p1, "Objectives:", 15, 65, 120);
        txtObjectives = addTextArea(p1, 140, 65, 680, 45);
        addLabel(p1, "Main Contents:", 15, 120, 120);
        txtMainContents = addTextArea(p1, 140, 120, 680, 45);
        addLabel(p1, "Spots:", 15, 170, 120);
        txtSpots = addTextField(p1, 140, 165, 100);
        mainPanel.add(p1);

        //SECTION 2: SCHEDULE
        JPanel p2 = createSection("2. Location & Schedule", 10, 220, 840, 150);
        addLabel(p2, "Start (yyyy-MM-dd):", 15, 30, 130);
        txtStartDate = addTextField(p2, 145, 30, 150);
        addLabel(p2, "End (yyyy-MM-dd):", 350, 30, 130);
        txtEndDate = addTextField(p2, 480, 30, 150);
        addLabel(p2, "Duration (Hrs):", 15, 70, 130);
        spnDuration = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnDuration.setBounds(145, 70, 80, 25);
        spnDuration.setFont(FONT_REGULAR);
        p2.add(spnDuration);
        addLabel(p2, "Location:", 350, 70, 130);
        txtLocation = addTextField(p2, 480, 70, 340);
        chkOnline = new JCheckBox("Held Online");
        chkOnline.setBackground(COLOR_BG);
        chkOnline.setBounds(15, 110, 150, 25);
        chkOnline.setFont(FONT_REGULAR);
        p2.add(chkOnline);
        mainPanel.add(p2);

        //SECTION 3: TEACHERS
        JPanel p3 = createSection("3. Teachers", 10, 380, 840, 150);
        addLabel(p3, "Teacher:", 15, 30, 100);
        cbTeacher = new JComboBox<>();
        cbTeacher.setFont(FONT_REGULAR);
        cbTeacher.setBackground(Color.WHITE);
        cbTeacher.setBounds(115, 30, 280, 25);
        p3.add(cbTeacher);
        addLabel(p3, "Remuneration:", 15, 65, 100);
        txtRemuneration = addTextField(p3, 115, 65, 100);
        
        btnAddTeacher = createStyledButton("Add", COLOR_INSERT, Color.WHITE, 15, 100, 80, 25);
        btnUpdateTeacher = createStyledButton("Update", COLOR_UPDATE, Color.WHITE, 15, 100, 80, 25);
        // Only shows with update logic  
        btnUpdateTeacher.setVisible(false); 
        btnRemoveTeacher = createStyledButton("Remove", COLOR_DELETE, Color.WHITE, 105, 100, 90, 25);
        
        p3.add(btnAddTeacher); 
        p3.add(btnUpdateTeacher); 
        p3.add(btnRemoveTeacher);
        
        //Hiddent until a teaqcher is in the table
        lblTeacherEditHint = new JLabel("(i)Select a teacher from the grid to update remuneration");
        lblTeacherEditHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblTeacherEditHint.setForeground(new Color(100, 120, 140));
        lblTeacherEditHint.setBounds(420, 10, 350, 15);
        lblTeacherEditHint.setVisible(false);
        p3.add(lblTeacherEditHint);
        
        tblTeachers = new JTable(new DefaultTableModel(new String[]{"ID", "Name", "Remuneration (€)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        styleTable(tblTeachers);
        
        //Debug, row con ID
        tblTeachers.getColumnModel().getColumn(0).setMinWidth(0);
        tblTeachers.getColumnModel().getColumn(0).setMaxWidth(0);
        tblTeachers.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        JScrollPane spTeachers = new JScrollPane(tblTeachers);
        spTeachers.setBounds(420, 25, 400, 100);
        spTeachers.getViewport().setBackground(COLOR_BG);
        p3.add(spTeachers);
        mainPanel.add(p3);

        // SECTION 4: FEES PER COMMUNITY 
        JPanel p4 = createSection("4. Fees per Community Management", 10, 540, 840, 240);
        
        // Subsection Left - Current Communities Repository
        JPanel subList = createSubSection("Community Repository", 15, 25, 420, 200);
        tblCommunities = new JTable(new DefaultTableModel(new String[]{"ID", "Community Name", "Fee (€)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        styleTable(tblCommunities); 
        
        //Debug, hide ID for the review
        tblCommunities.getColumnModel().getColumn(0).setMinWidth(0);
        tblCommunities.getColumnModel().getColumn(0).setMaxWidth(0);
        tblCommunities.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        JScrollPane spComm = new JScrollPane(tblCommunities);
        spComm.setBounds(10, 25, 400, 130);
        spComm.getViewport().setBackground(COLOR_BG);
        subList.add(spComm);
        btnDeleteCommunity = createStyledButton("Delete Selected Community", COLOR_DELETE, Color.WHITE, 10, 165, 200, 25);
        subList.add(btnDeleteCommunity);
        p4.add(subList);

        // Subsection Right - Edit y Add panel
        JPanel subEdit = createSubSection("Edit / Set Fee", 445, 25, 380, 200);
        addLabel(subEdit, "Name:", 15, 30, 80);
        txtEditCommName = addTextField(subEdit, 100, 30, 260);
        addLabel(subEdit, "Fee (€):", 15, 70, 80);
        txtEditCommFee = addTextField(subEdit, 100, 70, 100);
        
        btnUpdateComm = createStyledButton("Update Selected", COLOR_UPDATE, Color.WHITE, 15, 110, 150, 25);
        subEdit.add(btnUpdateComm);
        btnAddCommunity = createStyledButton("+ Add New", COLOR_INSERT, Color.WHITE, 180, 110, 150, 25);
        subEdit.add(btnAddCommunity);
        
        JSeparator sep = new JSeparator();
        sep.setBounds(15, 155, 350, 10);
        subEdit.add(sep);
        
        chkSingleFee = new JCheckBox("Apply this fee to all table rows");
        chkSingleFee.setBackground(COLOR_BG);
        chkSingleFee.setBounds(15, 165, 300, 25);
        chkSingleFee.setFont(FONT_REGULAR);
        subEdit.add(chkSingleFee);
        p4.add(subEdit);
        
        mainPanel.add(p4);

        //SECTION 5: ENROLMENT PERIOD
        JPanel p5 = createSection("5. Enrolment Period", 10, 790, 840, 90);
        addLabel(p5, "Start (yyyy-MM-dd):", 15, 30, 150);
        txtEnrolStart = addTextField(p5, 165, 30, 200);
        addLabel(p5, "End (yyyy-MM-dd):", 390, 30, 150);
        txtEnrolEnd = addTextField(p5, 540, 30, 200);
        mainPanel.add(p5);

        //BOTTOM TOOLBAR
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        toolbar.setOpaque(false); 
        toolbar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_SEPARATOR));
        
        btnFillDebug = createStyledButton("Fill Debug Data", COLOR_LIGHT_GRAY, Color.DARK_GRAY, 0, 0, 150, 25);
        //Escondido pa la review
        btnFillDebug.setVisible(false);
        btnClear = createStyledButton("Clear", COLOR_CLEAR, Color.BLACK, 0, 0, 100, 25);
        btnSave = createStyledButton("Save Action", COLOR_UPDATE, Color.WHITE, 0, 0, 150, 25);
        
        toolbar.add(btnFillDebug); 
        toolbar.add(btnClear); 
        toolbar.add(btnSave);
        frame.getContentPane().add(toolbar, BorderLayout.SOUTH);
    }

    // =========================================================================
    //  HELPERS d ESTILO Y RENDERIZADO "WEB CARDS"
    // =========================================================================

    //draws a round border on each section acting as "cards"
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
            @Override
            public Insets getBorderInsets(Component c) { return new Insets(4, 10, 4, 10); }
            @Override
            public Insets getBorderInsets(Component c, Insets insets) {
                insets.set(4, 10, 4, 10);
                return insets;
            }
        };
    }

    private JPanel createSection(String title, int x, int y, int w, int h) {
        JPanel p = new JPanel(null) {
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
        p.setBounds(x, y, w, h);
        p.setBorder(BorderFactory.createTitledBorder(createRoundedBorder(COLOR_UPDATE, 2, 16), title, TitledBorder.LEFT, TitledBorder.TOP, FONT_BOLD, COLOR_UPDATE));
        return p;
    }

    private JPanel createSubSection(String title, int x, int y, int w, int h) {
        JPanel p = new JPanel(null) {
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
        p.setBounds(x, y, w, h);
        p.setBorder(BorderFactory.createTitledBorder(createRoundedBorder(new Color(230, 200, 150), 2, 12), title, TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.ITALIC, 11), COLOR_UPDATE));
        return p;
    }

    //Apply unified style to the tables
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

    private void addLabel(JPanel p, String text, int x, int y, int w) {
        JLabel l = new JLabel(text);
        l.setBounds(x, y, w, 25);
        l.setFont(FONT_REGULAR);
        p.add(l);
    }

    private JTextField addTextField(JPanel p, int x, int y, int w) {
        JTextField t = new JTextField();
        t.setBounds(x, y, w, 25);
        t.setFont(FONT_REGULAR);
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 210)),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        p.add(t);
        return t;
    }

    private JTextArea addTextArea(JPanel p, int x, int y, int w, int h) {
        JTextArea t = new JTextArea();
        t.setFont(FONT_REGULAR);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        JScrollPane s = new JScrollPane(t);
        s.setBounds(x, y, w, h);
        s.setBorder(BorderFactory.createLineBorder(new Color(200, 205, 210)));
        p.add(s);
        return t;
    }

    
    //apply the same style to all buttons
    private JButton createStyledButton(String text, Color bg, Color fg, int x, int y, int w, int h) {
        JButton b = new JButton(text);
        if (x != 0 || y != 0) b.setBounds(x, y, w, h);
        
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
    //Getter section
    //------------------
    //OBSOLETOS: Algunos getters q devuelven texto plano están en desuso porq el controlador lee el objeto d JTextField


    public JFrame getFrame() {
        return frame;
    }

    public JTable getTblCommunities() {
        return tblCommunities;
    }

    public JButton getBtnAddCommunity() {
        return btnAddCommunity;
    }

    public JButton getBtnDeleteCommunity() {
        return btnDeleteCommunity;
    }

    public JButton getBtnUpdateComm() {
        return btnUpdateComm;
    }

    public JTextField getTxtEditCommName() {
        return txtEditCommName;
    }

    public JTextField getTxtEditCommFee() {
        return txtEditCommFee;
    }

    public JCheckBox getChkSingleFee() {
        return chkSingleFee;
    }

    public JButton getBtnSave() {
        return btnSave;
    }

    public JButton getBtnClear() {
        return btnClear;
    }

    public JButton getBtnBack() {
        return btnBack;
    }

    public JButton getBtnFillDebug() {
        return btnFillDebug;
    }

    public JCheckBox getChkOnline() {
        return chkOnline;
    }

    public JComboBox<TeacherDTO> getCbTeacher() {
        return cbTeacher;
    }

    public JButton getBtnAddTeacher() {
        return btnAddTeacher;
    }

    public JButton getBtnUpdateTeacher() {
        return btnUpdateTeacher;
    }

    public JButton getBtnRemoveTeacher() {
        return btnRemoveTeacher;
    }

    public JTable getTblTeachers() {
        return tblTeachers;
    }

    public JSpinner getSpnDuration() {
        return spnDuration;
    }

    public JLabel getLblTeacherEditHint() {
        return lblTeacherEditHint;
    }

    public JTextField getTxtCourseNameField() {
        return txtCourseName;
    }

    public JTextField getTxtLocationField() {
        return txtLocation;
    }

    public JTextField getTxtRemunerationField() {
        return txtRemuneration;
    }

    public JTextArea getTxtObjectivesField() {
        return txtObjectives;
    }

    public JTextArea getTxtMainContentsField() {
        return txtMainContents;
    }

    public JTextField getTxtSpotsField() {
        return txtSpots;
    }

    public JTextField getTxtStartDateField() {
        return txtStartDate;
    }

    public JTextField getTxtEndDateField() {
        return txtEndDate;
    }

    public JTextField getTxtEnrolStartField() {
        return txtEnrolStart;
    }

    public JTextField getTxtEnrolEndField() {
        return txtEnrolEnd;
    }

    //String methods, most of them obsolete or repeated
    
    public String getTxtCourseName() {
        return txtCourseName.getText().trim();
    }

    public String getTxtObjectives() {
        return txtObjectives.getText().trim();
    }

    public String getTxtMainContents() {
        return txtMainContents.getText().trim();
    }

    public String getTxtSpots() {
        return txtSpots.getText().trim();
    }

    public String getTxtStartDate() {
        return txtStartDate.getText().trim();
    }

    public String getTxtEndDate() {
        return txtEndDate.getText().trim();
    }

    public String getTxtLocation() {
        return txtLocation.getText().trim();
    }

    public String getTxtEnrolStart() {
        return txtEnrolStart.getText().trim();
    }

    public String getTxtEnrolEnd() {
        return txtEnrolEnd.getText().trim();
    }
    
}