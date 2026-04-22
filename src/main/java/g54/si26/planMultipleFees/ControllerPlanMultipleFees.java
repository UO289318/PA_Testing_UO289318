package g54.si26.planMultipleFees;

import g54.si26.DTOs.CommunityDTO;
import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.DTOs.TeacherDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.SwingUtil;
import g54.si26.utils.Util;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;


public class ControllerPlanMultipleFees {

    private final ModelPlanMultipleFees model;
    private final ViewPlanMultipleFees  view;
    
    // From the SwingMain
    private String simulatedDateStr;

    public ControllerPlanMultipleFees(ModelPlanMultipleFees model, ViewPlanMultipleFees view){
        this.model = model;
        this.view  = view;
    }

    public void initController(){
        loadTeachers();
        loadCommunities();
        updateTeacherHintVisibility(); 
        setupValidationHints();
        view.getFrame().setVisible(true);

        // Bottom toolbar listeners
        view.getBtnBack().addActionListener(e -> view.getFrame().dispose());
        view.getBtnClear().addActionListener(e -> SwingUtil.exceptionWrapper(this::clearForm));
        view.getBtnSave().addActionListener(e -> SwingUtil.exceptionWrapper(this::saveFormativeAction));

        //--------
        // Teacher listeners section
        //--------------
        view.getBtnAddTeacher().addActionListener(e -> SwingUtil.exceptionWrapper(this::addTeacherToGrid));
        view.getBtnUpdateTeacher().addActionListener(e -> SwingUtil.exceptionWrapper(this::updateTeacherInGrid));
        view.getBtnRemoveTeacher().addActionListener(e -> SwingUtil.exceptionWrapper(this::removeTeacherFromGrid));

        // Detection of the Teacher in grid for UPDATE
        view.getTblTeachers().getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()){
                int row=view.getTblTeachers().getSelectedRow();
                if(row!=-1){
                    int tId=(int) view.getTblTeachers().getValueAt(row, 0);
                    double rem=(double) view.getTblTeachers().getValueAt(row, 2);
                    
                    view.getTxtRemunerationField().setText(String.valueOf(rem));
                    
                    // Block DropDownBox until update
                    for(int i=0; i<view.getCbTeacher().getItemCount(); i++){
                        TeacherDTO t=view.getCbTeacher().getItemAt(i);
                        if(t!=null && t.getTeacherId()==tId){
                            view.getCbTeacher().setSelectedIndex(i);
                            break;
                        }
                    }
                    view.getCbTeacher().setEnabled(false); 
                    view.getBtnAddTeacher().setVisible(false);
                    view.getBtnUpdateTeacher().setVisible(true);
                }
                else{
                    // return to normal conditions
                    view.getTxtRemunerationField().setText("");
                    view.getCbTeacher().setEnabled(true);
                    view.getCbTeacher().setSelectedIndex(0);
                    view.getBtnAddTeacher().setVisible(true);
                    view.getBtnUpdateTeacher().setVisible(false);
                }
            }
        });

        //-------------
        // Communities listener SECTION
        //-----------------
        view.getBtnAddCommunity().addActionListener(e -> SwingUtil.exceptionWrapper(this::addNewCommunity));
        view.getBtnUpdateComm().addActionListener(e -> SwingUtil.exceptionWrapper(this::updateCommunityData));
        view.getBtnDeleteCommunity().addActionListener(e -> SwingUtil.exceptionWrapper(this::deleteCommunity));

        // Communities listener INSIDE GRID
        view.getTblCommunities().getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting() && view.getTblCommunities().isEnabled())
            		populateEditFields();
        });

        // After exit we check if the name exists inside the grid
        view.getTxtCourseNameField().addFocusListener(new FocusAdapter(){
            @Override public void focusLost(FocusEvent e){
                String name = view.getTxtCourseName();
                if(!name.isEmpty() && model.nameExistsInPastOrPresent(name, getSimulatedDateStr()))
                    view.showError("The name '" + name + "' already exists and is blocked.");
            }
        });

        // Listener for online checkbox
        view.getChkOnline().addItemListener(e -> {
            boolean online = (e.getStateChange() == ItemEvent.SELECTED);
            view.getTxtLocationField().setEnabled(!online);
            if(online)
            		view.getTxtLocationField().setText("");
        });
        
        //Single Fee listener
        view.getChkSingleFee().addItemListener(e -> {
            boolean single = (e.getStateChange() == ItemEvent.SELECTED);
            view.getTblCommunities().setEnabled(!single);
            view.getTxtEditCommName().setEnabled(!single);
            view.getBtnAddCommunity().setEnabled(!single);
            
            if(single){
            	//Changes depending if its selected or not
                view.getBtnUpdateComm().setText("Update");
                view.getTblCommunities().clearSelection();
                view.getTxtEditCommName().setText("");
            }
            else
                view.getBtnUpdateComm().setText("Update Selected");
            
        });

        view.getBtnFillDebug().addActionListener(e -> SwingUtil.exceptionWrapper(this::fillDebugData));
    }

    

    //-----------------
    //  COMMUNITY CRUD & FEES SECTION
    //	----------
    //Loads all communities to the grid while saving all the input info from the user
    public void loadCommunities(){
        Map<Integer, Double> currentFees = collectCommunityFees();
        
        List<CommunityDTO> list = model.getAllCommunities();
        DefaultTableModel tm = (DefaultTableModel) view.getTblCommunities().getModel();
        tm.setRowCount(0);
        
        for(CommunityDTO c : list){
            String feeStr = "";
            if(currentFees.containsKey(c.getCommunityId()))
                feeStr=String.valueOf(currentFees.get(c.getCommunityId()));
            
            tm.addRow(new Object[]{ c.getCommunityId(), c.getName(), feeStr });
        }
    }

    
    //Fills up the data from the grid to the info section
    private void populateEditFields(){
        int row=view.getTblCommunities().getSelectedRow();
        if(row!=-1){
            String name = view.getTblCommunities().getValueAt(row, 1).toString();
            String fee = view.getTblCommunities().getValueAt(row, 2).toString();
            view.getTxtEditCommName().setText(name);
            view.getTxtEditCommFee().setText(fee);
        }
    }

    
    // Adds new community, default value is 0 for fee, updates grid directly without full DB reload
    private void addNewCommunity(){
        String name = view.getTxtEditCommName().getText();
        String feeText = view.getTxtEditCommFee().getText().trim();
        
        double newFee = 0.0;
        if(!feeText.isEmpty()){
            try{
                newFee = Double.parseDouble(feeText.replace(",", "."));
                if(newFee < 0)
                		throw new ApplicationException("Fee cannot be negative.");
            }
            catch (NumberFormatException ex){
                throw new ApplicationException("Invalid fee format.");
            }
        }
        
        
        	int newId = model.addCommunity(name);
        view.getTxtEditCommName().setText("");
        view.getTxtEditCommFee().setText("");
        
        DefaultTableModel tm = (DefaultTableModel) view.getTblCommunities().getModel();
        //	tm.addRow(new Object[]{newId, name, newFee});
        
        boolean alreadyInGrid = false;
        for(int i=0; i<tm.getRowCount(); i++) 
        		//Check if its already on the db
            if(Integer.parseInt(tm.getValueAt(i, 0).toString()) == newId){
                tm.setValueAt(newFee, i, 2);
                alreadyInGrid = true;
                break;
            }
        
        
        // If it was not in the table (Deleted previously)
        if(!alreadyInGrid) 
            tm.addRow(new Object[]{newId, name, newFee});

        
        // Si el Single Fee está marcado, aplicamos el precio a todas
        if(view.getChkSingleFee().isSelected())
            for(int j=0; j<tm.getRowCount(); j++) 
                tm.setValueAt(newFee, j, 2);
            
        
    }

    
    //Update the name of the community in the BD
    private void updateCommunityData(){
        boolean isSingle = view.getChkSingleFee().isSelected();
        int row = view.getTblCommunities().getSelectedRow();
        
        if(!isSingle && row==-1)
            throw new ApplicationException("Please select a community from the grid first.");
        
        if(!isSingle){
            int id = (int) view.getTblCommunities().getValueAt(row, 0);
            String newName = view.getTxtEditCommName().getText();
            model.editCommunity(id, newName);
            view.getTblCommunities().setValueAt(newName, row, 1);
        }
        
        try{
            String feeText = view.getTxtEditCommFee().getText().trim();
            if(!feeText.isEmpty()){
                double newFee = Double.parseDouble(feeText.replace(",", "."));
                if(newFee < 0)
                		throw new ApplicationException("Fee cannot be negative.");
                
                if(isSingle){
                    for(int i=0; i<view.getTblCommunities().getRowCount(); i++)
                        view.getTblCommunities().setValueAt(newFee, i, 2);
                    
                    view.getChkSingleFee().setSelected(false);
                    view.getTxtEditCommFee().setText("");
                }
                else 
                    view.getTblCommunities().setValueAt(newFee, row, 2);
                
            }
            else 
                if(!isSingle)
                		view.getTblCommunities().setValueAt("", row, 2);
            
        }
        catch (NumberFormatException ex){
            throw new ApplicationException("Invalid fee format.");
        }
    }

    
    // Removes the community from the visual grid so it won't be saved with the FA (Does not delete from DB)
    private void deleteCommunity(){
        int row = view.getTblCommunities().getSelectedRow();
        if(row == -1)
        		throw new ApplicationException("Select a community to remove from the list.");
        
        ((DefaultTableModel) view.getTblCommunities().getModel()).removeRow(row);
    }

    //Reads comminity grid and returns map with ID and fees
    private Map<Integer, Double> collectCommunityFees(){
        DefaultTableModel tm = (DefaultTableModel) view.getTblCommunities().getModel();
        Map<Integer, Double> fees = new LinkedHashMap<>();
        
        for(int i=0; i<tm.getRowCount(); i++){
            int id = (int) tm.getValueAt(i, 0);
            String feeStr = tm.getValueAt(i, 2).toString();
            // Solo recogemos aquellas filas que tienen una fee explícita para guardar
            if(feeStr!=null && !feeStr.isBlank())
                fees.put(id, Double.parseDouble(feeStr));
        }
        return fees;
    }

    // ---------------------
    //  TEACHERS SECTION
    // --------------

    //Hide or show the Update button under the teacher section
    private void updateTeacherHintVisibility(){
        int count = view.getTblTeachers().getRowCount();
        view.getLblTeacherEditHint().setVisible(count>0);
    }

    //Loads the list of teachers in the dropdownbox
    private void loadTeachers(){
        List<TeacherDTO> teachers = model.getAllTeachers();
        view.getCbTeacher().removeAllItems();
        view.getCbTeacher().addItem(null);
        for(TeacherDTO t : teachers)
        		view.getCbTeacher().addItem(t);
    }

    //Adds a new teacher and the remuneration to the grid
    private void addTeacherToGrid(){
        TeacherDTO selected = (TeacherDTO) view.getCbTeacher().getSelectedItem();
        if(selected == null)
        		throw new ApplicationException("Please select a teacher.");
        try{
            double rem = Double.parseDouble(view.getTxtRemunerationField().getText().replace(",", "."));
            if(rem<0)
            		throw new ApplicationException("Remuneration cannot be negative.");
            
            DefaultTableModel tm = (DefaultTableModel) view.getTblTeachers().getModel();
            for(int i=0; i<tm.getRowCount(); i++)
                if(Integer.parseInt(tm.getValueAt(i, 0).toString()) == selected.getTeacherId())
                    throw new ApplicationException("This teacher has already been added.");

            tm.addRow(new Object[]{selected.getTeacherId(), selected.getName(), rem});
            view.getTxtRemunerationField().setText("");
            updateTeacherHintVisibility();
        }
        catch (ApplicationException ae){
        		throw ae;
        }
        	catch (Exception ex){
        		throw new ApplicationException("Invalid remuneration amount.");
        	}
    }

    //Update remuneration for the teacher
    private void updateTeacherInGrid(){
        int row = view.getTblTeachers().getSelectedRow();
        if(row == -1)
        		return;
        try{
            double rem = Double.parseDouble(view.getTxtRemunerationField().getText().replace(",", "."));
            if(rem<0)
            		throw new ApplicationException("Remuneration cannot be negative.");
            
            view.getTblTeachers().setValueAt(rem, row, 2);
            view.getTblTeachers().clearSelection(); 
            
        }
        catch (NumberFormatException ex){
            throw new ApplicationException("Invalid remuneration amount.");
        }
    }

    //Remove the teacher from the temporal grid
    private void removeTeacherFromGrid(){
        int row = view.getTblTeachers().getSelectedRow();
        if(row==-1)
        		throw new ApplicationException("Select a teacher row to remove.");
        
        ((DefaultTableModel) view.getTblTeachers().getModel()).removeRow(row);
        updateTeacherHintVisibility();
    }

    //-------------------
    //SAVE AND CLEAN SECTION IMPORTANT
    //----------------------
    
    /*	
     * Saves the Formative Action with all the data from the View, makes a precheck validations 
     * of the form, this is found in the Model under the validate() method.
     * If everything is correct the Formative Action is sent to the Model and saved.
     */
    private void saveFormativeAction(){
        FormativeActionDTO dto = new FormativeActionDTO();
        //Uses auxiliar class from the Model
        ModelPlanMultipleFees.ValidationResult preCheck = new ModelPlanMultipleFees.ValidationResult();

    		//Gets data from View to DTO
        	dto.setName(view.getTxtCourseName());
        	dto.setObjectives(view.getTxtObjectives());
        	dto.setMainContents(view.getTxtMainContents());
        	dto.setStartDate(view.getTxtStartDate());
        	dto.setEndDate(view.getTxtEndDate());
        	dto.setInscriptionPeriodStart(view.getTxtEnrolStart());
        	dto.setInscriptionPeriodEnd(view.getTxtEnrolEnd());

        	if(view.getChkOnline().isSelected())
            	dto.setLocation("Online");
        	else 
        		dto.setLocation(view.getTxtLocation());
        
        try{
        		dto.setSpots(view.getTxtSpots().isEmpty() ? 0 : Integer.parseInt(view.getTxtSpots()));
        	} 
        catch (NumberFormatException e){
        		preCheck.errors.add("Places must be a valid number.");
        	}

        try{
        		dto.setNumberOfHours(Integer.parseInt(view.getSpnDuration().getValue().toString()));
        	} 
        catch (Exception e){
        		preCheck.errors.add("Duration must be a valid integer number."); dto.setNumberOfHours(0);
        	}

        DefaultTableModel tmodel=(DefaultTableModel) view.getTblTeachers().getModel();
        if(tmodel.getRowCount()==0)
            preCheck.errors.add("At least one teacher must be assigned.");
        
        
        /*
         * OBSOLETE
         * Saving a single TeacherID or one initialPayment in the DTO is of no use
         * The model reads the obs tmodel directly.
         */
        /* else {
            dto.setTeacherId(Integer.parseInt(tmodel.getValueAt(0, 0).toString()));
            dto.setInitialPayment(Double.parseDouble(tmodel.getValueAt(0, 2).toString()));
        }
        */

        Map<Integer, Double> fees=collectCommunityFees();

        // Adds all the errors
        ModelPlanMultipleFees.ValidationResult finalResult = model.validate(dto, getSimulatedDateStr(), fees);
        finalResult.errors.addAll(preCheck.errors);

        //Errors block the user to continue
        if(finalResult.hasErrors()){
            view.showValidationSummary(finalResult.errors, finalResult.warnings);
            return;
        }

        // Warning section
        if(finalResult.hasWarnings()){
            StringBuilder sb = new StringBuilder("The following warnings were detected:\n\n");
            finalResult.warnings.forEach(w -> sb.append(" - ").append(w).append("\n"));
            sb.append("\nDo you want to proceed and save anyway?");
            
            int choice = JOptionPane.showConfirmDialog(view.getFrame(), sb.toString(), "Validation Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if(choice==JOptionPane.NO_OPTION)
            		return;
        }

        // Saves data
        model.addFormativeAction(dto, getSimulatedDateStr(), fees, tmodel);
        view.showSuccess(dto.getName());
        clearForm();
    }

    
    //Clear the form
    private void clearForm(){
        view.getTxtCourseNameField().setText(""); view.getTxtObjectivesField().setText(""); view.getTxtMainContentsField().setText("");
        view.getTxtSpotsField().setText(""); view.getTxtStartDateField().setText(""); view.getTxtEndDateField().setText("");
        view.getTxtEnrolStartField().setText(""); view.getTxtEnrolEndField().setText("");
        view.getTxtLocationField().setText(""); view.getChkOnline().setSelected(false);
        view.getTxtRemunerationField().setText(""); view.getTxtEditCommName().setText(""); view.getTxtEditCommFee().setText("");
        view.getChkSingleFee().setSelected(false);
        ((DefaultTableModel) view.getTblTeachers().getModel()).setRowCount(0);
        updateTeacherHintVisibility();
        
        List<CommunityDTO> list = model.getAllCommunities();
        DefaultTableModel tm = (DefaultTableModel) view.getTblCommunities().getModel();
        tm.setRowCount(0);
        for(CommunityDTO c : list)
        		tm.addRow(new Object[]{ c.getCommunityId(), c.getName(), "" });
    }

    //Get and set date from SwingMain
    private String getSimulatedDateStr(){
        if(simulatedDateStr==null || simulatedDateStr.trim().isEmpty())
        		return Util.dateToIsoString(new Date());
        try{
        		return Util.dateToIsoString(Util.isoStringToDate(simulatedDateStr));
        	}
        catch(Exception e){
        		return Util.dateToIsoString(new Date()); 
        	}
    }
    
    public void setSimulatedDate(String dateIso){
		this.simulatedDateStr = dateIso; 
	}
    
    private void setupValidationHints() {
        javax.swing.event.DocumentListener docListener = new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){
            		updateHints();
            	}
            public void removeUpdate(javax.swing.event.DocumentEvent e){
            		updateHints();
            	}
            public void changedUpdate(javax.swing.event.DocumentEvent e){
            		updateHints();
            	}
        };

        view.getTxtCourseNameField().getDocument().addDocumentListener(docListener);
        view.getTxtSpotsField().getDocument().addDocumentListener(docListener);
        view.getTxtStartDateField().getDocument().addDocumentListener(docListener);
        view.getTxtEndDateField().getDocument().addDocumentListener(docListener);
        view.getTxtEnrolStartField().getDocument().addDocumentListener(docListener);
        view.getTxtEnrolEndField().getDocument().addDocumentListener(docListener);

        view.getTblTeachers().getModel().addTableModelListener(e -> updateHints());
        view.getTblCommunities().getModel().addTableModelListener(e -> updateHints());
        
        updateHints();
    }

    private void updateHints(){
        view.getLblHintCourseName().setVisible(view.getTxtCourseName().isEmpty());
        view.getLblHintSpots().setVisible(view.getTxtSpots().isEmpty());
        view.getLblHintStartDate().setVisible(view.getTxtStartDate().isEmpty());
        view.getLblHintEndDate().setVisible(view.getTxtEndDate().isEmpty());
        
        boolean enrolEmpty=view.getTxtEnrolStart().isEmpty() || view.getTxtEnrolEnd().isEmpty();
        view.getLblHintEnrolment().setVisible(enrolEmpty);
        
        boolean noTeachers=view.getTblTeachers().getRowCount() == 0;
        view.getLblHintTeacher().setVisible(noTeachers);
        
        boolean noFees=collectCommunityFees().isEmpty();
        view.getLblHintCommunity().setVisible(noFees);
    }
    

    /** Rellena el formulario con datos d prueba pa agilizar la entrada manual. */
    private void fillDebugData(){
        String base = getSimulatedDateStr().substring(0, 10);
        java.time.LocalDate today = java.time.LocalDate.parse(base);
        
        view.getTxtCourseNameField().setText("Lorem Ipsum Advanced Training");
        view.getTxtObjectivesField().setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Participants will gain proficiency in lorem ipsum techniques.");
        view.getTxtMainContentsField().setText("Unit 1: Lorem Ipsum Basics\nUnit 2: Dolor Sit Amet Patterns\nUnit 3: Consectetur Advanced Topics");
        view.getTxtSpotsField().setText("20");
        view.getTxtStartDateField().setText(today.plusMonths(2).toString());
        view.getTxtEndDateField().setText(today.plusMonths(2).plusDays(1).toString());
        view.getTxtLocationField().setText("Lorem Ipsum Classroom 1");
        view.getTxtEnrolStartField().setText(today.plusDays(1).toString());
        view.getTxtEnrolEndField().setText(today.plusMonths(1).toString());
    }
}