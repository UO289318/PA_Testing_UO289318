package g54.si26.planFormativeAction;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.DTOs.TeacherDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ModelPlanFormativeAction {

    private Database db = new Database();

    public List<TeacherDTO> getAllTeachers(){
        	String sql = "SELECT teacher_id AS teacherId, name, fiscal_id AS fiscalId, email, phone FROM Teacher ORDER BY name";
        	return db.executeQueryPojo(TeacherDTO.class, sql);
    	}

    	public boolean enrolmentMeetsLeadTimeRule(String inscriptionPeriodStart, String sessionStartDate){
    		try {
    			LocalDate enrol = LocalDate.parse(inscriptionPeriodStart.substring(0, 10));
    			LocalDate session = LocalDate.parse(sessionStartDate.substring(0, 10));
    			return !enrol.isAfter(session.minusWeeks(3));
        }
    		catch (Exception e){
    			return true;
    		}
    }

    	public static class ValidationResult {
    		public final List<String> errors = new ArrayList<>();
    		public final List<String> warnings = new ArrayList<>();
    		public boolean hasErrors(){
    			return !errors.isEmpty();
    		}
    		public boolean hasWarnings(){
    			return !warnings.isEmpty();
    		}
    	}

    	public boolean nameExistsInPastOrPresent(String name, String simulatedToday) {
            if (name == null || name.isBlank()) return false;
            
            String dateLimit;
            try {
                // Intentamos normalizar la fecha recibida
                if (simulatedToday != null && simulatedToday.length() >= 10) {
                    dateLimit = simulatedToday.substring(0, 10);
                } else {
                    // Fallback a la fecha real del sistema si la simulada no es válida
                    dateLimit = LocalDate.now().toString();
                }
            } catch (Exception e) {
                dateLimit = LocalDate.now().toString();
            }
            
            String sql = "SELECT COUNT(*) FROM FormativeAction " +
                         "WHERE LOWER(name) = LOWER(?) " +
                         "AND SUBSTR(startDate, 1, 10) <= ?";
            
            List<Object[]> res = db.executeQueryArray(sql, name, dateLimit);
            return !res.isEmpty() && Integer.parseInt(res.get(0)[0].toString()) > 0;
        }

    	public void addFormativeAction(FormativeActionDTO dto, String simulatedToday, javax.swing.table.DefaultTableModel teacherModel) {
    	    if (nameExistsInPastOrPresent(dto.getName(), simulatedToday))
    	        throw new ApplicationException("Cannot create: A course named '" + dto.getName() + "' already exists created today or in the past.");

    	    deleteFutureDuplicates(dto.getName(), simulatedToday);

    	    String sqlAction = "INSERT INTO FormativeAction (name, objectives, mainContents, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, status, creationDate) "
    	            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?)";

    	    Object generatedId = db.executeInsert(sqlAction, 
    	            dto.getName(), dto.getObjectives(), dto.getMainContents(), 
    	            dto.getSpots(), dto.getStartDate(), dto.getEndDate(), 
    	            dto.getNumberOfHours(), dto.getInscriptionPeriodStart(), 
    	            dto.getInscriptionPeriodEnd(), dto.getLocation(), simulatedToday); 

    	    int actionId = Integer.parseInt(generatedId.toString());
    	    db.executeUpdate("INSERT INTO Fee (amount, action_id) VALUES (?, ?)", dto.getFee(), actionId);
    	    
    	    // LOGIC SYNC: Loop to insert ALL teachers from the table
    	    for (int i = 0; i < teacherModel.getRowCount(); i++) {
    	        int tId = Integer.parseInt(teacherModel.getValueAt(i, 0).toString());
    	        double rem = Double.parseDouble(teacherModel.getValueAt(i, 2).toString());
    	        db.executeUpdate("INSERT INTO Teacher_FormativeAction (remuneration, status, action_id, teacher_id) VALUES (?, 'PENDING', ?, ?)", 
    	                rem, actionId, tId);
    	    }
    	}

    	public ValidationResult validate(FormativeActionDTO dto, String simulatedToday) {
    	    ValidationResult result = new ValidationResult();

    	    if (dto.getName() == null || dto.getName().isBlank())
    	        result.errors.add("Course name is required.");
    	    else if (nameExistsInPastOrPresent(dto.getName(), simulatedToday))
    	        result.errors.add("A formative action named \"" + dto.getName() + "\" already exists created today or in the past.");

    	    if (dto.getObjectives() == null || dto.getObjectives().isBlank()) result.errors.add("Objectives are required.");
    	    if (dto.getMainContents() == null || dto.getMainContents().isBlank()) result.errors.add("Main contents are required.");
    	    
    	    // LOGIC SYNC: Mandatory location or Online
    	    if (dto.getLocation() == null || dto.getLocation().isBlank()) 
    	        result.errors.add("Location is required (or must be marked as Online).");
    	    
    	    // LOGIC SYNC: Duration check
    	    if (dto.getNumberOfHours() < 1) 
    	        result.errors.add("Duration must be at least 1 hour.");

    	    if (dto.getSpots() <= 0) result.errors.add("Number of spots must be greater than zero.");
    	    if (dto.getFee() < 0) result.errors.add("Fee must be a positive value.");

    	    if (dto.getStartDate() != null && !dto.getStartDate().isBlank() && 
    	        dto.getInscriptionPeriodStart() != null && !dto.getInscriptionPeriodStart().isBlank() &&
    	        simulatedToday != null) {
    	        try {
    	            LocalDate today = LocalDate.parse(simulatedToday.substring(0, 10));
    	            LocalDate start = LocalDate.parse(dto.getStartDate().substring(0, 10));
    	            LocalDate enStart = LocalDate.parse(dto.getInscriptionPeriodStart().substring(0, 10));

    	            if (!start.isAfter(today)) result.errors.add("Session start date must be after today.");
    	            if (enStart.isAfter(start)) result.errors.add("Enrolment must end before the session starts.");

    	            // LOGIC SYNC: Warning for lead time (No UI label, just logic for popup)
    	            if (!enrolmentMeetsLeadTimeRule(dto.getInscriptionPeriodStart(), dto.getStartDate())) {
    	                result.warnings.add("Enrolment starts less than 3 weeks before the session date.");
    	            }
    	        } catch (Exception e) {
    	            result.errors.add("Invalid date format detected.");
    	        }
    	    }
    	    return result;
    	}

    private void deleteFutureDuplicates(String name, String simulatedToday){
        String dateLimit = (simulatedToday != null && simulatedToday.length() >= 10) 
                           ? simulatedToday.substring(0, 10) : "9999-12-31";
        
        String sql = "DELETE FROM FormativeAction WHERE LOWER(name) = LOWER(?) AND SUBSTR(creationDate, 1, 10) > ?";
        db.executeUpdate(sql, name, dateLimit);
    }
}