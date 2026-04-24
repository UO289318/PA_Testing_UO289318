package g54.si26.secretaryStatusFA;

import g54.si26.secretaryStatusFA.dto.FARegistrationDTO;
import g54.si26.secretaryStatusFA.dto.FAStatusDTO;
import g54.si26.tmConsulting.TMConsultingModel;
import g54.si26.utils.BaseModel;
import g54.si26.utils.Database;
import java.util.List;

public class StatusFAModel extends BaseModel {

    private final TMConsultingModel tmModel = new TMConsultingModel();

    /**
     * Retrieves the list of formative actions with basic status information.
     * Uses a simplified version of the status logic found in the project.
     */
    public List<FAStatusDTO> getFormativeActions(String simulatedDate) {
    	String safeDate = (simulatedDate != null && !simulatedDate.isBlank()) ? simulatedDate.substring(0, 10) : "9999-12-31";
        String statusSql = getTemporalFaStatusSql(simulatedDate, "fa");
        String sql = 
            "SELECT " +
            "    fa.action_id AS actionId, " +
            "    fa.name AS name, " +
            "    (" + statusSql + ") AS status, " +
            "    fa.inscriptionPeriodStart AS inscriptionPeriodStart, " +
            "    fa.inscriptionPeriodEnd AS inscriptionPeriodEnd, " +
            "    fa.startDate AS startDate, " +
            "    fa.endDate AS endDate, " +
            "    fa.spots AS totalSpots, " +
            
            "    (SELECT COUNT(*) FROM Inscription i WHERE i.action_id = fa.action_id AND i.state = 'CONFIRMED' AND date(i.inscription_date) <= date('" + safeDate + "')) AS confirmedPlaces " +
            "FROM FormativeAction fa " +
            "WHERE fa.creationDate IS NULL OR date(substr(fa.creationDate, 1, 10)) <= date('" + safeDate + "') " +
            "ORDER BY fa.startDate DESC";
        
        return db.executeQueryPojo(FAStatusDTO.class, sql);
    }

    /**
     * Retrieves detailed financial metrics for a specific formative action.
     */
    public FAStatusDTO getFADetail(int actionId, String simulatedDate) {
    	
        // We get the basic data first
    		String safeDate = (simulatedDate != null && !simulatedDate.isBlank()) ? simulatedDate.substring(0, 10) : "9999-12-31";
        String statusSql = getTemporalFaStatusSql(simulatedDate, "fa");
        String sqlBasic = 
            "SELECT " +
            "    fa.action_id AS actionId, " +
            "    fa.name AS name, " +
            "    (" + statusSql + ") AS status, " +
            "    fa.inscriptionPeriodStart AS inscriptionPeriodStart, " +
            "    fa.inscriptionPeriodEnd AS inscriptionPeriodEnd, " +
            "    fa.startDate AS startDate, " +
            "    fa.endDate AS endDate, " +
            "    fa.spots AS totalSpots, " +
            "    (SELECT COUNT(*) FROM Inscription i WHERE i.action_id = fa.action_id AND i.state = 'CONFIRMED' AND date(i.inscription_date) <= date('" + safeDate + "')) AS confirmedPlaces " +
            "FROM FormativeAction fa " +
            "WHERE fa.action_id = ?";
        
        List<FAStatusDTO> results = db.executeQueryPojo(FAStatusDTO.class, sqlBasic, actionId);
        if (results.isEmpty()) return null;
        
        FAStatusDTO dto = results.get(0);
        
        // Use TMConsultingModel for financial metrics to ensure consistent logic across US
        double confIncome = tmModel.getConfirmedIncome(actionId, simulatedDate);
        dto.setConfirmedIncome(confIncome);
        
        double confExpenses = tmModel.getConfirmedExpenses(actionId, simulatedDate);
        dto.setConfirmedExpenses(confExpenses);
        
        double totalRemuneration = tmModel.getTotalRemuneration(actionId);
        dto.setTotalRemuneration(totalRemuneration);
        
        // New consistent estimation logic using the same formulas as TMConsulting
        double avgFee = tmModel.getAvgFee(actionId);
        int pendingInscriptions = tmModel.getPendingInscriptionsCount(actionId, simulatedDate);
        int confirmedInscriptions = tmModel.getConfirmedInscriptionsCount(actionId, simulatedDate);
        
        double estimatedIncome = tmModel.calculateEstimatedIncome(dto.getTotalSpots(), confirmedInscriptions, pendingInscriptions, avgFee, confIncome);
        dto.setEstimatedIncome(estimatedIncome);
        
        double totalEstimatedExpenses = tmModel.calculateEstimatedExpenses(confExpenses, totalRemuneration);
        // The StatusFA DTO treats 'Estimated Expenses' as the ADDITIONAL amount expected
        // based on the original implementation (Total Remuneration - Confirmed Expenses)
        dto.setEstimatedExpenses(Math.max(0, totalEstimatedExpenses - confExpenses));
        
        return dto;
    }

    /**
     * Retrieves the list of registrations for a specific formative action.
     */
    public List<FARegistrationDTO> getFARegistrations(int actionId, String simulatedDate) {
    		String safeDate = (simulatedDate != null && !simulatedDate.isBlank()) ? simulatedDate.substring(0, 10) : "9999-12-31";
        String sql = 
            "SELECT " +
            "    p.name || ' ' || p.surname AS professionalName, " +
            "    p.email AS professionalEmail, " +
            "    i.inscription_date AS registrationDate, " +
            "    i.applied_fee AS fee, " +
            "    i.state AS state " +
            "FROM Inscription i " +
            "JOIN Professional p ON i.professional_id = p.professional_id " +
            "WHERE i.action_id = ? AND date(i.inscription_date) <= date(?) " +
            "ORDER BY i.inscription_date DESC";
        
        return db.executeQueryPojo(FARegistrationDTO.class, sql, actionId, safeDate);
    }
}
