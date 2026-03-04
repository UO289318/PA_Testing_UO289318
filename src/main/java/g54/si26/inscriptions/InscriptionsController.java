package g54.si26.inscriptions;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableCellRenderer;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.DTOs.ProfessionalDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.SwingUtil;
import g54.si26.utils.Util;

//Controlador pa la US d'Inscripciones.

public class InscriptionsController {

    private InscriptionsModel model;
    private InscriptionsView view;
    private String simulatedDateStr;

    public InscriptionsController(InscriptionsModel m, InscriptionsView v){
        this.model = m;
        this.view = v;
    }

    //DEBUG FOR Multiple USERS
    public void initController(){
        this.initView(); 
        
        //Botón pa recargar la llistina de cursos según la fecha simulada
        view.getBtnLoadCourses().addActionListener(
            e -> SwingUtil.exceptionWrapper(() -> loadCourses())
        );

        //Botón p'arrincar el procesu d'inscripción
        view.getBtnEnroll().addActionListener(
            e -> SwingUtil.exceptionWrapper(() -> processEnrollment())
        );
        
        view.getBtnBack().addActionListener(
            e -> view.getFrame().dispose()
        );
            
        // Evento para autocompletar formulario al seleccionar usuario del Dropdown
        view.getCbUsuarios().addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED){
                ProfessionalDTO selectedUser = (ProfessionalDTO) view.getCbUsuarios().getSelectedItem();
                if(selectedUser != null){
                    view.setTxtName(selectedUser.getName());
                    view.setTxtSurname(selectedUser.getSurname());
                    view.setTxtPhone(selectedUser.getPhone());
                    view.setTxtEmail(selectedUser.getEmail());
                }
            }
        });
    }
    
    // Método para inyectar la fecha desde el SwingMain
    public void setSimulatedDate(String dateIso){
    		this.simulatedDateStr = dateIso;
    }

    public void initView(){
        // Cargar los usuarios en el dropdown
    		List<ProfessionalDTO> users = model.getAllProfessionals(); 
    		view.getCbUsuarios().addItem(null);//Elemento vacío por defecto
    		for(ProfessionalDTO u : users) 
    			view.getCbUsuarios().addItem(u);
        
        //Cargamos la tabla na más abrir la ventana
    		this.loadCourses();
        
        //Abre la ventana
    		view.getFrame().setVisible(true); 
    }


    public void loadCourses(){
    		Date simulatedDate = Util.isoStringToDate(this.simulatedDateStr);
        
        // Pasamos la fecha simulada para liberar las caducadas
    		model.checkAndReleaseExpiredBookings(simulatedDate);
        
        // Traemos los cursos
    		List<FormativeActionDTO> courses = model.getAvailableCourses(simulatedDate);        
    		String[] columnProperties = {"actionId", "name", "enrolmentPeriod", "fee", "availabilityStatus"};
        
        TableModel tmodel = SwingUtil.getTableModelFromPojos(courses, columnProperties);
        view.getTablaCursos().setModel(tmodel);
        
        // 2. CABECERAS LIMPIAS Y PROFESIONALES
        view.getTablaCursos().getColumnModel().getColumn(1).setHeaderValue("Course Name");
        view.getTablaCursos().getColumnModel().getColumn(2).setHeaderValue("Enrollment Period");
        view.getTablaCursos().getColumnModel().getColumn(3).setHeaderValue("Fee (€)");
        view.getTablaCursos().getColumnModel().getColumn(4).setHeaderValue("Places");
        
        
        // Ajuste manual d las columnns
        // ID is hidden for the review, onlfy for DEBUG
        view.getTablaCursos().getColumnModel().getColumn(0).setMinWidth(0);
        view.getTablaCursos().getColumnModel().getColumn(0).setMaxWidth(0);
        view.getTablaCursos().getColumnModel().getColumn(0).setPreferredWidth(0);
        
        //Name
        view.getTablaCursos().getColumnModel().getColumn(1).setPreferredWidth(200);
        
        // Enrolment period
        view.getTablaCursos().getColumnModel().getColumn(2).setPreferredWidth(170);
        
        //Fee and Places
        view.getTablaCursos().getColumnModel().getColumn(3).setPreferredWidth(70);
        view.getTablaCursos().getColumnModel().getColumn(4).setPreferredWidth(90);
        
        // Grey for Full courses, Green otherwise
        view.getTablaCursos().setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column){
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Read availabilityStatus, hidden for review, only for DEBUG
                String status = table.getModel().getValueAt(row, 4).toString();
                
                if (!isSelected){ 
                		//Full Courses
                    if ("Full".equals(status)){
                    		c.setBackground(new Color(235, 235, 235));
                    		c.setForeground(Color.GRAY);
                    }
                    else{
                    		c.setBackground(table.getBackground());
                    		// If it's the Places column, we add green color if there are still places
                        if (column == 4)
                            c.setForeground(new Color(0, 128, 0)); 
                         else 
                            c.setForeground(Color.BLACK);
                    }
                }
                else{
                		c.setForeground(table.getSelectionForeground());
                		c.setBackground(table.getSelectionBackground());
                }
                return c;
            }
        });
    }

   
    public void processEnrollment(){
    		int selectedRow = view.getTablaCursos().getSelectedRow(); 
        if(selectedRow == -1)
        		throw new ApplicationException("Please, select a Formative Action to progress");
        
        int actionId;
        String feeStr;
        try{
        		String selectedKey = view.getTablaCursos().getValueAt(selectedRow, 0).toString();
        		actionId = Integer.parseInt(selectedKey);
        		feeStr = view.getTablaCursos().getValueAt(selectedRow, 3).toString(); 
        }catch (Exception e){
        		throw new ApplicationException("Error reading course data from the table. Data might be corrupted.");
        }
        
        ProfessionalDTO alumno = new ProfessionalDTO();
        alumno.setName(view.getTxtName());
        alumno.setSurname(view.getTxtSurname());
        alumno.setPhone(view.getTxtPhone());
        alumno.setEmail(view.getTxtEmail());

        // Receive the simulated Date and send it to the enrollment
        Date simulatedDate = Util.isoStringToDate(this.simulatedDateStr);
        model.enrollProfessional(alumno, actionId, simulatedDate);
        view.showSuccessMessage(feeStr);
        view.resetForm();
        loadCourses();
    }
}