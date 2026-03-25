package g54.si26.inscriptions;

import java.util.Date;
import java.util.List;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.DTOs.ProfessionalDTO;
import g54.si26.DTOs.InscriptionDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;
import g54.si26.utils.Util;

public class InscriptionsModel {

    private Database db = new Database();
    
    public List<FormativeActionDTO> getAvailableCourses(Date currentDate){
        validateNotNull(currentDate, "The consult date cannot be null");
        
        String sql = "SELECT action_id AS actionId, name, spots, "
                   + "startDate, endDate, inscriptionPeriodStart, inscriptionPeriodEnd, status "
                   + "FROM FormativeAction "
                   + "WHERE status IN ('ACTIVE', 'CLOSED') "
                   + "AND ? >= inscriptionPeriodStart "
                   + "AND ? <= inscriptionPeriodEnd";

        String d = dateToTimestamp(currentDate); 
        List<FormativeActionDTO> courses = db.executeQueryPojo(FormativeActionDTO.class, sql, d, d);

        for(FormativeActionDTO course : courses){
            String sqlEnrolled = "SELECT COUNT(*) FROM Inscription WHERE action_id = ? AND state IN ('RECEIVED', 'CONFIRMED') AND inscription_date <= ?";
            List<Object[]> result = db.executeQueryArray(sqlEnrolled, course.getActionId(), d);
        
            int enrolledCount = Integer.parseInt(result.get(0)[0].toString());
            int availableSpots = course.getSpots() - enrolledCount;
            course.setAvailableSpots(availableSpots);
        }
        return courses;
    }

    public List<Object[]> getCourseFees(int actionId){
        String sql = "SELECT c.community_id, c.communityName, f.amount "
           + "FROM Fee f JOIN Community c ON f.community_id = c.community_id "
           + "WHERE f.action_id = ? ORDER BY c.communityName";
        return db.executeQueryArray(sql, actionId);
    }

    public List<ProfessionalDTO> getAllProfessionals(){
        String sql = "SELECT professional_id AS professionalId, name, surname, phone, email FROM Professional";
        return db.executeQueryPojo(ProfessionalDTO.class, sql);
    }

    public synchronized void enrollProfessional(ProfessionalDTO profesional, int actionId, int communityId, Date simulatedDate){
        validateNotNull(profesional, "The professional object cannot be null");
        validateNotNull(profesional.getName(), "The professional name cannot be null");
        validateNotNull(profesional.getSurname(), "The professional surname cannot be null");
        validateNotNull(profesional.getPhone(), "The professional phone cannot be null");
        validateNotNull(profesional.getEmail(), "The email field cannot be null");
            
        int professionalId = getOrCreateProfessional(profesional, communityId);
        clearFutureParadox(professionalId, actionId, simulatedDate);
        validateAvailableSpots(actionId, simulatedDate);
        validateNoDuplicateEnrollment(professionalId, actionId, simulatedDate);
        createInscription(professionalId, actionId, communityId, simulatedDate);
    }

