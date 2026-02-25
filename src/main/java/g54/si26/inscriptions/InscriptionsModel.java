package g54.si26.inscriptions;

import java.util.Date;
import java.util.List;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.DTOs.ProfessionalDTO;
import g54.si26.DTOs.InscriptionDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;
import g54.si26.utils.Util;

/**
 * Modelu pa la xestión d'inscripciones y aiciones formatives.
 * Implementa la lóxica de negociu pa la US d'inscripción de profesionales.
 */
public class InscriptionsModel {

    //Creates the db
    private Database db = new Database();

    /*Llogra la llista d Formative Actions disponibles pa una fecha dada.
     * Criterios: Actives, en periodu d'inscripción y con places reales disponibles.
     */
    public List<FormativeActionDTO> getAvailableCourses(Date currentDate) {
        validateNotNull(currentDate, "The consult date cannot be null");
        
        //Conseguimos los q aún tienen plazas
        String sql = "SELECT action_id AS actionId, name, spots, fee, "
                   + "startDate, endDate, status "
                   + "FROM FormativeAction "
                   + "WHERE status = 'ACTIVE' "
                   + "AND ? >= inscriptionPeriodStart "
                   + "AND ? <= inscriptionPeriodEnd "
                   + "AND spots > (SELECT COUNT(*) FROM Inscription WHERE action_id = FormativeAction.action_id)";

        String d = Util.dateToIsoString(currentDate);
        return db.executeQueryPojo(FormativeActionDTO.class, sql, d, d);
    }

    /**
     * Realiza la inscripción completa d'un professional.
     * Pasos: validar places, gestionar profesional, validar duplicaos ya inxertar.
     */
    public void enrollProfessional(ProfessionalDTO profesional, int actionId) {
        validateNotNull(profesional.getEmail(), "The email field cannot be null");
        validateNotNull(profesional.getName(), "The professional name cannot be null");

        //Validate that there's still available spots
        validateAvailableSpots(actionId);

        //Create the professional if its new or get the ID if it's not
        int professionalId = getOrCreateProfessional(profesional);

        //Check for no duplicate enrollment
        validateNoDuplicateEnrollment(professionalId, actionId);

        //Finally we inscprit the professional
        createInscription(professionalId, actionId);
    }

    /*get al profesional pol corréu electrónicu.
     * Si nun existe, faemos l'INSERT y recuperamos la ID automáticamente gracies.
     */
    private int getOrCreateProfessional(ProfessionalDTO profesional) {
        //Buscamos si existe
        String sqlSearch = "SELECT professional_id AS professionalId FROM Professional WHERE email = ?";
        List<ProfessionalDTO> result = db.executeQueryPojo(ProfessionalDTO.class, sqlSearch, profesional.getEmail());

        if (!result.isEmpty()) 
        	 	//Ya taba na base de datos
            return result.get(0).getProfessionalId();
        else {
            //Nun existe: inxertamos y recuperamos la ID (autoincremental) nun solu pasu
            String sqlInsert = "INSERT INTO Professional (name, surname, phone, email) VALUES (?, ?, ?, ?)";
            Object generatedKey = db.executeInsert(sqlInsert, 
                    profesional.getName(), 
                    profesional.getSurname(), 
                    profesional.getPhone(), 
                    profesional.getEmail());

            // Convertimos l'oxetu recuperáu a int
            return Integer.parseInt(generatedKey.toString());
        }
    }

    /*Comprueba si hay places disponibles comparando 'spots' col 'COUNT' d'inscripciones.
     */
    private void validateAvailableSpots(int actionId) {
        //1. Sacamos la capacidá máxima dl cursu
        String sqlMaxSpots = "SELECT spots FROM FormativeAction WHERE action_id = ?";
        List<Object[]> maxSpotsRows = db.executeQueryArray(sqlMaxSpots, actionId);
        
        if (maxSpotsRows.isEmpty() || maxSpotsRows.get(0)[0] == null) {
            throw new ApplicationException("This Formative Action does not exist or could not be found");
        }
        int maxSpots = Integer.parseInt(maxSpotsRows.get(0)[0].toString());

        // 2. Cuntamos cuántos alumnos hay apuntaos nesti cursu
        String sqlEnrolledCount = "SELECT COUNT(*) FROM Inscription WHERE action_id = ?";
        List<Object[]> enrolledRows = db.executeQueryArray(sqlEnrolledCount, actionId);
        int enrolledCount = Integer.parseInt(enrolledRows.get(0)[0].toString());

        //3. Comparamos si ta lleno. 
        if (enrolledCount >= maxSpots)
        	//Sentímoslo, yá nun queden places disponibles pa esti cursu.
            throw new ApplicationException("This Formative Action is full.");
        
    }

    /*Comprueba que nun exista ya una inscripción pa esa combinación
     */
    private void validateNoDuplicateEnrollment(int professionalId, int actionId) {
        String sql = "SELECT inscription_id AS inscriptionId FROM Inscription WHERE professional_id = ? AND action_id = ?";
        List<InscriptionDTO> inscripciones = db.executeQueryPojo(InscriptionDTO.class, sql, professionalId, actionId);
        
        if (!inscripciones.isEmpty()) {
            throw new ApplicationException("The professinoal is already enrolled in the Formative Action");
        }
    }

    /*Inxerta la nueva inscripción cola fecha d'agora y estau 'RECEIVED'.
     */
    private void createInscription(int professionalId, int actionId) {
        String sqlInsertInsc = "INSERT INTO Inscription (inscription_date, fee, state, professional_id, action_id) "
                             + "SELECT datetime('now', 'localtime'), fee, 'RECEIVED', ?, ? "
                             + "FROM FormativeAction WHERE action_id = ?";
        
        db.executeUpdate(sqlInsertInsc, professionalId, actionId, actionId);
    }

    /* De uso general para validacion de objetos */
	private void validateNotNull(Object obj, String message) {
		if (obj == null)
			throw new ApplicationException(message);
	}
    
}