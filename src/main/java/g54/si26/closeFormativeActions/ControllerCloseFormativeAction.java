package g54.si26.closeFormativeActions;

import java.util.Date;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import g54.si26.DTOs.*;
import g54.si26.utils.SwingUtil;
import g54.si26.utils.Util;

public class ControllerCloseFormativeAction{
    
    	private ViewCloseFormativeAction view;
    	private ModelCloseFormativeAction model;
    	private String simulatedDateStr;
    
    	private CloseValidationDTO currentValidation;

    	public ControllerCloseFormativeAction(ViewCloseFormativeAction view, ModelCloseFormativeAction model, String simulatedDateStr){
    		this.view=view;
    		this.model=model;
    		this.simulatedDateStr=simulatedDateStr;
    	}

    	public void initController(){
    		loadCoursesToClose();
        view.getTabCourses().getSelectionModel().addListSelectionListener(e -> {
        		if (!e.getValueIsAdjusting())
        			updateValidationTable();
        });

        	view.getBtnCloseAction().addActionListener(e -> processClosure());
        	view.getBtnBack().addActionListener(e -> view.getFrame().dispose());
    	}

    	private void loadCoursesToClose() {
    		//We use the date and we pass it to the model
    		Date simulatedDate = getSafeSimulatedDate();
    		List<FormativeActionDTO> activeCourses = model.getUnclosedCourses(simulatedDate);
        
    		String[] columnProperties = {"actionId", "name", "endDate", "unhandledRegistrations", "teacherInvoicesStatus"};
        
    		TableModel tmodel = SwingUtil.getTableModelFromPojos(activeCourses, columnProperties);
    		view.getTabCourses().setModel(tmodel);
        
    		view.getTabCourses().getColumnModel().getColumn(1).setHeaderValue("Course Name");
    		view.getTabCourses().getColumnModel().getColumn(2).setHeaderValue("End Date");
    		view.getTabCourses().getColumnModel().getColumn(3).setHeaderValue("Unhandled Regs.");
    		view.getTabCourses().getColumnModel().getColumn(4).setHeaderValue("Teacher Invoice");
    		
    		view.getTabCourses().getColumnModel().getColumn(0).setMinWidth(0);
    		view.getTabCourses().getColumnModel().getColumn(0).setMaxWidth(0);
        
    		view.getTabCourses().getColumnModel().getColumn(1).setPreferredWidth(200);
    		view.getTabCourses().getColumnModel().getColumn(3).setPreferredWidth(120);
    		view.getTabCourses().getColumnModel().getColumn(4).setPreferredWidth(120);
        
    		clearValidationTable();
    	}

    	private void updateValidationTable(){
        	int selectedRow=view.getTabCourses().getSelectedRow();
        
        	if (selectedRow == -1){
        		clearValidationTable();
        		return;
        	}

        	int actionId=Integer.parseInt(view.getTabCourses().getValueAt(selectedRow, 0).toString());
        
        	//Get date
        	Date simulatedDate = getSafeSimulatedDate();
        
        	currentValidation = model.validateClosure(actionId, simulatedDate);
        	DefaultTableModel valModel=new DefaultTableModel(new String[]{"Type", "Message"}, 0);

        	for (String error : currentValidation.getErrors())
                	valModel.addRow(new Object[]{"BLOCKED", error});
        
        	for (String warning : currentValidation.getWarnings())
                	valModel.addRow(new Object[]{"WARNING!", warning});
        
        	if (currentValidation.isCanClose() && currentValidation.getWarnings().isEmpty())
                	valModel.addRow(new Object[]{"READY!", "All checks passed. Ready to close."});
        
        	view.getTabValidation().setModel(valModel);
        	view.getTabValidation().getColumnModel().getColumn(0).setPreferredWidth(100);
        	view.getTabValidation().getColumnModel().getColumn(0).setMaxWidth(120);
        	view.getBtnCloseAction().setEnabled(currentValidation.isCanClose());
    }
    
    //clear the table
    private void clearValidationTable(){
        	view.getTabValidation().setModel(new DefaultTableModel(new String[]{"Type", "Message"}, 0));
        	view.getBtnCloseAction().setEnabled(false);
        	currentValidation= null;
    }

    //Whenever we want to close the FA
    private void processClosure(){
    		if (currentValidation == null || !currentValidation.isCanClose())
        		return;

        	if (!currentValidation.getWarnings().isEmpty()){
        		boolean isConfirmed=view.showWarningConfirmation();
        		if (!isConfirmed)
        			return;
        }

        	int selectedRow=view.getTabCourses().getSelectedRow();
        	int actionId=Integer.parseInt(view.getTabCourses().getValueAt(selectedRow, 0).toString());
        	String closureDateStr = Util.dateToIsoString(getSafeSimulatedDate());
        	boolean success=model.executeClosure(actionId, closureDateStr);
        
        	if (success){
        		view.showSuccessMessage();
        		loadCoursesToClose(); 
        	}
        	else
        		view.showError("Database error during closure.");
        
    }
    
    //Just in case someone inputs the wrong date
    	private Date getSafeSimulatedDate() {
    		//If no date
    		if (this.simulatedDateStr == null || this.simulatedDateStr.trim().isEmpty())
    			return new Date();
    		try {
    			//Try the correct format
    			Date parsedDate = Util.isoStringToDate(this.simulatedDateStr);
    			return (parsedDate != null) ? parsedDate : new Date();
    		} catch (Exception e) {
    			return new Date(); 
    		}
    }
}