    private String calculateFutureDate(Date simulatedDate){
        java.time.LocalDateTime future = java.time.LocalDateTime.ofInstant(simulatedDate.toInstant(), java.time.ZoneId.systemDefault());
        int hoursFuture = 48;         
        while (hoursFuture > 0){
            future = future.plusHours(1);
            if(future.getDayOfWeek() != java.time.DayOfWeek.SATURDAY && future.getDayOfWeek() != java.time.DayOfWeek.SUNDAY) hoursFuture--;
        }
        return future.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    private String calculateCutoffDate(Date simulatedDate){
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.ofInstant(simulatedDate.toInstant(), java.time.ZoneId.systemDefault());
        int hoursToSubtract = 48; 
        while (hoursToSubtract > 0){
            cutoff = cutoff.minusHours(1);
            if(cutoff.getDayOfWeek() != java.time.DayOfWeek.SATURDAY && cutoff.getDayOfWeek() != java.time.DayOfWeek.SUNDAY) hoursToSubtract--;
        }
        return cutoff.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private void clearFutureParadox(int professionalId, int actionId, Date simulatedDate){
        String currentDate = dateToTimestamp(simulatedDate); 
        String futureDate = calculateFutureDate(simulatedDate);
        String sqlDelete = "DELETE FROM Inscription WHERE professional_id = ? AND action_id = ? AND inscription_date > ? AND inscription_date <= ?";
        db.executeUpdate(sqlDelete, professionalId, actionId, currentDate, futureDate);
    }

    private int getOrCreateProfessional(ProfessionalDTO profesional, int communityId){
        String sqlSearch = "SELECT professional_id AS professionalId, email, phone FROM Professional WHERE email = ? OR phone = ?";
        List<ProfessionalDTO> result = db.executeQueryPojo(ProfessionalDTO.class, sqlSearch, profesional.getEmail(), profesional.getPhone());

        if (result.isEmpty()) {
            String sqlInsert = "INSERT INTO Professional (name, surname, phone, email, community_id) VALUES (?, ?, ?, ?, ?)";            
            db.executeUpdate(sqlInsert, profesional.getName(), profesional.getSurname(), profesional.getPhone(), profesional.getEmail(), communityId);
            String sqlSearchNew = "SELECT professional_id AS professionalId FROM Professional WHERE email = ?";
            List<ProfessionalDTO> newResult = db.executeQueryPojo(ProfessionalDTO.class, sqlSearchNew, profesional.getEmail());
            return newResult.get(0).getProfessionalId();
        }
        if (result.size() == 1 && result.get(0).getEmail().equalsIgnoreCase(profesional.getEmail()) && result.get(0).getPhone().equals(profesional.getPhone())) 
            return result.get(0).getProfessionalId();
        else 
            throw new ApplicationException("Error: Email or phone already in use.");
    }

    private void validateAvailableSpots(int actionId, Date simulatedDate){
        String sqlMaxSpots = "SELECT spots, status FROM FormativeAction WHERE action_id = ?";
        List<Object[]> maxSpotsRows = db.executeQueryArray(sqlMaxSpots, actionId);
        if(maxSpotsRows.isEmpty()) throw new ApplicationException("Course not found.");
        
        String status = maxSpotsRows.get(0)[1].toString();
        if(!"ACTIVE".equals(status) && !"CLOSED".equals(status)) throw new ApplicationException("Not available.");
        
        int maxSpots = Integer.parseInt(maxSpotsRows.get(0)[0].toString());
        String d = dateToTimestamp(simulatedDate); 
        String sqlCount = "SELECT COUNT(*) FROM Inscription WHERE action_id = ? AND state IN ('RECEIVED', 'CONFIRMED') AND inscription_date <= ?";
        List<Object[]> enrolledRows = db.executeQueryArray(sqlCount, actionId, d);
        if(Integer.parseInt(enrolledRows.get(0)[0].toString()) >= maxSpots) throw new ApplicationException("Course is full.");
    }

    private void validateNoDuplicateEnrollment(int professionalId, int actionId, Date simulatedDate){
        String d = dateToTimestamp(simulatedDate); 
        String sql = "SELECT inscription_id FROM Inscription WHERE professional_id = ? AND action_id = ? AND state != 'CANCELLED' AND inscription_date <= ?";
        if(!db.executeQueryArray(sql, professionalId, actionId, d).isEmpty()) throw new ApplicationException("Already enrolled.");
    }

    private void createInscription(int professionalId, int actionId, int communityId, Date simulatedDate){
        String d = dateToTimestamp(simulatedDate); 
        String sqlFee = "SELECT amount FROM Fee WHERE action_id = ? AND community_id = ?";
        List<Object[]> feeRes = db.executeQueryArray(sqlFee, actionId, communityId);
        double feeAmount = 0.0;
        if(!feeRes.isEmpty())
            feeAmount = Double.parseDouble(feeRes.get(0)[0].toString());

        String state = (feeAmount == 0.0) ? "CONFIRMED" : "RECEIVED";

        String sqlInsertInsc = "INSERT INTO Inscription (inscription_date, applied_fee, state, professional_id, action_id) "
                            + "VALUES (?, ?, ?, ?, ?)";

        db.executeUpdate(sqlInsertInsc, d, feeAmount, state, professionalId, actionId);
        db.executeUpdate("UPDATE FormativeAction SET status = 'ACTIVE' WHERE action_id = ? AND status = 'CLOSED'", actionId);
    }
    
    public void checkAndReleaseExpiredBookings(Date simulatedDate){
        validateNotNull(simulatedDate, "Simulated date null");
        String cutoffDateIso = calculateCutoffDate(simulatedDate);

        // Se ha sustituido Payment por MoneyMovement, filtrando por movimientos positivos (ingresos) vinculados a la inscripción
        db.executeUpdate("UPDATE Inscription SET state = 'RECEIVED' WHERE state = 'CONFIRMED' AND inscription_id NOT IN (SELECT inscription_id FROM MoneyMovement WHERE amount > 0)");

        String sqlAudit = "SELECT i.inscription_id, i.inscription_date, m.movement_date, i.state "
                        + "FROM Inscription i JOIN MoneyMovement m ON i.inscription_id = m.inscription_id "
                        + "WHERE m.amount > 0 AND i.state IN ('RECEIVED', 'CONFIRMED')";
        List<Object[]> auditRows = db.executeQueryArray(sqlAudit);
        
        for(Object[] row : auditRows){
            int id = Integer.parseInt(row[0].toString());
            Date inscDate = timestampToDate(row[1].toString());
            String maxValidDateStr = calculateFutureDate(inscDate);
            String payDateStr = row[2].toString();
            if(payDateStr.length() == 10) payDateStr += " 00:00:00";
            
            if(payDateStr.compareTo(maxValidDateStr) > 0)
                db.executeUpdate("UPDATE Inscription SET state = 'CANCELLED' WHERE inscription_id = ?", id);
            else if ("RECEIVED".equals(row[3].toString())) 
                db.executeUpdate("UPDATE Inscription SET state = 'CONFIRMED' WHERE inscription_id = ?", id);
        }

        db.executeUpdate("UPDATE Inscription SET state = 'CANCELLED' WHERE state = 'RECEIVED' AND inscription_date < ?", cutoffDateIso);
        db.executeUpdate("UPDATE Inscription SET state = 'RECEIVED' WHERE state = 'CANCELLED' AND inscription_date >= ?", cutoffDateIso);
    }

    private String dateToTimestamp(Date date) { return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date); }
    private Date timestampToDate(String timestamp) {
        try { return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestamp.length() == 10 ? timestamp + " 00:00:00" : timestamp); }
        catch (Exception e) { return new Date(); }
    }
    private void validateNotNull(Object obj, String messages){ if(obj == null || (obj instanceof String && ((String) obj).trim().isEmpty())) throw new ApplicationException(messages); }
}
