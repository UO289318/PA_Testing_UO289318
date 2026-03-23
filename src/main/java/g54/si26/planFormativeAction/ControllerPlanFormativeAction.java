package g54.si26.planFormativeAction;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.DTOs.TeacherDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.SwingUtil;
import g54.si26.utils.Util;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class ControllerPlanFormativeAction {

    private final ModelPlanFormativeAction model;
    private final ViewPlanFormativeAction  view;
    private String simulatedDateStr;

    public ControllerPlanFormativeAction(ModelPlanFormativeAction model, ViewPlanFormativeAction  view){
        this.model = model;
        this.view  = view;
    }

    public void initController(){
        loadTeachers();
        view.getFrame().setVisible(true);

        view.getBtnBack().addActionListener(e -> view.getFrame().dispose());
        view.getBtnClear().addActionListener(e -> SwingUtil.exceptionWrapper(() -> clearForm()));
        view.getBtnSave().addActionListener(e -> SwingUtil.exceptionWrapper(() -> saveFormativeAction()));
        view.getBtnAddTeacher().addActionListener(e -> SwingUtil.exceptionWrapper(() -> addTeacherToGrid()));
        view.getBtnRemoveTeacher().addActionListener(e -> SwingUtil.exceptionWrapper(() -> removeTeacherFromGrid()));

        //Listener pa validar el nombre
        view.getTxtCourseNameField().addFocusListener(new FocusAdapter(){
            @Override
            public void focusLost(FocusEvent e){
                String name = view.getTxtCourseName();
                String simulatedToday = ControllerPlanFormativeAction.this.getSimulatedDateStr();
                
                	if (!name.isEmpty() && model.nameExistsInPastOrPresent(name, simulatedToday))
                		view.showError("The name '" + name + "' already exists created today or in the past and is blocked.");
                
            	}
        	});

        	view.getChkFreeCourse().addItemListener(e -> {
        		boolean isFree = (e.getStateChange() == ItemEvent.SELECTED);
        		view.getTxtFeeField().setEnabled(!isFree);
        		if (isFree) 
        			view.getTxtFeeField().setText("0");
        });

        	view.getChkOnline().addItemListener(e -> {
        		boolean isOnline = (e.getStateChange() == ItemEvent.SELECTED);
        		view.getTxtLocationField().setEnabled(!isOnline);
        		if (isOnline)
        			view.getTxtLocationField().setText("");
        });

        view.getTxtStartDateField().addFocusListener(new FocusAdapter(){
            @Override
            	public void focusLost(FocusEvent e){
            		refreshEnrolWarning();
            	}
        	});
        
        	view.getBtnFillDebug().addActionListener(e -> SwingUtil.exceptionWrapper(() -> fillDebugData()));
    	}

    	public void setSimulatedDate(String dateIso){
    		this.simulatedDateStr = dateIso;
    	}

    private void loadTeachers(){
        List<TeacherDTO> teachers = model.getAllTeachers();
        view.getCbTeacher().removeAllItems();
        view.getCbTeacher().addItem(null);
        for (TeacherDTO t : teachers)
        		view.getCbTeacher().addItem(t);
    }

    	private void refreshEnrolWarning(){
    		boolean ok=model.enrolmentMeetsLeadTimeRule(view.getTxtEnrolStart(), view.getTxtStartDate());
    		view.getLblEnrolWarning().setVisible(!ok);
    	}

    	private void addTeacherToGrid(){
    		TeacherDTO selected = (TeacherDTO) view.getCbTeacher().getSelectedItem();
    		if (selected== null)
    			throw new ApplicationException("Please select a teacher.");
        try{
            double rem = Double.parseDouble(view.getTxtRemuneration().replace(",", "."));
            if (rem < 0)
            		throw new Exception();
            DefaultTableModel tm = (DefaultTableModel) view.getTblTeachers().getModel();
            for(int i = 0; i < tm.getRowCount(); i++)
            		if (Integer.parseInt(tm.getValueAt(i, 0).toString()) == selected.getTeacherId())
                    throw new ApplicationException("Teacher already added.");
            
            tm.addRow(new Object[]{selected.getTeacherId(), selected.getName(), rem});
            view.setTxtRemuneration("");
        } catch (ApplicationException ae){ 
        		throw ae;
        } catch (Exception ex){ 
        		throw new ApplicationException("Invalid remuneration."); 
        	}
    }

    private void removeTeacherFromGrid(){
        int row=view.getTblTeachers().getSelectedRow();
        if (row ==-1)
        		throw new ApplicationException("Select a teacher to remove.");
        ((DefaultTableModel) view.getTblTeachers().getModel()).removeRow(row);
    }

    private void clearForm(){
        	view.setTxtCourseName("");
        	view.setTxtObjectives("");
        	view.setTxtMainContents("");
        	view.setTxtSpots("");
        	view.setTxtStartDate("");
        	view.setTxtEndDate("");
        	view.setTxtLocation("");
        	view.getTxtLocationField().setEnabled(true);
        	view.getSpnDuration().setValue(1);
        	view.getChkOnline().setSelected(false);
        	view.getCbTeacher().setSelectedIndex(-1);
        	view.setTxtRemuneration("");
        	((DefaultTableModel) view.getTblTeachers().getModel()).setRowCount(0);
        	view.setTxtFee("");
        	view.getTxtFeeField().setEnabled(true);
        	view.getChkFreeCourse().setSelected(false);
        	view.setTxtEnrolStart("");
        	view.setTxtEnrolEnd("");
        	view.getLblEnrolWarning().setVisible(false);
    	}

    private void saveFormativeAction() {
        FormativeActionDTO dto = new FormativeActionDTO();
        ModelPlanFormativeAction.ValidationResult preCheck = new ModelPlanFormativeAction.ValidationResult();

        dto.setName(view.getTxtCourseName());
        dto.setObjectives(view.getTxtObjectives());
        dto.setMainContents(view.getTxtMainContents());
        dto.setStartDate(view.getTxtStartDate());
        dto.setEndDate(view.getTxtEndDate());
        dto.setInscriptionPeriodStart(view.getTxtEnrolStart());
        dto.setInscriptionPeriodEnd(view.getTxtEnrolEnd());

        // LOGIC SYNC: Online logic
        if (view.getChkOnline().isSelected())
            dto.setLocation("Online");
        else 
            dto.setLocation(view.getTxtLocation());

        try {
            String s = view.getTxtSpots();
            dto.setSpots(s.isEmpty() ? 0 : Integer.parseInt(s));
        } catch (NumberFormatException e) {
            preCheck.errors.add("Places must be a valid number.");
        }

        // LOGIC SYNC: Duration integer validation
        try {
            Object value = view.getSpnDuration().getValue();
            dto.setNumberOfHours(Integer.parseInt(value.toString()));
        } catch (Exception e) {
            preCheck.errors.add("Duration must be a valid integer number.");
            dto.setNumberOfHours(0);
        }

        try {
            String f = view.getTxtFee().replace(",", ".");
            dto.setFee(f.isEmpty() ? -1 : Double.parseDouble(f));
        } catch (NumberFormatException e) {
            preCheck.errors.add("Fee must be a valid number.");
        }

        DefaultTableModel tmodel = (DefaultTableModel) view.getTblTeachers().getModel();
        if (tmodel.getRowCount() == 0) {
            preCheck.errors.add("At least one teacher must be assigned.");
        } else {
            // First teacher for DTO compatibility, but model will loop through table
            dto.setTeacherId(Integer.parseInt(tmodel.getValueAt(0, 0).toString()));
            dto.setInitialPayment(Double.parseDouble(tmodel.getValueAt(0, 2).toString()));
        }

        ModelPlanFormativeAction.ValidationResult finalResult = model.validate(dto, getSimulatedDateStr());
        finalResult.errors.addAll(preCheck.errors);

        if (finalResult.hasErrors()) {
            view.showValidationSummary(finalResult.errors, finalResult.warnings);
            return;
        }

        // LOGIC SYNC: Popup warning if < 3 weeks
        if (finalResult.hasWarnings()) {
            StringBuilder sb = new StringBuilder("The following warnings were detected:\n\n");
            for (String w : finalResult.warnings) sb.append(" - ").append(w).append("\n");
            sb.append("\nDo you want to proceed and save anyway?");
            
            int choice = JOptionPane.showConfirmDialog(view.getFrame(), sb.toString(), 
                    "Validation Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (choice == JOptionPane.NO_OPTION) return;
        }

        // Pass the tmodel to the model
        model.addFormativeAction(dto, getSimulatedDateStr(), tmodel);
        view.showSuccess(dto.getName());
        clearForm();
    }

    	private String getSimulatedDateStr() {
            if (simulatedDateStr == null || simulatedDateStr.trim().isEmpty()) {
                return Util.dateToIsoString(new Date()); // Fecha real formateada
            }
            try {
                // Intentamos parsear para validar el formato ISO
                Date parsed = Util.isoStringToDate(simulatedDateStr);
                return (parsed != null) ? Util.dateToIsoString(parsed) : Util.dateToIsoString(new Date());
            } catch (Exception e) {
                return Util.dateToIsoString(new Date());
            }
        }

	private void fillDebugData(){
		String today = getSimulatedDateStr();
		java.time.LocalDate base = java.time.LocalDate.parse(today.substring(0, 10));
		view.setTxtCourseName("Lorem Ipsum Advanced Training");
		view.setTxtObjectives("Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
            + "Participants will gain proficiency in lorem ipsum techniques.");
		view.setTxtMainContents("Unit 1: Lorem Ipsum Basics\n"
            + "Unit 2: Dolor Sit Amet Patterns\n"
            + "Unit 3: Consectetur Advanced Topics");
		view.setTxtSpots("20");
		view.setTxtStartDate(base.plusMonths(2).toString());
		view.setTxtEndDate(base.plusMonths(2).plusDays(1).toString());
		view.setTxtLocation("Lorem Ipsum Classroom 1");
		view.setTxtFee("100");
		view.setTxtEnrolStart(base.plusDays(1).toString());
		view.setTxtEnrolEnd(base.plusMonths(1).toString());
	}
}