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

public class InscriptionsController {

    private InscriptionsModel model;
    private InscriptionsView view;
    private String simulatedDateStr;
    private ProfessionalDTO selectedProfessional; // Profesional recibido de SwingMain

    public InscriptionsController(InscriptionsModel m, InscriptionsView v){
        this.model = m;
        this.view = v;
    }

    // Método para inyectar el profesional desde SwingMain
    public void setSelectedProfessional(ProfessionalDTO p) {
        this.selectedProfessional = p;
    }
    
    public void initController(){
        this.initView(); 
        
        view.getBtnLoadCourses().addActionListener(
            e -> SwingUtil.exceptionWrapper(() -> loadCourses())
        );

        view.getBtnEnroll().addActionListener(
            e -> SwingUtil.exceptionWrapper(() -> processEnrollment())
        );
    
        view.getBtnBack().addActionListener(
            e -> view.getFrame().dispose()
        );
    }

    public void setSimulatedDate(String dateIso){
        this.simulatedDateStr = dateIso;
    }

    public void initView(){
        // Si hay un profesional seleccionado, rellenamos los campos automáticamente
        if (selectedProfessional != null) {
            view.setTxtName(selectedProfessional.getName());
            view.setTxtSurname(selectedProfessional.getSurname());
            view.setTxtPhone(selectedProfessional.getPhone());
            view.setTxtEmail(selectedProfessional.getEmail());
        }
        
        this.loadCourses();
        view.getFrame().setVisible(true); 
    }

    public void loadCourses(){
        Date simulatedDate = getHybridSimulatedDate();
        model.checkAndReleaseExpiredBookings(simulatedDate);
        
        List<FormativeActionDTO> courses = model.getAvailableCourses(simulatedDate);        
        String[] columnProperties = {"actionId", "name", "enrolmentPeriod", "fee", "availabilityStatus"};
        TableModel tmodel = SwingUtil.getTableModelFromPojos(courses, columnProperties);
        view.getTablaCursos().setModel(tmodel);
    
        view.getTablaCursos().getColumnModel().getColumn(1).setHeaderValue("Course Name");
        view.getTablaCursos().getColumnModel().getColumn(2).setHeaderValue("Enrollment Period");
        view.getTablaCursos().getColumnModel().getColumn(3).setHeaderValue("Fee (€)");
        view.getTablaCursos().getColumnModel().getColumn(4).setHeaderValue("Places");
        view.getTablaCursos().getColumnModel().getColumn(0).setMinWidth(0);
        view.getTablaCursos().getColumnModel().getColumn(0).setMaxWidth(0);
        view.getTablaCursos().getColumnModel().getColumn(0).setPreferredWidth(0);
    
        view.getTablaCursos().getColumnModel().getColumn(1).setPreferredWidth(200);
        view.getTablaCursos().getColumnModel().getColumn(2).setPreferredWidth(170);
        view.getTablaCursos().getColumnModel().getColumn(3).setPreferredWidth(70);
        view.getTablaCursos().getColumnModel().getColumn(4).setPreferredWidth(90);
    
        view.getTablaCursos().setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = table.getModel().getValueAt(row, 4).toString();
                if (!isSelected){ 
                    if ("Full".equals(status)){
                        c.setBackground(new Color(235, 235, 235));
                        c.setForeground(Color.GRAY);
                    }
                    else{
                        c.setBackground(table.getBackground());
                        if (column == 4) c.setForeground(new Color(0, 128, 0)); 
                        else c.setForeground(Color.BLACK);
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
            actionId = Integer.parseInt(view.getTablaCursos().getValueAt(selectedRow, 0).toString());
            feeStr = view.getTablaCursos().getValueAt(selectedRow, 3).toString(); 
        }catch (Exception e){
            throw new ApplicationException("Error reading course data.");
        }
    
        ProfessionalDTO alumno = new ProfessionalDTO();
        alumno.setName(view.getTxtName());
        alumno.setSurname(view.getTxtSurname());
        alumno.setPhone(view.getTxtPhone());
        alumno.setEmail(view.getTxtEmail());
        
        Date simulatedDate = getHybridSimulatedDate();
        model.enrollProfessional(alumno, actionId, simulatedDate);
        view.showSuccessMessage(feeStr);
        view.resetForm();
        loadCourses();
    }
    
    private Date getHybridSimulatedDate() {
        if (this.simulatedDateStr == null || this.simulatedDateStr.trim().isEmpty()) return new Date();
        Date baseDate;
        try {
            baseDate = Util.isoStringToDate(this.simulatedDateStr);
            if (baseDate == null) return new Date();
        } catch (Exception e) { return new Date(); }

        java.util.Calendar calSimulado = java.util.Calendar.getInstance();
        calSimulado.setTime(baseDate);
        java.util.Calendar calReal = java.util.Calendar.getInstance();
        calSimulado.set(java.util.Calendar.HOUR_OF_DAY, calReal.get(java.util.Calendar.HOUR_OF_DAY));
        calSimulado.set(java.util.Calendar.MINUTE, calReal.get(java.util.Calendar.MINUTE));
        calSimulado.set(java.util.Calendar.SECOND, calReal.get(java.util.Calendar.SECOND));
        return calSimulado.getTime();
    }
}