package g54.si26.consultFormativeActionsSecretary;

import g54.si26.DTOs.FormativeActionDetailsDTO;
import g54.si26.DTOs.FormativeActionManagementDTO;
import g54.si26.utils.SwingUtil;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import g54.si26.secretaryStatusFA.*;

public class ControllerConsultFormativeActions {

    private final ModelConsultFormativeActions model;
    private final ViewConsultFormativeActions view;
    private String simulatedDateStr;
    
    

    public String getSimulatedDateStr() {
		return simulatedDateStr;
	}

	public void setSimulatedDateStr(String simulatedDateStr) {
		this.simulatedDateStr = simulatedDateStr;
	}

	public ControllerConsultFormativeActions(ModelConsultFormativeActions model, ViewConsultFormativeActions view){
        this.model =model;
        this.view = view;
    }

    public void initController(){
    		view.getTxtDateFilter().setText(getSimulatedDateStr());
    		//By default it shows the Active FA
    		loadFormativeActions(null, getSimulatedDateStr());
        view.getFrame().setVisible(true);

        // For back and refresh
        view.getBtnBack().addActionListener(e -> view.getFrame().dispose());
        
        view.getBtnRefresh().addActionListener(e -> SwingUtil.exceptionWrapper(this::handleRefresh));
        
        view.getBtnSearch().addActionListener(e -> SwingUtil.exceptionWrapper(this::handleSearch));
        view.getBtnStatusFA().addActionListener(e -> {
            
            StatusFAModel statusModel = new StatusFAModel();
            StatusFAView statusView = new StatusFAView();
            StatusFAController statusController = new StatusFAController(statusModel, statusView);
            
            statusController.setSimulatedDate(getSimulatedDateStr()); 
            statusController.initController();
        });
        view.getBtnExecuteTMConsult().addActionListener(e -> {
            g54.si26.tmConsulting.TMConsultingModel tmModel = new g54.si26.tmConsulting.TMConsultingModel();
            g54.si26.tmConsulting.TMConsultingView tmView = new g54.si26.tmConsulting.TMConsultingView();
            g54.si26.tmConsulting.TMConsultingController tmController = new g54.si26.tmConsulting.TMConsultingController(tmModel, tmView);
            
            tmController.setSimulatedDate(getSimulatedDateStr());
            tmController.initController(); 
        });

        // For further info
        view.getTblFormativeActions().getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()){
                SwingUtil.exceptionWrapper(this::handleRowSelection);
            }
        });
    }

    
    //Extracts filters from View and asks the info to the model
    private void handleSearch(){
        String dateFilter=view.getTxtDateFilter().getText().trim();
        if(dateFilter.isEmpty()){
            dateFilter = getSimulatedDateStr();
            view.getTxtDateFilter().setText(dateFilter);
        }
        String statusFilter=null;
        String selectedCombo = (String) view.getCbStatusFilter().getSelectedItem();

        // Gets the option from combobox
        if(selectedCombo != null){
            if(selectedCombo.startsWith("ACTIVE (Default)"))
                statusFilter = null; 
            else if(selectedCombo.equals("ALL")) 
                statusFilter = "ALL"; 
            else 
                statusFilter = selectedCombo; // "PLANNING", "ENROLLMENT_OPEN", "FINSHED", "IN PROGRES" y "CLOSED"           
        }
        
        loadFormativeActions(statusFilter, dateFilter);
        //resets the grid after changing the filters
        clearDetailsSection(); 
    }

    
    //Refresh btn logic
    private void handleRefresh(){
    		view.getTxtDateFilter().setText(getSimulatedDateStr());
    		view.getCbStatusFilter().setSelectedIndex(0);
    		loadFormativeActions(null, getSimulatedDateStr());
    		clearDetailsSection();
    }

   
    //Loads the info from the model
    private void loadFormativeActions(String status, String date){
        List<FormativeActionManagementDTO> actions = model.getFormativeActions(status, date);
        DefaultTableModel tm = (DefaultTableModel) view.getTblFormativeActions().getModel();
        tm.setRowCount(0); 

        for(FormativeActionManagementDTO action : actions)
            tm.addRow(new Object[]{action.getActionId(), action.getName(),action.getStatus(),action.getEnrolmentPeriod(), action.getTotalPlaces(),action.getPlacesLeft(), action.getReservedPlaces(), action.getConfirmedPlaces(), action.getActionDate(), String.format("%.2f", action.getIncome()),String.format("%.2f", action.getExpenses()),String.format("%.2f", action.getBalance())});
        
    }

    
    //Whenever s row is selected, gets the id and asks for the details section
    private void handleRowSelection(){
        int row = view.getTblFormativeActions().getSelectedRow();
        
        if(row==-1){
            clearDetailsSection();
            return;
        }

        //Gets the ID (col 0)
        int actionId=(int) view.getTblFormativeActions().getValueAt(row, 0);

        // ask the details
        FormativeActionDetailsDTO details = model.getActionDetails(actionId);

        if(details!=null){
            view.getTxtObjectives().setText(details.getObjectives() != null ? details.getObjectives() : "N/A");
            view.getTxtMainContents().setText(details.getMainContents() != null ? details.getMainContents() : "N/A");
            view.getTxtLocation().setText(details.getLocation() != null ? details.getLocation() : "N/A");
            view.getTxtTeachers().setText(details.getTeachers() != null ? details.getTeachers() : "No teachers assigned");
            view.getTxtTotalRegisters().setText(String.valueOf(details.getTotalRegisters()));
            
            List<Object[]> fees = model.getCourseFees(actionId);
            DefaultTableModel tmFees = (DefaultTableModel) view.getTblCommunityFees().getModel();
            tmFees.setRowCount(0);
            for(Object[] feeRow : fees) 
                tmFees.addRow(feeRow);
            
            
            view.setDetailsPanelVisible(true);
        } else 
            clearDetailsSection();
        
    }

    
    //Clear details
    private void clearDetailsSection(){
        view.getTxtObjectives().setText("");
        view.getTxtMainContents().setText("");
        view.getTxtLocation().setText("");
        view.getTxtTeachers().setText("");
        view.getTxtTotalRegisters().setText("");
        ((DefaultTableModel) view.getTblCommunityFees().getModel()).setRowCount(0);
        view.setDetailsPanelVisible(false);
    }
}