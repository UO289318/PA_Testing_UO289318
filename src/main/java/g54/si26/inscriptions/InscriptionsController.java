package g54.si26.inscriptions;

import java.awt.Color;
import java.awt.Component;
import java.util.Date;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;
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
    	private ProfessionalDTO selectedProfessional;

    	public InscriptionsController(InscriptionsModel m, InscriptionsView v){
    		this.model = m;
    		this.view = v;
    	}

    	public void setSelectedProfessional(ProfessionalDTO p){
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

    		// Listener: Cargar tasas al seleccionar un curso
    		view.getTablaCursos().getSelectionModel().addListSelectionListener(e -> {
    			if(!e.getValueIsAdjusting()){
    				int row = view.getTablaCursos().getSelectedRow();
    				if(row != -1){
    					int actionId = Integer.parseInt(view.getTablaCursos().getValueAt(row, 0).toString());
    					loadCommunityFees(actionId);
    					view.getLblCommunityHint().setVisible(true);
    				}
    				else{
    					((DefaultTableModel) view.getTblCommunityFees().getModel()).setRowCount(0);
    					view.getLblCommunityHint().setVisible(true);
    				}
    			}
    		});

    		// Listener pa ocultar aviso al seleccionar una comunidad
    		view.getTblCommunityFees().getSelectionModel().addListSelectionListener(e -> {
    			if(!e.getValueIsAdjusting()){
    				int row = view.getTblCommunityFees().getSelectedRow();
    				view.getLblCommunityHint().setVisible(row == -1);
    			}
    		});
    	}

    	public void setSimulatedDate(String dateIso){
    		this.simulatedDateStr = dateIso;
    	}

    	public void initView(){
    		if(selectedProfessional != null){
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
    		
    		String[] columnProperties = {"actionId", "name", "enrolmentPeriod", "availabilityStatus"};
    		TableModel tmodel = SwingUtil.getTableModelFromPojos(courses, columnProperties);
    		view.getTablaCursos().setModel(tmodel);
    		
    		view.getTablaCursos().getColumnModel().getColumn(1).setHeaderValue("Course Name");
    		view.getTablaCursos().getColumnModel().getColumn(2).setHeaderValue("Enrollment Period");
    		view.getTablaCursos().getColumnModel().getColumn(3).setHeaderValue("Places");
    		
    		view.getTablaCursos().getColumnModel().getColumn(0).setMinWidth(0);
    		view.getTablaCursos().getColumnModel().getColumn(0).setMaxWidth(0);
    		view.getTablaCursos().getColumnModel().getColumn(0).setPreferredWidth(0);
    		
    		view.getTablaCursos().getColumnModel().getColumn(1).setPreferredWidth(350);
    		view.getTablaCursos().getColumnModel().getColumn(2).setPreferredWidth(200);
    		view.getTablaCursos().getColumnModel().getColumn(3).setPreferredWidth(100);
    		
    		view.getTablaCursos().setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
    			@Override
    			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
    				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    				String status = table.getModel().getValueAt(row, 3).toString();
    				if(!isSelected){ 
    					if("Full".equals(status)){
    						c.setBackground(new Color(235, 235, 235));
                        	c.setForeground(Color.GRAY);
                    	}
                    	else{
                    		c.setBackground(table.getBackground());
                        	if(column == 3)
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

    	private void loadCommunityFees(int actionId){
    		List<Object[]> fees = model.getCourseFees(actionId);
    		DefaultTableModel tm = (DefaultTableModel) view.getTblCommunityFees().getModel();
    		tm.setRowCount(0);
    		for(Object[] row : fees) 
    			tm.addRow(row); 
    	}

    	public void processEnrollment(){
    		int selectedCourseRow = view.getTablaCursos().getSelectedRow(); 
    		if(selectedCourseRow == -1)
    			throw new ApplicationException("Please, select a Formative Action from the top table to progress.");
            	
    		int selectedCommunityRow = view.getTblCommunityFees().getSelectedRow();
    		if(selectedCommunityRow == -1)
    			throw new ApplicationException("Please, select your Profile (Community) to apply the correct fee.");
    		
    		int actionId;
    		int communityId;
    		String communityName;
    		String feeStr;
    		double feeAmount;
    		
    		try{
    			actionId = Integer.parseInt(view.getTablaCursos().getValueAt(selectedCourseRow, 0).toString());
    			communityId = Integer.parseInt(view.getTblCommunityFees().getValueAt(selectedCommunityRow, 0).toString());
    			communityName = view.getTblCommunityFees().getValueAt(selectedCommunityRow, 1).toString();
    			feeStr = view.getTblCommunityFees().getValueAt(selectedCommunityRow, 2).toString(); 
    			feeAmount = Double.parseDouble(feeStr);
    		}	
    		catch (Exception e){
    			throw new ApplicationException("Error reading course or fee data.");
    		}	
    
    		ProfessionalDTO alumno = new ProfessionalDTO();
    		alumno.setName(view.getTxtName());
    		alumno.setSurname(view.getTxtSurname());
    		alumno.setPhone(view.getTxtPhone());
    		alumno.setEmail(view.getTxtEmail());
    		
    		Date simulatedDate = getHybridSimulatedDate();
    		
    		model.enrollProfessional(alumno, actionId, communityId, simulatedDate);
    		view.showSuccessMessage(communityName, feeStr, feeAmount == 0.0);
    		view.resetForm();
    		loadCourses();
    	}	
    
    	private Date getHybridSimulatedDate(){
    		if(this.simulatedDateStr == null || this.simulatedDateStr.trim().isEmpty())
    			return new Date();
    		Date baseDate;
    		try {
    			baseDate = Util.isoStringToDate(this.simulatedDateStr);
    			if(baseDate == null)
    				return new Date();
    		}	
    		catch (Exception e){
    			return new Date();
        	}

    		java.util.Calendar calSimulado = java.util.Calendar.getInstance();
    		calSimulado.setTime(baseDate);
    		java.util.Calendar calReal = java.util.Calendar.getInstance();
    		calSimulado.set(java.util.Calendar.HOUR_OF_DAY, calReal.get(java.util.Calendar.HOUR_OF_DAY));
    		calSimulado.set(java.util.Calendar.MINUTE, calReal.get(java.util.Calendar.MINUTE));
    		calSimulado.set(java.util.Calendar.SECOND, calReal.get(java.util.Calendar.SECOND));
    		return calSimulado.getTime();
    	
    	}	
}