package g54.si26.inscriptions;

import g54.si26.DTOs.ProfessionalDTO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InscriptionsView {

    private JFrame frame;
    

    private JTable tabCourses;
    private JComboBox<ProfessionalDTO> cbUsuarios; 
    private JTextField txtName, txtSurname, txtPhone, txtEmail;
    private JTable tblCommunityFees;
    private JLabel lblCommunityHint; 
    private JButton btnEnroll, btnBack, btnLoadCourses;

    private static final Color COLOR_BG  = UIManager.getColor("Panel.background");
    private static final Color COLOR_SECTION_BG = COLOR_BG;//new Color(225, 240, 255);    
    private static final Color COLOR_SUBSECTION_BG = COLOR_BG;//new Color(255, 252, 235);    
    private static final Color COLOR_UPDATE = new Color(0, 85, 180);       
    private static final Color COLOR_INSERT = new Color(255, 140, 50);     
    private static final Color COLOR_CLEAR = new Color(255, 235, 100);    
    private static final Color COLOR_SEPARATOR = new Color(180, 205, 230);
    private static final Color COLOR_LIGHT_BLUE = new Color(227, 242, 253);
    
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);

    public InscriptionsView(){
        initialize();
    }

    private void initialize(){
        frame = new JFrame("Enrol in a Formative Action");
        frame.setName("InscriptionsView");
        frame.setBounds(100, 100, 850, 680);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().setBackground(COLOR_BG);

        // Panel principal
        JPanel mainPanel = new JPanel(null);
        mainPanel.setPreferredSize(new Dimension(810, 600));
        mainPanel.setBackground(COLOR_BG);
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        //TOP PANEL (BACK y REFRESH)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(COLOR_BG);
        topPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_SEPARATOR));
        
        btnBack = createStyledButton("Back", COLOR_LIGHT_BLUE, COLOR_UPDATE, 0, 0, 80, 25);
        btnLoadCourses = createStyledButton("Refresh Courses", COLOR_CLEAR, Color.DARK_GRAY, 0, 0, 150, 30);
        topPanel.add(btnBack);
        topPanel.add(btnLoadCourses);
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        //SECCION 1
        JPanel p1 = createSection("1. Available Formative Actions", 10, 15, 810, 250);
        tabCourses = new JTable();
        styleTable(tabCourses);
        tabCourses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane spCourses = new JScrollPane(tabCourses);
        spCourses.setBounds(15, 25, 780, 210);
        spCourses.getViewport().setBackground(Color.WHITE);
        spCourses.setBorder(BorderFactory.createLineBorder(COLOR_SEPARATOR));
        p1.add(spCourses);
        mainPanel.add(p1);

        // SECCION 2
        JPanel p2 = createSection("2. Professional's Info", 10, 280, 810, 240);
        
        //Subsecció: Personal Data
        JPanel subPersonal = createSubSection("Personal Data", 15, 25, 380, 200);
        addLabel(subPersonal, "Name:", 15, 30, 80);
        txtName = addTextField(subPersonal, 100, 30, 260);
        
        addLabel(subPersonal, "Surname:", 15, 70, 80);
        txtSurname = addTextField(subPersonal, 100, 70, 260);
        
        addLabel(subPersonal, "Phone:", 15, 110, 80);
        txtPhone = addTextField(subPersonal, 100, 110, 260);
        
        addLabel(subPersonal, "Email:", 15, 150, 80);
        txtEmail = addTextField(subPersonal, 100, 150, 260);
        p2.add(subPersonal);

        // Subsección: Community Fees
        JPanel subFees = createSubSection("Community Fees", 415, 25, 380, 200);
        lblCommunityHint = new JLabel("(!) Select a community fee from the grid");
        lblCommunityHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblCommunityHint.setForeground(Color.red);
        lblCommunityHint.setBounds(15, 20, 300, 20);
        subFees.add(lblCommunityHint);
        
        tblCommunityFees = new JTable(new DefaultTableModel(new String[]{"ID", "Community", "Fee (€)"}, 0){
            @Override public boolean isCellEditable(int r, int c){ return false; }
        });
        styleTable(tblCommunityFees);
        tblCommunityFees.getColumnModel().getColumn(0).setMinWidth(0);
        tblCommunityFees.getColumnModel().getColumn(0).setMaxWidth(0);
        tblCommunityFees.getColumnModel().getColumn(0).setPreferredWidth(0);
        tblCommunityFees.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane spFees = new JScrollPane(tblCommunityFees);
        spFees.setBounds(15, 45, 350, 140);
        spFees.getViewport().setBackground(Color.WHITE);
        spFees.setBorder(BorderFactory.createLineBorder(COLOR_SEPARATOR));
        subFees.add(spFees);
        p2.add(subFees);

        mainPanel.add(p2);

        //BOTTOM TOOLBAR
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        toolbar.setOpaque(false); 
        toolbar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_SEPARATOR));
        
        btnEnroll = createStyledButton("ENROLL NOW", COLOR_INSERT, Color.WHITE, 0, 0, 150, 30);
        toolbar.add(btnEnroll);
        frame.getContentPane().add(toolbar, BorderLayout.SOUTH);
    }
    
    //METODOS AUX DE ESTILO
    
    private javax.swing.border.Border createRoundedBorder(Color color, int thickness, int radius){
        return new javax.swing.border.AbstractBorder(){
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new java.awt.BasicStroke(thickness));
                int offset = thickness / 2;
                g2.drawRoundRect(x + offset, y + offset, width - thickness - 1, height - thickness - 1, radius, radius);
                g2.dispose();
            }
            @Override
            public Insets getBorderInsets(Component c){ return new Insets(4, 10, 4, 10); }
            @Override
            public Insets getBorderInsets(Component c, Insets insets){
                insets.set(4, 10, 4, 10);
                return insets;
            }
        };
    }

    private JPanel createSection(String title, int x, int y, int w, int h){
        JPanel p = new JPanel(null){
            @Override
            protected void paintComponent(Graphics g){
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
        p.setBorder(BorderFactory.createTitledBorder(createRoundedBorder(COLOR_UPDATE, 1, 16), title, TitledBorder.LEFT, TitledBorder.TOP, FONT_BOLD, COLOR_UPDATE));
        return p;
    }

    private JPanel createSubSection(String title, int x, int y, int w, int h){
        JPanel p = new JPanel(null){
            @Override
            protected void paintComponent(Graphics g){
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
        p.setBorder(BorderFactory.createTitledBorder(createRoundedBorder(new Color(230, 200, 150), 1, 12), title, TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.ITALIC, 11), COLOR_UPDATE));
        return p;
    }

    private void styleTable(JTable table){
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

    private void addLabel(JPanel p, String text, int x, int y, int w){
        JLabel l = new JLabel(text);
        l.setBounds(x, y, w, 25);
        l.setFont(FONT_REGULAR);
        l.setOpaque(false); 
        p.add(l);
    }

    private JTextField addTextField(JPanel p, int x, int y, int w){
        JTextField t = new JTextField();
        t.setBounds(x, y, w, 25);
        t.setFont(FONT_REGULAR);
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_SEPARATOR),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        p.add(t);
        return t;
    }

    private JButton createStyledButton(String text, Color bg, Color fg, int x, int y, int w, int h){
        JButton b = new JButton(text);
        if (x != 0 || y != 0) b.setBounds(x, y, w, h);
        b.setPreferredSize(new Dimension(w, h)); 
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(FONT_BOLD);
        b.setFocusPainted(false);
        Color borderColor = bg.equals(COLOR_BG) || bg.equals(COLOR_CLEAR) ? COLOR_SEPARATOR : bg.darker();
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor, 1), BorderFactory.createEmptyBorder(2, 15, 2, 15)));
        return b;
    }

    //FEEDBACK SECTION
    
    public void showSuccessMessage(String communityName, String fee, boolean isFree){
        String msg;
        if(isFree){
            msg = "Enrolment successful for: " + communityName + "\n\n"
                + "Since the fee is 0 €, your enrolment is automatically CONFIRMED.\nNo payment is required.";
        }
        else {
            msg = "Enrolment successful for: " + communityName + "\n\n"
                + "IMPORTANT: You have 48 working hours to make the payment\n"
                + "by bank transfer of the amount of " + fee + " €.\n\n"
                + "If the payment is not received in the given period, the place will be made available to another applicant.";
        }
        JOptionPane.showMessageDialog(frame, msg, "Enrolment completed", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String msg){
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void resetForm(){
        this.txtName.setText("");
        this.txtSurname.setText("");
        this.txtPhone.setText("");
        this.txtEmail.setText("");
        this.tabCourses.clearSelection();
        ((DefaultTableModel) this.tblCommunityFees.getModel()).setRowCount(0);
        this.lblCommunityHint.setVisible(true);
    }

   //  GETTERS
 
    public JFrame getFrame() { 
    		return this.frame; 
    	}
    public JButton getBtnLoadCourses() { 
    		return this.btnLoadCourses; 
    	}
    public JTable getTablaCursos() { 
    		return this.tabCourses;
    	}
    public JTable getTblCommunityFees() { 
    		return this.tblCommunityFees; 
    	}
    public JComboBox<ProfessionalDTO> getCbUsuarios() { 
    		return this.cbUsuarios; 
    	}
    public JButton getBtnEnroll() { 
    		return this.btnEnroll; 
    	}
    public JButton getBtnBack() { 
    		return this.btnBack; 
    	} 
    public JLabel getLblCommunityHint() { 
    		return this.lblCommunityHint; 
    	}
    
    public String getTxtName() { 
    		return this.txtName.getText().trim(); 
    	}
    public void setTxtName(String name) { 
    		this.txtName.setText(name); 
    	}
    public String getTxtSurname() { 
    		return this.txtSurname.getText().trim(); 
    	}
    public void setTxtSurname(String surname) { 
    		this.txtSurname.setText(surname); 
    	}
    public String getTxtPhone() { 
    		return this.txtPhone.getText().trim(); 
    	}
    public void setTxtPhone(String phone) { 
    		this.txtPhone.setText(phone);
    	}
    public String getTxtEmail() { 
    		return this.txtEmail.getText().trim(); 
    	}
    public void setTxtEmail(String email) { 
    		this.txtEmail.setText(email); 
    	}
}