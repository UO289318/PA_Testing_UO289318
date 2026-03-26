package g54.si26.planFormativeAction;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.DTOs.TeacherDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ModelPlanFormativeAction {

    private final Database db = new Database();
    
    // Constante con el nombre de la comunidad por defecto
    private static final String GENERAL_PUBLIC_NAME = "General Public";

    public List<TeacherDTO> getAllTeachers(){
        String sql = "SELECT teacher_id AS teacherId, name, fiscal_id AS fiscalId, email, phone FROM Teacher ORDER BY name";
        return db.executeQueryPojo(TeacherDTO.class, sql);
    }

    public boolean enrolmentMeetsLeadTimeRule(String inscriptionPeriodStart, String sessionStartDate){
        try {
            LocalDate enrol = LocalDate.parse(inscriptionPeriodStart.substring(0, 10));
            LocalDate session = LocalDate.parse(sessionStartDate.substring(0, 10));
            return !enrol.isAfter(session.minusWeeks(3));
        } catch (Exception e){
            return true;
        }
    }

    public boolean nameExistsInPastOrPresent(String name, String simulatedToday){
        if (name == null || name.isBlank())
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

    // Validación alineada al 100% con la versión de MultipleFees
    public ValidationResult validate(FormativeActionDTO dto, String simulatedToday){
        ValidationResult result = new ValidationResult();

        if (dto.getName() == null || dto.getName().isBlank())
            result.errors.add("Course name is required.");
        else 
        		if (nameExistsInPastOrPresent(dto.getName(), simulatedToday))
        			result.errors.add("A formative action named \"" + dto.getName() + "\" already exists created today or in the past.");

        if (dto.getObjectives() == null || dto.getObjectives().isBlank()) 
        		result.errors.add("Objectives are required.");
        if (dto.getMainContents() == null || dto.getMainContents().isBlank())
        		result.errors.add("Main contents are required.");
        
        if (dto.getLocation() == null || dto.getLocation().isBlank()) 
            result.errors.add("Location is required (or must be marked as Online).");
        
        if (dto.getNumberOfHours() < 1) 
            result.errors.add("Duration must be at least 1 hour.");

        if (dto.getSpots() <= 0)
        		result.errors.add("Number of spots must be greater than zero.");
        if (dto.getFee() < 0)
        		result.errors.add("Fee must be zero or a positive value.");

        if (dto.getStartDate() == null || dto.getStartDate().isBlank())
        		result.errors.add("Formative Action start date is required.");
        if (dto.getEndDate() == null || dto.getEndDate().isBlank())
        		result.errors.add("Formative Action end date is required.");
        if (dto.getInscriptionPeriodStart() == null || dto.getInscriptionPeriodStart().isBlank()) 
        		result.errors.add("Enrolment start date is required.");
        if (dto.getInscriptionPeriodEnd() == null || dto.getInscriptionPeriodEnd().isBlank()) 
        		result.errors.add("Enrolment end date is required.");

        if (dto.getStartDate() != null && !dto.getStartDate().isBlank() && dto.getInscriptionPeriodStart()!=null && !dto.getInscriptionPeriodStart().isBlank() &&simulatedToday!=null){
            try {
                LocalDate today = LocalDate.parse(simulatedToday.substring(0, 10));
                LocalDate start = LocalDate.parse(dto.getStartDate().substring(0, 10));
                LocalDate end = LocalDate.parse(dto.getEndDate().substring(0, 10));
                LocalDate enStart = LocalDate.parse(dto.getInscriptionPeriodStart().substring(0, 10));
                LocalDate enEnd = LocalDate.parse(dto.getInscriptionPeriodEnd().substring(0, 10));

                if (!start.isAfter(today))
                		result.errors.add("Formative Action start date cannot be set in the past");
                if (end.isBefore(start)) 
                		result.errors.add("The end date cannot be before the start date.");
                if (enStart.isBefore(today)) 
                		result.errors.add("Enrolment cannot start in the past.");
                if (enEnd.isBefore(enStart)) 
                		result.errors.add("Enrolment end date cannot be before its start date.");
                if (enEnd.isAfter(start)) 
                		result.warnings.add("Enrolment does not end before the Formative Action starts.");

                if (!enrolmentMeetsLeadTimeRule(dto.getInscriptionPeriodStart(), dto.getStartDate())) 
                    result.warnings.add("Enrolment starts less than 3 weeks before the Formative Action.");
                
            } catch (Exception e){
                result.errors.add("Invalid date format detected. Please use YYYY-MM-DD.");
            }
        }
        return result;
    }

    //Searches for the General Public and creates it if it does not exist
    private int getOrCreateGeneralPublicCommunity(){
        String sqlSearch="SELECT community_id FROM Community WHERE LOWER(communityName) = LOWER(?)";
        List<Object[]> res=db.executeQueryArray(sqlSearch, GENERAL_PUBLIC_NAME);
        
        if (!res.isEmpty()) 
            return Integer.parseInt(res.get(0)[0].toString());
        else{
            // Si no existe o fue borrada, la volvemos a crear
            Object newId = db.executeInsert("INSERT INTO Community (communityName) VALUES (?)", GENERAL_PUBLIC_NAME);
            return Integer.parseInt(newId.toString());
        }
    }

    public void addFormativeAction(FormativeActionDTO dto, String simulatedToday, javax.swing.table.DefaultTableModel teacherModel){
        deleteFutureDuplicates(dto.getName(), simulatedToday);

        String sqlAction="INSERT INTO FormativeAction (name, objectives, mainContents, spots, startDate, endDate, numberOfHours, inscriptionPeriodStart, inscriptionPeriodEnd, location, status, creationDate) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?)";

        Object generatedId=db.executeInsert(sqlAction, 
                dto.getName(), dto.getObjectives(), dto.getMainContents(), 
                dto.getSpots(), dto.getStartDate(), dto.getEndDate(), 
                dto.getNumberOfHours(), dto.getInscriptionPeriodStart(), 
                dto.getInscriptionPeriodEnd(), dto.getLocation(), simulatedToday); 

        int actionId = Integer.parseInt(generatedId.toString());
        
        // Obtenemos dinámicamente el ID asegurándonos de que la comunidad exista
        int communityId = getOrCreateGeneralPublicCommunity();
        
        // Asociamos la tasa a esta comunidad
        db.executeUpdate("INSERT INTO Fee (amount, community_id, action_id) VALUES (?, ?, ?)", dto.getFee(), communityId, actionId);
        
        for (int i=0; i<teacherModel.getRowCount(); i++){
            int tId=Integer.parseInt(teacherModel.getValueAt(i, 0).toString());
            double rem=Double.parseDouble(teacherModel.getValueAt(i, 2).toString());
            db.executeUpdate("INSERT INTO Teacher_FormativeAction (remuneration, status, action_id, teacher_id) VALUES (?, 'PENDING', ?, ?)", rem, actionId, tId);
            double netAmount = rem / 1.21;
            double vat = rem - netAmount;
            
            // Usamos la fecha de fin de curso como la fecha en la que se emite la factura
            String invoiceDate = dto.getEndDate();
            if (invoiceDate != null && invoiceDate.length() > 10) 
                invoiceDate = invoiceDate.substring(0, 10);
        
            db.executeUpdate("INSERT INTO Invoice (invoice_date, netAmount, vat, totalAmount, status, teacher_id, action_id) " +
                    "VALUES (?, ?, ?, ?, 'PENDING', ?, ?)", 
                    invoiceDate, netAmount, vat, rem, tId, actionId);
        }
    }

    private void deleteFutureDuplicates(String name, String simulatedToday){
        String dateLimit = (simulatedToday != null && simulatedToday.length() >= 10) 
                           ? simulatedToday.substring(0, 10) : "9999-12-31";
        String sql = "DELETE FROM FormativeAction WHERE LOWER(name) = LOWER(?) AND SUBSTR(creationDate, 1, 10) > ?";
        db.executeUpdate(sql, name, dateLimit);
    }
}