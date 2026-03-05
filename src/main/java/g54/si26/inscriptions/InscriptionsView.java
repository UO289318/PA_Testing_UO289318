package g54.si26.inscriptions;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.ListSelectionModel;
import g54.si26.DTOs.ProfessionalDTO;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.border.TitledBorder;
import java.awt.SystemColor;
import java.awt.Font;
import java.awt.Color;
import javax.swing.BorderFactory;


public class InscriptionsView {

    private JFrame frame;
    private JButton btnLoadCourses;
    private JTable tabCourses;
    
    private JComboBox<ProfessionalDTO> cbUsuarios; 
    private JTextField txtName;
    private JTextField txtSurname;
    private JTextField txtPhone;
    private JTextField txtEmail;
    
    private JButton btnEnroll;
    private JButton btnBack;
    
    private JPanel bottomPanel;

    public InscriptionsView(){
        initialize();
    }

    private void initialize(){
        frame = new JFrame();
        frame.setTitle("Enrol in a Formative Action");
        frame.setName("InscriptionsView");
        frame.setBounds(100, 100, 700, 520);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));

        // Back button
        JPanel topPanel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) topPanel.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        btnBack = new JButton("Back");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBack.setBackground(new Color(230, 230, 230));
        btnBack.setForeground(Color.BLACK);
        btnBack.setName("btnBack");
        topPanel.add(btnBack);

        btnLoadCourses = new JButton("Refresh Courses");
        btnLoadCourses.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLoadCourses.setBackground(new Color(230, 230, 230));
        btnLoadCourses.setForeground(Color.BLACK);
        btnLoadCourses.setName("btnLoadCourses");
        //topPanel.add(btnLoadCourses);

        // TABLE SECTION
        JPanel centerPanel = new JPanel();
        centerPanel.setBorder(new TitledBorder(null, "1. Available Formative Actions", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), null));
        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);
        centerPanel.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane = new JScrollPane();
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        tabCourses = new JTable();
        tabCourses.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabCourses.setName("tabCourses");
        tabCourses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabCourses.setRowSelectionAllowed(true);
        tabCourses.setSelectionBackground(SystemColor.textHighlight);
        tabCourses.setSelectionForeground(Color.WHITE);
        tabCourses.setRowHeight(24);
        tabCourses.setDefaultEditor(Object.class, null); 
        scrollPane.setViewportView(tabCourses);

        // FORM SECTION
        bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createTitledBorder(null, "2. Professional's Info", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), null));
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.setLayout(new BorderLayout(0, 10));

        JPanel formGridPanel = new JPanel();
        bottomPanel.add(formGridPanel, BorderLayout.CENTER);
        formGridPanel.setLayout(new GridLayout(5, 2, 10, 8));

        // DEBUG QUITAR LUEGO
        JLabel lblUserLoad = new JLabel("Load existing:");
        lblUserLoad.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formGridPanel.add(lblUserLoad);
        cbUsuarios = new JComboBox<ProfessionalDTO>();
        cbUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbUsuarios.setBackground(Color.WHITE);
        cbUsuarios.setName("cbUsuarios");
        formGridPanel.add(cbUsuarios);

        // Pal nombre
        JLabel lblName = new JLabel("Name:");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formGridPanel.add(lblName);
        txtName = new JTextField();
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtName.setBackground(Color.WHITE);
        txtName.setName("txtName");
        formGridPanel.add(txtName);
        txtName.setColumns(10);

        // Pal apellido
        JLabel lblSurname = new JLabel("Surname:");
        lblSurname.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formGridPanel.add(lblSurname);
        txtSurname = new JTextField();
        txtSurname.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSurname.setBackground(Color.WHITE);
        txtSurname.setName("txtSurname");
        formGridPanel.add(txtSurname);
        txtSurname.setColumns(10);

        // Pal teléfono
        JLabel lblPhone = new JLabel("Phone number:");
        lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formGridPanel.add(lblPhone);
        txtPhone = new JTextField();
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtPhone.setBackground(Color.WHITE);
        txtPhone.setName("txtPhone");
        formGridPanel.add(txtPhone);
        txtPhone.setColumns(10);

        //Pal email
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formGridPanel.add(lblEmail);
        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtEmail.setBackground(Color.WHITE);
        txtEmail.setName("txtEmail");
        formGridPanel.add(txtEmail);
        txtEmail.setColumns(10);

        // Pal enrol
        JPanel actionPanel = new JPanel();
        FlowLayout flowLayout_1 = (FlowLayout) actionPanel.getLayout();
        flowLayout_1.setAlignment(FlowLayout.RIGHT);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        btnEnroll = new JButton("ENROLL NOW");
        btnEnroll.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEnroll.setBackground(new Color(0, 120, 215)); 
        btnEnroll.setForeground(Color.WHITE);
        btnEnroll.setName("btnEnroll");
        actionPanel.add(btnEnroll);
    }

    
    public JFrame getFrame() {
    		return this.frame; 
    	}
    
    public JButton getBtnLoadCourses() {
    		return this.btnLoadCourses;
    	}
    
    public JTable getTablaCursos() { 
    		return this.tabCourses; 
    	}
    
    public JComboBox<ProfessionalDTO> getCbUsuarios() { 
    		return this.cbUsuarios; 
    	}
    
    public String getTxtName() {
    		return this.txtName.getText();
    	}
    
    public void setTxtName(String name) {
    		this.txtName.setText(name);
    	}
    
    public String getTxtSurname() {
    		return this.txtSurname.getText();
    	}
    
    public void setTxtSurname(String surname) {
    		this.txtSurname.setText(surname); 
    	}
    
    public String getTxtPhone() { 
    		return this.txtPhone.getText(); 
    	}
    
    public void setTxtPhone(String phone) { 
    		this.txtPhone.setText(phone); 
    	}
    
    public String getTxtEmail() {
    		return this.txtEmail.getText(); 
    	}
    
    public void setTxtEmail(String email) { 
    		this.txtEmail.setText(email); 
    	}

    public JButton getBtnEnroll() { 
    		return this.btnEnroll; 
    	}
    public JButton getBtnBack() { 
    		return this.btnBack; 
    	} 
    
    
    // FEEDBACK D LA UI
    public void updateFormTitle(String courseName){
        if (courseName == null || courseName.trim().isEmpty())
            bottomPanel.setBorder(BorderFactory.createTitledBorder(null, "2. Professional's Info", TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), null));
        else 
            // Cuando se selecciona el título cambia a azul pa destacar.
            bottomPanel.setBorder(BorderFactory.createTitledBorder(null, "Enrolling in: " + courseName, TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), new Color(0, 120, 215)));
        bottomPanel.repaint();
    }

    public void showSuccessMessage(String fee){
        String msg = "Enrolment successful!\n\n"
                   + "IMPORTANT:You have 48 working hours to make the payment\n"
                   + "by bank transfer of the amount of " + fee + " €.\n\n"
                   + "If the payment is not received in the given period, the place will be made available to another applicant.";
        
        JOptionPane.showMessageDialog(frame, msg, "Enrolment completed", JOptionPane.INFORMATION_MESSAGE);
    }

    public void resetForm(){
        this.setTxtName("");
        this.setTxtSurname("");
        this.setTxtPhone("");
        this.setTxtEmail("");
        this.cbUsuarios.setSelectedIndex(-1);
        this.tabCourses.clearSelection();
        this.updateFormTitle(""); 
    }
}