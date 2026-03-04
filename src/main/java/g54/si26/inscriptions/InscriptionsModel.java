package g54.si26.inscriptions;

import java.util.Date;
import java.util.List;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.DTOs.ProfessionalDTO;
import g54.si26.DTOs.InscriptionDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;
import g54.si26.utils.Util;

//Pa la gestión d inscripciones y Formative Actions
public class InscriptionsModel {

    //Creates the db
    private Database db = new Database();

    /* 
     * Llogra la llista d Formative Actions disponibles pa una fecha dada.
     * Criterios: Actives, en periodu d'inscripción y con places reales disponibles.
     */
    public List<FormativeActionDTO> getAvailableCourses(Date currentDate){
        validateNotNull(currentDate, "The consult date cannot be null");
        
        //Consulta llimpia y namái trai los cursos activos.
        String sql = "SELECT action_id AS actionId, name, spots, fee, "
                   + "startDate, endDate, inscriptionPeriodStart, inscriptionPeriodEnd, status "
                   + "FROM FormativeAction "
                   + "WHERE status = 'ACTIVE' "
                   + "AND ? >= inscriptionPeriodStart "
                   + "AND ? <= inscriptionPeriodEnd";

        String d = Util.dateToIsoString(currentDate);
        List<FormativeActionDTO> courses = db.executeQueryPojo(FormativeActionDTO.class, sql, d, d);

        //Buscai los q tien confirmed y reservaos pa comprobar los psots. Ignoramos el futuro.
        for(FormativeActionDTO course : courses){
            String sqlEnrolled = "SELECT COUNT(*) FROM Inscription WHERE action_id = ? AND state IN ('RECEIVED', 'CONFIRMED') AND inscription_date <= ?";
            List<Object[]> result = db.executeQueryArray(sqlEnrolled, course.getActionId(), d);
            
            int enrolledCount = Integer.parseInt(result.get(0)[0].toString());
            int availableSpots = course.getSpots() - enrolledCount;
            
            //Asignamos el valor correutu
            course.setAvailableSpots(availableSpots);
        }
        return courses;
    }
    

    //Devuelve todos los profesionales pal desplegable.
    public List<ProfessionalDTO> getAllProfessionals(){
        String sql = "SELECT professional_id AS professionalId, name, surname, phone, email FROM Professional";
        return db.executeQueryPojo(ProfessionalDTO.class, sql);
    }

    /* 
     * Makes the enrolment of a professional.
     * Tambn se encarga d borrai el futuro alternativo dl usuario.
     */
    public synchronized void enrollProfessional(ProfessionalDTO profesional, int actionId, Date simulatedDate){
    		//Null checks.
    		validateNotNull(profesional, "");
    		validateNotNull(profesional, "The professional object cannot be null");
    		validateNotNull(profesional.getName(), "The professional name cannot be null");
    		validateNotNull(profesional.getSurname(), "The professional surname cannot be null");
    		validateNotNull(profesional.getPhone(), "The professional phone cannot be null");
    		validateNotNull(profesional.getEmail(), "The email field cannot be null");
    		//We get the ID of the professional
    		int professionalId = getOrCreateProfessional(profesional);
        
    		//Facemos comprobaciones 
        //Borramos la inscripción de su yo dl futuro (inscripciones en las proximas 48h)
        clearFutureParadox(professionalId, actionId, simulatedDate);

        //Validar plazas en fecha actual
        validateAvailableSpots(actionId, simulatedDate);

        //Validar duplicados 
        validateNoDuplicateEnrollment(professionalId, actionId, simulatedDate);

        //Inscribir en la fecha simulada
        createInscription(professionalId, actionId, simulatedDate);
    }
    

    /*CAMBIAR: Enrolment pal día actual (esto ye redundante ahora)*/
    public synchronized void enrollProfessional(ProfessionalDTO profesional, int actionId){
        enrollProfessional(profesional, actionId, new Date());
    }

