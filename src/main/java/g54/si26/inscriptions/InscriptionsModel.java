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

    // Creates the db
    private Database db = new Database();
    
    //Gets the list of availablee Formative Actions for the given date (It must be available for the given date and in the enrolment period) 
    	public List<FormativeActionDTO> getAvailableCourses(Date currentDate){
    		validateNotNull(currentDate, "The consult date cannot be null");
        
    		//Since the Closed attribute has no date, we'll show the FA that are also closed for the given date if it's within the
    		//enrolment period since a course cannot be closed before endinf¡g.
    		String sql = "SELECT action_id AS actionId, name, spots, fee, "
                   + "startDate, endDate, inscriptionPeriodStart, inscriptionPeriodEnd, status "
                   + "FROM FormativeAction "
                   + "WHERE status IN ('ACTIVE', 'CLOSED') "
                   + "AND ? >= inscriptionPeriodStart "
                   + "AND ? <= inscriptionPeriodEnd";

    		String d = Util.dateToIsoString(currentDate);
    		List<FormativeActionDTO> courses = db.executeQueryPojo(FormativeActionDTO.class, sql, d, d);

    		//We check CONFIRMED and RESERVED until now. Ingoring the future. 
    		for(FormativeActionDTO course : courses){
    			String sqlEnrolled = "SELECT COUNT(*) FROM Inscription WHERE action_id = ? AND state IN ('RECEIVED', 'CONFIRMED') AND inscription_date <= ?";
    			List<Object[]> result = db.executeQueryArray(sqlEnrolled, course.getActionId(), d);
            
    			int enrolledCount = Integer.parseInt(result.get(0)[0].toString());
    			int availableSpots = course.getSpots() - enrolledCount;
            course.setAvailableSpots(availableSpots);
    		}
    		return courses;
    	}
    

    	//Returns the professionals for the dropdown
    	public List<ProfessionalDTO> getAllProfessionals(){
    		String sql = "SELECT professional_id AS professionalId, name, surname, phone, email FROM Professional";
    		return db.executeQueryPojo(ProfessionalDTO.class, sql);
    	}

    /* * Makes the enrolment of a professional.
     * Tambn se encarga d borrai el futuro alternativo dl usuario.
     */
    	public synchronized void enrollProfessional(ProfessionalDTO profesional, int actionId, Date simulatedDate){
    		// Null checks.
    		validateNotNull(profesional, "The professional object cannot be null");
    		validateNotNull(profesional.getName(), "The professional name cannot be null");
    		validateNotNull(profesional.getSurname(), "The professional surname cannot be null");
    		validateNotNull(profesional.getPhone(), "The professional phone cannot be null");
    		validateNotNull(profesional.getEmail(), "The email field cannot be null");
            
    		// We get the ID of the professional
    		int professionalId = getOrCreateProfessional(profesional);
        
    		//Delete the fture inscriptions
        clearFutureParadox(professionalId, actionId, simulatedDate);

        // check fr places for the given date
        validateAvailableSpots(actionId, simulatedDate);

        // check for duples
        validateNoDuplicateEnrollment(professionalId, actionId, simulatedDate);

        //Finally enrol
        	createInscription(professionalId, actionId, simulatedDate);
    	}
    

    /* CAMBIAR: Enrolment pal día actual (esto ye redundante ahora) */
    	public synchronized void enrollProfessional(ProfessionalDTO profesional, int actionId){
    		enrollProfessional(profesional, actionId, new Date());
    	}

    	// Calculates the future date in 48 working hrs.
    	private String calculateFutureDate(Date simulatedDate){
    		java.time.LocalDateTime future = java.time.LocalDateTime.ofInstant(
    			simulatedDate.toInstant(), java.time.ZoneId.systemDefault()
    		);

        int hoursFuture = 48;         
        	while (hoursFuture > 0){
            future = future.plusHours(1);
            java.time.DayOfWeek day = future.getDayOfWeek();
            // Suma horas a no ser q sea finde
            if(day != java.time.DayOfWeek.SATURDAY && day != java.time.DayOfWeek.SUNDAY)
                hoursFuture--;
        }
        return future.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    
    // Same as the previous method but checks the past 48 wrkng hrs.
    private String calculateCutoffDate(Date simulatedDate){
        // Convertimos la fecha
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.ofInstant(
            simulatedDate.toInstant(), java.time.ZoneId.systemDefault()
        );

        int hoursToSubtract = 48; 

        while (hoursToSubtract > 0){
            cutoff = cutoff.minusHours(1);
            // Check for working days
            java.time.DayOfWeek day = cutoff.getDayOfWeek();
            	if(day != java.time.DayOfWeek.SATURDAY && day != java.time.DayOfWeek.SUNDAY)
                hoursToSubtract--;
        }
        return cutoff.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    
    /*
     * Deletes the inscription if there was any new inscription in the next 48 wrkn hors.
     * Only used if we make a trip to the past and we need to adjust the present or the future. 
     */
    	private void clearFutureParadox(int professionalId, int actionId, Date simulatedDate){
    		String currentDate = Util.dateToIsoString(simulatedDate);
    		String futureDate = calculateFutureDate(simulatedDate);
        
    		// Borramos si la inscripción dl usuario es estrictamente mayor q la simulada y menor q +48h
    		//Delete if the inscription is not within the simulated date and the next 48h 
        String sqlDelete = "DELETE FROM Inscription "
                         + "WHERE professional_id = ? AND action_id = ? "
                         + "AND inscription_date > ? AND inscription_date <= ?";
                         
        db.executeUpdate(sqlDelete, professionalId, actionId, currentDate, futureDate);
    }

    /*
     * Gets a professional looking by email OR phone.
     * Bloquea si intentan mezclar datos de personas distintas.
     */
    private int getOrCreateProfessional(ProfessionalDTO profesional){
        String sqlSearch = "SELECT professional_id AS professionalId, email, phone FROM Professional WHERE email = ? OR phone = ?";
        List<ProfessionalDTO> result = db.executeQueryPojo(ProfessionalDTO.class, sqlSearch, profesional.getEmail(), profesional.getPhone());

        if (result.isEmpty()) {
        		//No email or phone in the DB. New enrolment.
            	String sqlInsert = "INSERT INTO Professional (name, surname, phone, email) VALUES (?, ?, ?, ?)";            
            	db.executeUpdate(sqlInsert, profesional.getName(), profesional.getSurname(), profesional.getPhone(), profesional.getEmail());
            
            	//Search by email.
            	String sqlSearchNew = "SELECT professional_id AS professionalId FROM Professional WHERE email = ?";
            	List<ProfessionalDTO> newResult = db.executeQueryPojo(ProfessionalDTO.class, sqlSearchNew, profesional.getEmail());
            
            	if(newResult.isEmpty())
            		throw new ApplicationException("Error crítico: No se pudo recuperar el profesional recién insertado.");
            		return newResult.get(0).getProfessionalId();

        		}
        		if (result.size() == 1 && result.get(0).getEmail().equalsIgnoreCase(profesional.getEmail()) && result.get(0).getPhone().equals(profesional.getPhone())) 
        			return result.get(0).getProfessionalId();
        		else 
        			//Something is repeated (email or phone)
        			throw new ApplicationException("No se puede realizar la inscripción: El email o el teléfono introducidos ya están en uso en el COIIPA.");
    }


    // Checks if there's any places left comparing spots with COUNT of paid and received inscriptions.
    	private void validateAvailableSpots(int actionId, Date simulatedDate){
    		String sqlMaxSpots = "SELECT spots, status FROM FormativeAction WHERE action_id = ?";
    		List<Object[]> maxSpotsRows = db.executeQueryArray(sqlMaxSpots, actionId);
        
    		if(maxSpotsRows.isEmpty() || maxSpotsRows.get(0)[0] == null)
    			throw new ApplicationException("This Formative Action does not exist or could not be found");
        
    		String status = maxSpotsRows.get(0)[1].toString();
    		//We allow CLOSED courses to reopen if we are within the enrolment period
    		if(!"ACTIVE".equals(status) && !"CLOSED".equals(status)) 
    			throw new ApplicationException("Security Error: The Formative Action is not available for enrollment.");
        
    		int maxSpots = Integer.parseInt(maxSpotsRows.get(0)[0].toString());
        
    		//Check for free places
    		String simulatedDateStr = Util.dateToIsoString(simulatedDate);
    		String sqlEnrolledCount = "SELECT COUNT(*) FROM Inscription WHERE action_id = ? AND state IN ('RECEIVED', 'CONFIRMED') AND inscription_date <= ?";
    		List<Object[]> enrolledRows = db.executeQueryArray(sqlEnrolledCount, actionId, simulatedDateStr);
    		int enrolledCount = Integer.parseInt(enrolledRows.get(0)[0].toString());

    		if(enrolledCount >= maxSpots)
    			throw new ApplicationException("This Formative Action is full.");
        
    	}

    	// Checks if there's no duplicated inscriptions.
    	private void validateNoDuplicateEnrollment(int professionalId, int actionId, Date simulatedDate){
    		String simulatedDateStr = Util.dateToIsoString(simulatedDate);
    		String sql = "SELECT inscription_id AS inscriptionId FROM Inscription "
    					+ "WHERE professional_id = ? AND action_id = ? AND state != 'CANCELLED' AND inscription_date <= ?";

    		List<InscriptionDTO> inscripciones = db.executeQueryPojo(InscriptionDTO.class, sql, professionalId, actionId, simulatedDateStr);
    		if(!inscripciones.isEmpty())
    			throw new ApplicationException("The professional is already enrolled (and active) in the Formative Action");
    	}

    	// Inserts the new inscription with date and received data, and triggers the Butterfly Effect.
    	private void createInscription(int professionalId, int actionId, Date simulatedDate){
    		String simulatedDateStr = Util.dateToIsoString(simulatedDate);

    		//Create new inscription with state RECEIVED
    		String sqlInsertInsc = "INSERT INTO Inscription (inscription_date, fee, state, professional_id, action_id) "
                             + "SELECT ?, fee, 'RECEIVED', ?, ? "
                             + "FROM FormativeAction WHERE action_id = ?";
    		db.executeUpdate(sqlInsertInsc, simulatedDateStr, professionalId, actionId, actionId);

    		//If someone enrols in a closed FA, we reopen it since now there is an unhandled registration.
    		String sqlReactivate = "UPDATE FormativeAction SET status = 'ACTIVE' WHERE action_id = ? AND status = 'CLOSED'";
    		db.executeUpdate(sqlReactivate, actionId);
    	}
    
    
    
    	//Releases expired bookings after modifing the date
    	public void checkAndReleaseExpiredBookings(Date simulatedDate){

    		validateNotNull(simulatedDate, "The simulated date cannot be null");
    		String cutoffDateIso = calculateCutoffDate(simulatedDate);

    		// If there are no payment data we put the state of the enrolment into received 
    		db.executeUpdate("UPDATE Inscription SET state = 'RECEIVED' WHERE state = 'CONFIRMED' AND inscription_id NOT IN (SELECT inscription_id FROM Payment)");

        	//bring al the inscriptions with associated apyments
        	String sqlAudit = "SELECT i.inscription_id, i.inscription_date, p.payment_date, i.state "
                        + "FROM Inscription i JOIN Payment p ON i.inscription_id = p.inscription_id "
                        + "WHERE i.state IN ('RECEIVED', 'CONFIRMED')";
        	List<Object[]> auditRows = db.executeQueryArray(sqlAudit);
        
        	for(Object[] row : auditRows){
        		int id = Integer.parseInt(row[0].toString());
        		String inscDateStr = row[1].toString();
        		String payDateStr = row[2].toString(); 
        		String currentState = row[3].toString();
            
        		//48 wrkng hrs
        		Date inscDate = Util.isoStringToDate(inscDateStr);
        		String maxValidDateStr = calculateFutureDate(inscDate);

            if(payDateStr.compareTo(maxValidDateStr) > 0)
                db.executeUpdate("UPDATE Inscription SET state = 'CANCELLED' WHERE inscription_id = ?", id);
            else 
            		//Pays in time
            		if ("RECEIVED".equals(currentState)) 
            			db.executeUpdate("UPDATE Inscription SET state = 'CONFIRMED' WHERE inscription_id = ?", id);
            
        }

        	//The ones that are left are because has not been paid in the time
        db.executeUpdate("UPDATE Inscription SET state = 'CANCELLED' WHERE state = 'RECEIVED' AND inscription_date < ?", cutoffDateIso);

        //If we come back in time we return the state as received
        db.executeUpdate("UPDATE Inscription SET state = 'RECEIVED' WHERE state = 'CANCELLED' AND inscription_date >= ?", cutoffDateIso);
    }

    

    /* De uso general para validacion de objetos */
    private void validateNotNull(Object obj, String messages){
        if(obj == null)
            throw new ApplicationException(messages);
        if(obj instanceof String && ((String) obj).trim().isEmpty())
            throw new ApplicationException(messages);
    }
    
}