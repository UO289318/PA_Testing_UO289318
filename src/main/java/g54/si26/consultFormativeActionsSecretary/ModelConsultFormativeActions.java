package g54.si26.consultFormativeActionsSecretary;

import g54.si26.DTOs.FormativeActionManagementDTO;
import g54.si26.DTOs.FormativeActionDetailsDTO;
import g54.si26.utils.Database;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ModelConsultFormativeActions {

    private final Database db = new Database();

    //Obtains a list with the Formative Actions an the financial data
    public List<FormativeActionManagementDTO> getFormativeActions(String statusFilter, String dateFilter){
        //Safe check of the date and cuttofDate
        String safeDate = (dateFilter != null && !dateFilter.isBlank()) ? dateFilter : "9999-12-31";
        String cutoffDate = calculateCutoffDate(safeDate);
        String maxDateLimit = safeDate.length() == 10 ? safeDate + " 23:59:59" : safeDate;
        
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM ( " +
            "  SELECT " +
            "    fa.action_id AS actionId, " +
            "    fa.name AS name, " +
            "    CASE " +
            "      WHEN fa.closureDate IS NOT NULL AND date(?) >= date(fa.closureDate) " +
            "           AND (fa.reopenDate IS NULL OR date(?) < date(fa.reopenDate)) THEN 'CLOSED' " +
            "      WHEN (fa.cancelDate IS NOT NULL AND date(?) >= date(fa.cancelDate) AND (fa.reopenDate IS NULL OR date(?) < date(fa.reopenDate))) " +
            "           OR (fa.status = 'CANCELLED' AND fa.cancelDate IS NULL) THEN 'Cancelled' " + "      WHEN date(?) > date(fa.endDate) THEN 'Finished' " +
            "      WHEN date(?) >= date(fa.startDate) AND date(?) <= date(fa.endDate) THEN 'In progress' " +
            "      WHEN date(?) >= date(fa.inscriptionPeriodStart) AND date(?) <= date(fa.inscriptionPeriodEnd) THEN 'Enrolment open' " +
            "      WHEN date(?) < date(fa.startDate) THEN 'Upcoming' " +
            "      ELSE fa.status " +
            "    END AS status, " +
            "    fa.inscriptionPeriodStart || ' to ' || fa.inscriptionPeriodEnd AS enrolmentPeriod, " +
            "    fa.spots AS totalPlaces, " +
            "    (SELECT COUNT(*) FROM Inscription i WHERE i.action_id = fa.action_id AND i.state = 'CONFIRMED' AND date(i.inscription_date) <= date(?)) AS confirmedPlaces, " +


			"    (SELECT COUNT(*) FROM Inscription i WHERE i.action_id = fa.action_id AND i.state = 'RECEIVED' AND i.inscription_date > ? AND i.inscription_date <= ?) AS reservedPlaces, " +


			"    (fa.spots - " +
			"      (SELECT COUNT(*) FROM Inscription i WHERE i.action_id = fa.action_id AND i.state = 'CONFIRMED' AND date(i.inscription_date) <= date(?)) - " +
			"      (SELECT COUNT(*) FROM Inscription i WHERE i.action_id = fa.action_id AND i.state = 'RECEIVED' AND i.inscription_date > ? AND i.inscription_date <= ?)" +
			"    ) AS placesLeft, " +
			"    fa.startDate || ' to ' || fa.endDate AS actionDate, " +
            "    COALESCE((SELECT SUM(mm.amount) FROM MoneyMovement mm JOIN Inscription i ON mm.inscription_id = i.inscription_id WHERE i.action_id = fa.action_id AND mm.status = 'EXECUTED' AND date(mm.movement_date) <= date(?)), 0.0) AS income, " +
            "    COALESCE(ABS((SELECT SUM(mm.amount) FROM MoneyMovement mm JOIN Invoice inv ON mm.invoice_id = inv.invoice_id WHERE inv.action_id = fa.action_id AND mm.status = 'EXECUTED' AND date(mm.movement_date) <= date(?))), 0.0) AS expenses, " +
            "    (COALESCE((SELECT SUM(mm.amount) FROM MoneyMovement mm JOIN Inscription i ON mm.inscription_id = i.inscription_id WHERE i.action_id = fa.action_id AND mm.status = 'EXECUTED' AND date(mm.movement_date) <= date(?)), 0.0) + " +
            "     COALESCE((SELECT SUM(mm.amount) FROM MoneyMovement mm JOIN Invoice inv ON mm.invoice_id = inv.invoice_id WHERE inv.action_id = fa.action_id AND mm.status = 'EXECUTED' AND date(mm.movement_date) <= date(?)), 0.0)) AS balance " +
            "  FROM FormativeAction fa " +
            "  WHERE fa.creationDate IS NULL OR date(substr(fa.creationDate, 1, 10)) <= date(?) " +
            ") AS temporalFA " +
            "WHERE 1=1 "
        );

        	List<Object> params = new ArrayList<>();
        
        //Add parameters
        	for(int i=0; i<10; i++)
            	params.add(safeDate);
        	params.add(safeDate);
        	params.add(cutoffDate);
        	params.add(maxDateLimit);
        	
        	params.add(safeDate);
        	params.add(cutoffDate);
        	params.add(maxDateLimit);
        	for(int i=0; i<5; i++)
            	params.add(safeDate);
        		

        if(statusFilter != null && !statusFilter.isBlank()){
            if(!statusFilter.equals("ALL")){
            		sql.append("AND status = ? ");
            		params.add(statusFilter);
            }
        }
        else 
        		// "ACTIVE (Default)": All states not included CLOSED or Cancelled
            	sql.append("AND status != 'CLOSED' "); 

        sql.append("ORDER BY actionDate DESC");

        return db.executeQueryPojo(FormativeActionManagementDTO.class, sql.toString(), params.toArray());
    }

    
    //Obtains details from an FOrmative Action's ID (When selecting in the table)
    public FormativeActionDetailsDTO getActionDetails(int actionId){
        String sql = 
            "SELECT " +
            "    fa.objectives AS objectives, " +
            "    fa.mainContents AS mainContents, " +
            "    fa.location AS location, " +
            "    (SELECT GROUP_CONCAT(t.name, ', ') " +
            "     FROM Teacher t " +
            "     JOIN Teacher_FormativeAction tfa ON t.teacher_id = tfa.teacher_id " +
            "     WHERE tfa.action_id = fa.action_id) AS teachers, " +
            "    (SELECT COUNT(*) FROM Inscription i WHERE i.action_id = fa.action_id) AS totalRegisters " +
            "FROM FormativeAction fa " +
            "WHERE fa.action_id = ?";
        List<FormativeActionDetailsDTO> result = db.executeQueryPojo(FormativeActionDetailsDTO.class, sql, actionId);
        if(result.isEmpty())
            return null;
        
        return result.get(0);
    }
    
    // Gets the Community Fees for the Formative Action Selected
    public List<Object[]> getCourseFees(int actionId){
        String sql = "SELECT c.community_id, c.communityName, f.amount "
                   + "FROM Fee f JOIN Community c ON f.community_id = c.community_id "
                   + "WHERE f.action_id = ? ORDER BY c.communityName";
        return db.executeQueryArray(sql, actionId);
    }

    private String calculateCutoffDate(String dateStr){
        try{
            String fullDate = dateStr.length() == 10 ? dateStr + " 00:00:00" : dateStr;
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            java.time.LocalDateTime cutoff = java.time.LocalDateTime.parse(fullDate, formatter);
            int hoursToSubtract = 48; 
            while(hoursToSubtract > 0){
                cutoff = cutoff.minusHours(1);
                if(cutoff.getDayOfWeek() != java.time.DayOfWeek.SATURDAY && cutoff.getDayOfWeek() != java.time.DayOfWeek.SUNDAY)
                    hoursToSubtract--;
            }
            return cutoff.format(formatter);
        }catch (Exception e){
            return "1970-01-01 00:00:00"; 
        }
    }
   
}
