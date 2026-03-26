package g54.si26.planMultipleFees;

import g54.si26.DTOs.CommunityDTO;
import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.DTOs.TeacherDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ModelPlanMultipleFees {

    private final Database db = new Database();

    // [OBSOLETO] Esta constante ya no se usa.
    // public static final int COMMUNITY_ID_GENERAL = 3;

    //CLASE AUXILIAR
    public static class ValidationResult {
        public final List<String> errors   = new ArrayList<>();
        public final List<String> warnings = new ArrayList<>();
        public boolean hasErrors()   { return !errors.isEmpty(); }
        public boolean hasWarnings(){ return !warnings.isEmpty(); }
    }
    
    //Gets all the communities from the DB
    public List<CommunityDTO> getAllCommunities(){
        String sql = "SELECT community_id AS communityId, communityName AS name "
                   + "FROM Community ORDER BY communityName";
        return db.executeQueryPojo(CommunityDTO.class, sql);
    }
    
    //Gets all the theachers
    public List<TeacherDTO> getAllTeachers(){
        String sql = "SELECT teacher_id AS teacherId, name, fiscal_id AS fiscalId, email, phone FROM Teacher ORDER BY name";
        return db.executeQueryPojo(TeacherDTO.class, sql);
    }

    //-----------------------
    //COMMUNITY SECTION
    //----------------------
    
    	//Useful for the UPDATE nad INSERT, chcecks if a name already exists
    	public boolean communityNameExists(String name, int excludeId){
        	if(name == null || name.isBlank()) 
        		return false;
        	String sql = excludeId > 0
            ? "SELECT COUNT(*) FROM Community WHERE LOWER(communityName) = LOWER(?) AND community_id <> ?"
            : "SELECT COUNT(*) FROM Community WHERE LOWER(communityName) = LOWER(?)";
        	List<Object[]> res = excludeId > 0
            ? db.executeQueryArray(sql, name, excludeId)
            : db.executeQueryArray(sql, name);
        	return !res.isEmpty() && Integer.parseInt(res.get(0)[0].toString()) > 0;
    	}

    	//Safety check for the Addition of Community
    	public int addCommunity(String name){
    		name = (name==null) ? "" : name.trim();
    		if(name.isBlank())
    			throw new ApplicationException("Community name cannot be empty.");
    		if(communityNameExists(name, -1))
    			throw new ApplicationException("A community named \"" + name + "\" already exists.");

    		Object id = db.executeInsert("INSERT INTO Community (communityName) VALUES (?)", name);
    		return Integer.parseInt(id.toString());
    }
    	
    	//UPDATE Logic in communities
    	public void editCommunity(int communityId, String newName){
    		newName = (newName == null) ? "" : newName.trim();
    		if(newName.isBlank())
    			throw new ApplicationException("Community name cannot be empty.");
    		if(communityNameExists(newName, communityId))
    			throw new ApplicationException("A community named \"" + newName + "\" already exists.");

    		db.executeUpdate("UPDATE Community SET communityName = ? WHERE community_id = ?", newName, communityId);
    	}
   
    	//DROP CASCADE, first from fees, then from Community 
    public void deleteCommunity(int communityId){
        db.executeUpdate("DELETE FROM Fee WHERE community_id = ?", communityId);
        db.executeUpdate("DELETE FROM Community WHERE community_id = ?", communityId);
    }

    
    // -------------------------
    //  VALIDATION SECTION
    //------------------------

    //Enrolment must end at least 3 weeks before start date, otherwise a warning will pop up
    public boolean enrolmentMeetsLeadTimeRule(String inscriptionPeriodStart, String sessionStartDate){
        try {
            LocalDate enrol   = LocalDate.parse(inscriptionPeriodStart.substring(0, 10));
            LocalDate session = LocalDate.parse(sessionStartDate.substring(0, 10));
            return !enrol.isAfter(session.minusWeeks(3));
        } catch (Exception e){
        	// En caso de parseo fallido, la validación general de fechas lo cazará después
            return true; 
        }
    }

    //Blocks creation of Formative Actions with the same name (no hay repetidos) 
    public boolean nameExistsInPastOrPresent(String name, String simulatedToday){
        if(name==null || name.isBlank())
        		return false;
        	String dateLimit;
        	try {
        		dateLimit = (simulatedToday != null && simulatedToday.length() >= 10)
                ? simulatedToday.substring(0, 10) : LocalDate.now().toString();
        	} catch (Exception e){
        		dateLimit = LocalDate.now().toString();
        	}
        	String sql = "SELECT COUNT(*) FROM FormativeAction WHERE LOWER(name) = LOWER(?) AND SUBSTR(creationDate, 1, 10) <= ?";
        	List<Object[]> res = db.executeQueryArray(sql, name, dateLimit);
        	return !res.isEmpty() && Integer.parseInt(res.get(0)[0].toString()) > 0;
    	}


    /**
     * Valida toda la estructura del DTO, incluyendo restricciones lógicas, cronológicas y de obligatoriedad.
     */
    //VALIDATE METHOD, EVERY ERROR IS VALIDATED HERE
    public ValidationResult validate(FormativeActionDTO dto, String simulatedToday, Map<Integer, Double> communityFees){
        ValidationResult result = new ValidationResult();

        // Mandatory data
        	if(dto.getName() == null || dto.getName().isBlank())
        		result.errors.add("Course name is required.");
        	else if(nameExistsInPastOrPresent(dto.getName(), simulatedToday))
        		result.errors.add("A formative action named \"" + dto.getName() + "\" already exists created today or in the past.");

        	if(dto.getObjectives() == null || dto.getObjectives().isBlank())
        			result.errors.add("Objectives are required.");
        if(dto.getMainContents() == null || dto.getMainContents().isBlank())
        		result.errors.add("Main contents are required.");
        if(dto.getLocation() == null || dto.getLocation().isBlank())
        		result.errors.add("Location is required (or must be marked as Online).");
            
        // Hrs and spots must be a possitive nmbr
        if(dto.getNumberOfHours() < 1)
        		result.errors.add("Duration must be at least 1 hour.");
        if(dto.getSpots()<=0)
        		result.errors.add("Number of spots must be greater than zero.");

        // Fees logic
        if(communityFees == null || communityFees.isEmpty()) 
            result.errors.add("At least one community must have an assigned fee.");
        else 
            for (Map.Entry<Integer, Double> entry : communityFees.entrySet())
                if(entry.getValue() < 0)
                    result.errors.add("Fee for community id " + entry.getKey() + " must be zero or positive.");
        

        // DATE VALIDATION SECTION
        if(dto.getStartDate() == null || dto.getStartDate().isBlank())
        		result.errors.add("Formative Action start date is required.");
        if(dto.getEndDate() == null || dto.getEndDate().isBlank())
        		result.errors.add("Formative Action end date is required.");
        if(dto.getInscriptionPeriodStart() == null || dto.getInscriptionPeriodStart().isBlank())
        		result.errors.add("Enrolment start date is required.");
        if(dto.getInscriptionPeriodEnd() == null || dto.getInscriptionPeriodEnd().isBlank())
        		result.errors.add("Enrolment end date is required.");

        if(dto.getStartDate() != null && !dto.getStartDate().isBlank() && dto.getInscriptionPeriodStart() != null && !dto.getInscriptionPeriodStart().isBlank() && simulatedToday != null){
            try {
                LocalDate today=LocalDate.parse(simulatedToday.substring(0, 10));
                LocalDate start = LocalDate.parse(dto.getStartDate().substring(0, 10));
                LocalDate end = LocalDate.parse(dto.getEndDate().substring(0, 10));
                LocalDate enStart= LocalDate.parse(dto.getInscriptionPeriodStart().substring(0, 10));
                LocalDate enEnd = LocalDate.parse(dto.getInscriptionPeriodEnd().substring(0, 10));

                if(!start.isAfter(today))
                		result.errors.add("The Formative Action cannot be planned in the past.");
                if(end.isBefore(start))
                		result.errors.add("The end date cannot be before the start date.");
                if(enStart.isBefore(today))
                		result.errors.add("Enrolment cannot start in the past.");
                if(enEnd.isBefore(enStart))
                		result.errors.add("Enrolment end date cannot be before its start date.");
                if(enEnd.isAfter(start))
                		result.warnings.add("Enrolment does not end before the Formative Action starts.");

                // Aviso d 3 semanas (Warning)
                if(!enrolmentMeetsLeadTimeRule(dto.getInscriptionPeriodStart(), dto.getStartDate()))
                    result.warnings.add("Enrolment starts less than 3 weeks before the Formative Action date.");

            }
            catch (Exception e){
                result.errors.add("Invalid date format detected. Please use YYYY-MM-DD.");
            }
        }
        return result;
    }

    //-----------------
    //Save in DB SECTION
    //-----------------

    //Creates the Formative Action and then links the Teachers and Communities
    public void addFormativeAction(FormativeActionDTO dto, String simulatedToday, Map<Integer, Double> communityFees, javax.swing.table.DefaultTableModel teacherModel){
        	deleteFutureDuplicates(dto.getName(), simulatedToday);	

        	//Insert in Formative Action Table
        	String sqlAction = "INSERT INTO FormativeAction (name, objectives, mainContents, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, status, creationDate) "
                         + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?)";

        	Object generatedId = db.executeInsert(sqlAction, dto.getName(), dto.getObjectives(), dto.getMainContents(), dto.getSpots(), dto.getStartDate(), dto.getEndDate(), dto.getNumberOfHours(), dto.getInscriptionPeriodStart(), dto.getInscriptionPeriodEnd(), dto.getLocation(), simulatedToday);
        	int actionId = Integer.parseInt(generatedId.toString());

        	// Multiple Fees
        	for (Map.Entry<Integer, Double> entry : communityFees.entrySet())
        		db.executeUpdate("INSERT INTO Fee (amount, community_id, action_id) VALUES (?, ?, ?)", entry.getValue(), entry.getKey(), actionId);

        	//Multiple Teachers
        	for (int i = 0; i < teacherModel.getRowCount(); i++){
        		int tId = Integer.parseInt(teacherModel.getValueAt(i, 0).toString());
        		double rem = Double.parseDouble(teacherModel.getValueAt(i, 2).toString());
        		db.executeUpdate("INSERT INTO Teacher_FormativeAction (remuneration, status, action_id, teacher_id) VALUES (?, 'PENDING', ?, ?)", rem, actionId, tId);
        		double netAmount = rem / 1.21;
        		double vat = rem - netAmount;
            
        		String invoiceDate = dto.getEndDate();
        		if (invoiceDate != null && invoiceDate.length() > 10) 
        		    invoiceDate = invoiceDate.substring(0, 10);
        		
        		db.executeUpdate("INSERT INTO Invoice (invoice_date, netAmount, vat, totalAmount, status, teacher_id, action_id) " +
        		                 "VALUES (?, ?, ?, ?, 'PENDING', ?, ?)", 
        		                 invoiceDate, netAmount, vat, rem, tId, actionId);
        	}
    	}

    
    //Erase duplicates that are in the "future"
    private void deleteFutureDuplicates(String name, String simulatedToday){
        String dateLimit = (simulatedToday != null && simulatedToday.length() >= 10) ? simulatedToday.substring(0, 10) : "9999-12-31";
        db.executeUpdate("DELETE FROM FormativeAction WHERE LOWER(name) = LOWER(?) AND SUBSTR(creationDate, 1, 10) > ?", name, dateLimit);
    }
}