    //Calculates the future date in 48 working hrs.
    private String calculateFutureDate(Date simulatedDate){
        java.time.LocalDateTime future = java.time.LocalDateTime.ofInstant(
            simulatedDate.toInstant(), java.time.ZoneId.systemDefault()
        );
        
        int hoursFuture = 48;
        while (hoursFuture > 0){
        		future = future.plusHours(1);
            java.time.DayOfWeek day = future.getDayOfWeek();
            //Suma horas a no ser q sea finde
            if(day != java.time.DayOfWeek.SATURDAY && day != java.time.DayOfWeek.SUNDAY)
                hoursFuture--;
        }
        return future.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    
    //Same as the previous method but check in the past 48 wrkng hrs.
    private String calculateCutoffDate(Date simulatedDate){
        // Convertimos la fecha
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.ofInstant(
            simulatedDate.toInstant(), java.time.ZoneId.systemDefault()
        );

        int hoursToSubtract = 48;

        while (hoursToSubtract > 0){
            cutoff = cutoff.minusHours(1);
            // Comprobamos si el día es laborable (Lunes a Viernes).
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
        String sqlDelete = "DELETE FROM Inscription "
                         + "WHERE professional_id = ? AND action_id = ? "
                         + "AND inscription_date > ? AND inscription_date <= ?";
                         
        db.executeUpdate(sqlDelete, professionalId, actionId, currentDate, futureDate);
        //DEBUG: Quitar luego
        //System.out.println("PARADOJA RESUELTA: Se limpiaron posibles inscripciones futuras de este usuario.");
    }

    /*
     * Gets a professional with their email.
     * Si nun existe, facemos l'INSERT y recuperamos la ID buscando pol email.
     */
    private int getOrCreateProfessional(ProfessionalDTO profesional){
        String sqlSearch = "SELECT professional_id AS professionalId FROM Professional WHERE email = ?";
        List<ProfessionalDTO> result = db.executeQueryPojo(ProfessionalDTO.class, sqlSearch, profesional.getEmail());

        if(!result.isEmpty()) 
            return result.get(0).getProfessionalId();
        else{
            String sqlInsert = "INSERT INTO Professional (name, surname, phone, email) VALUES (?, ?, ?, ?)";            
            db.executeUpdate(sqlInsert, 
                    profesional.getName(), 
                    profesional.getSurname(), 
                    profesional.getPhone(), 
                    profesional.getEmail());
            List<ProfessionalDTO> newResult = db.executeQueryPojo(ProfessionalDTO.class, sqlSearch, profesional.getEmail());
            
            if(newResult.isEmpty())
                throw new ApplicationException("Error crítico: No se pudo recuperar el profesional recién insertado.");
            return newResult.get(0).getProfessionalId();
        }
    }


    //Checks if there's any places left comparing spots with COUNT of paid and received inscriptions, not CANCELLED
    private void validateAvailableSpots(int actionId, Date simulatedDate){
        String sqlMaxSpots = "SELECT spots, status FROM FormativeAction WHERE action_id = ?";
        List<Object[]> maxSpotsRows = db.executeQueryArray(sqlMaxSpots, actionId);
        
        if(maxSpotsRows.isEmpty() || maxSpotsRows.get(0)[0] == null)
            throw new ApplicationException("This Formative Action does not exist or could not be found");
        
        String status = maxSpotsRows.get(0)[1].toString();
        if(!"ACTIVE".equals(status)) 
            throw new ApplicationException("Security Error: The Formative Action is not ACTIVE.");
        
        int maxSpots = Integer.parseInt(maxSpotsRows.get(0)[0].toString());
        
        //Comprueba si hay places disponibles comparando 'spots' col 'COUNT' d'inscripciones.
        String simulatedDateStr = Util.dateToIsoString(simulatedDate);
        String sqlEnrolledCount = "SELECT COUNT(*) FROM Inscription WHERE action_id = ? AND state IN ('RECEIVED', 'CONFIRMED') AND inscription_date <= ?";
        List<Object[]> enrolledRows = db.executeQueryArray(sqlEnrolledCount, actionId, simulatedDateStr);
        int enrolledCount = Integer.parseInt(enrolledRows.get(0)[0].toString());

        //DEBUG
        /*
        System.out.println("Intentando matricular nel Cursu ID: " + actionId);
        System.out.println("Places totales (maxSpots): " + maxSpots);
        System.out.println("Xente yá apuntada (enrolledCount): " + enrolledCount);
        System.out.println("---------------------------");
         */
        if(enrolledCount >= maxSpots)
            throw new ApplicationException("This Formative Action is full.");
        
    }

    	//Checks if there's no duplicated inscriptions.
    private void validateNoDuplicateEnrollment(int professionalId, int actionId, Date simulatedDate){
        String simulatedDateStr = Util.dateToIsoString(simulatedDate);
        String sql = "SELECT inscription_id AS inscriptionId FROM Inscription "
                   + "WHERE professional_id = ? AND action_id = ? AND state != 'CANCELLED' AND inscription_date <= ?";

        List<InscriptionDTO> inscripciones = db.executeQueryPojo(InscriptionDTO.class, sql, professionalId, actionId, simulatedDateStr);
        if(!inscripciones.isEmpty())
            throw new ApplicationException("The professional is already enrolled (and active) in the Formative Action");
        
    }

    //Inserts the new inscription with date and received data
    private void createInscription(int professionalId, int actionId, Date simulatedDate){
        String simulatedDateStr = Util.dateToIsoString(simulatedDate);
        String sqlInsertInsc = "INSERT INTO Inscription (inscription_date, fee, state, professional_id, action_id) "
                             + "SELECT ?, fee, 'RECEIVED', ?, ? "
                             + "FROM FormativeAction WHERE action_id = ?";
        
        db.executeUpdate(sqlInsertInsc, simulatedDateStr, professionalId, actionId, actionId);
    }
    
    
    /*Actualiza el estado de las inscripciones basándose en la fecha simulada.
     *  Si pasaron 48h laborables: Pasa de RECEIVED a CANCELLED.
     * Si volvemos atrás en el tiempo: Pasa de CANCELLED a RECEIVED (si aún está en plazo).
     *  Si volvemos atrás en el tiempo: Pasa de RECEIVED a Borrado si el enrolment se hizo antes del día dao.
     */
    public void checkAndReleaseExpiredBookings(Date simulatedDate){

    		validateNotNull(simulatedDate, "The simulated date cannot be null");
    		//Calculamos la fecha límite (48h laborables)
        String cutoffDateIso = calculateCutoffDate(simulatedDate);

        //DEBUG
        /*
        System.out.println("\n--- ACTUALIZACIÓN DE ESTADOS (VIAJE EN EL TIEMPO) ---");
        System.out.println("Fecha actual simulada: " + Util.dateToIsoString(simulatedDate));
        System.out.println("Fecha de corte (48h antes): " + cutoffDateIso);
         */
        // Buscamos cuántas están en RECEIVED y son vieyas
        String sqlCheckCancel = "SELECT inscription_id FROM Inscription WHERE state = 'RECEIVED' AND inscription_date < ?";
        List<Object[]> aCancelar = db.executeQueryArray(sqlCheckCancel, cutoffDateIso);
        
        if(!aCancelar.isEmpty()){
            String sqlCancel = "UPDATE Inscription SET state = 'CANCELLED' "
                             + "WHERE state = 'RECEIVED' AND inscription_date < ?";
            db.executeUpdate(sqlCancel, cutoffDateIso);
            //DEBUG, QUITAR LUEGO
            
            //System.out.println("CADUCADAS: Se han cancelado " + aCancelar.size() + " inscripciones.");
        }
        /*else
            //DEBUG, QUITAR LUEGO
            System.out.println("CADUCADAS: Ninguna (Todo está al día).");
       */
        // Buscamos cuántas están en CANCELLED pero su fecha es POSTERIOR a la de corte
        // Esto es pa los viajes en el tiempo, al cambiar fecha, igual necesitamos cambiar el estado d las inscripciones.
        String sqlCheckRecover = "SELECT inscription_id FROM Inscription WHERE state = 'CANCELLED' AND inscription_date >= ?";
        List<Object[]> aRecuperar = db.executeQueryArray(sqlCheckRecover, cutoffDateIso);

        if(!aRecuperar.isEmpty()){
            String sqlRecover = "UPDATE Inscription SET state = 'RECEIVED' "
                              + "WHERE state = 'CANCELLED' AND inscription_date >= ?";
            db.executeUpdate(sqlRecover, cutoffDateIso);
            //DEBUG
            //System.out.println("RECUPERADAS: Se han reactivado " + aRecuperar.size() + " inscripciones (Volviste al pasado).");
        }/*
        else 
        	//DEBUG
            System.out.println("✅ RECUPERADAS: Ninguna.");     
        
        System.out.println("-----------------------------------------------------\n");
        */
    }

    

    /* De uso general para validacion de objetos */
    private void validateNotNull(Object obj, String messages){
        if(obj == null)
            throw new ApplicationException(messages);
        if(obj instanceof String && ((String) obj).trim().isEmpty())
            throw new ApplicationException(messages);
    }
    
}