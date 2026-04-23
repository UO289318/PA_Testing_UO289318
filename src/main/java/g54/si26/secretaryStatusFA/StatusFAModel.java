package g54.si26.secretaryStatusFA;

import g54.si26.secretaryStatusFA.dto.FARegistrationDTO;
import g54.si26.secretaryStatusFA.dto.FAStatusDTO;
import g54.si26.utils.Database;
import java.util.List;

public class StatusFAModel {

    private final Database db = new Database();

    /**
     * Retrieves the list of formative actions with basic status information.
     * Uses a simplified version of the status logic found in the project.
     */
    public List<FAStatusDTO> getFormativeActions(String simulatedDate) {
    	String safeDate = (simulatedDate != null && !simulatedDate.isBlank()) ? simulatedDate.substring(0, 10) : "9999-12-31";
        String sql = 
            "SELECT " +
            "    fa.action_id AS actionId, " +
            "    fa.name AS name, " +
            "    CASE " +
            "      WHEN fa.closureDate IS NOT NULL AND date('" + safeDate + "') >= date(fa.closureDate) " +
            "           AND (fa.reopenDate IS NULL OR date('" + safeDate + "') < date(fa.reopenDate)) THEN 'CLOSED' " +
            "      WHEN (fa.cancelDate IS NOT NULL AND date('" + safeDate + "') >= date(fa.cancelDate) AND (fa.reopenDate IS NULL OR date('" + safeDate + "') < date(fa.reopenDate))) " +
            "           OR (fa.status = 'CANCELLED' AND fa.cancelDate IS NULL) THEN 'Cancelled' " +
            "      WHEN date('" + safeDate + "') > date(fa.endDate) THEN 'Finished' " +
            "      WHEN date('" + safeDate + "') >= date(fa.startDate) AND date('" + safeDate + "') <= date(fa.endDate) THEN 'In progress' " +
            "      WHEN date('" + safeDate + "') >= date(fa.inscriptionPeriodStart) AND date('" + safeDate + "') <= date(fa.inscriptionPeriodEnd) THEN 'Enrolment open' " +
            "      WHEN date('" + safeDate + "') < date(fa.startDate) THEN 'Upcoming' " +
            "      ELSE fa.status " +
            "    END AS status, " +
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
        String sqlBasic = 
            "SELECT " +
            "    fa.action_id AS actionId, " +
            "    fa.name AS name, " +
            "    CASE " +
            "      WHEN fa.closureDate IS NOT NULL AND date('" + safeDate + "') >= date(fa.closureDate) " +
            "           AND (fa.reopenDate IS NULL OR date('" + safeDate + "') < date(fa.reopenDate)) THEN 'CLOSED' " +
            "      WHEN (fa.cancelDate IS NOT NULL AND date('" + safeDate + "') >= date(fa.cancelDate) AND (fa.reopenDate IS NULL OR date('" + safeDate + "') < date(fa.reopenDate))) " +
            "           OR (fa.status = 'CANCELLED' AND fa.cancelDate IS NULL) THEN 'Cancelled' " +
            "      WHEN date('" + safeDate + "') > date(fa.endDate) THEN 'Finished' " +
            "      WHEN date('" + safeDate + "') >= date(fa.startDate) AND date('" + safeDate + "') <= date(fa.endDate) THEN 'In progress' " +
            "      WHEN date('" + safeDate + "') >= date(fa.inscriptionPeriodStart) AND date('" + safeDate + "') <= date(fa.inscriptionPeriodEnd) THEN 'Enrolment open' " +
            "      WHEN date('" + safeDate + "') < date(fa.startDate) THEN 'Upcoming' " +
            "      ELSE fa.status " +
            "    END AS status, " +
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
        
        // Estimated Income: Sum of all applied fees for professionals who didn't cancel their inscription
        String sqlExpected = "SELECT COALESCE(SUM(applied_fee), 0.0) FROM Inscription WHERE action_id = ? AND state != 'CANCELLED' AND date(inscription_date) <= date(?)";
        double estimatedIncome = (double) db.executeQueryArray(sqlExpected, actionId, safeDate).get(0)[0];
        dto.setEstimatedIncome(estimatedIncome);

        // Confirmed Income: Sum of actual verified payments made by the enrolled professionals
        String sqlIncome = "SELECT COALESCE(SUM(mm.amount), 0.0) " +
                           "FROM MoneyMovement mm " +
                           "JOIN Inscription i ON mm.inscription_id = i.inscription_id " +
                           "WHERE i.action_id = ? AND mm.status = 'EXECUTED' AND date(mm.movement_date) <= date(?)";
        double confirmedIncome = (double) db.executeQueryArray(sqlIncome, actionId, safeDate).get(0)[0];
        dto.setConfirmedIncome(confirmedIncome);
        
        // Confirmed Expenses: Sum of actual money movements (payments) already transferred to the teachers
        String sqlConfExp = "SELECT COALESCE(SUM(ABS(mm.amount)), 0.0) " +
                            "FROM MoneyMovement mm " +
                            "JOIN Invoice inv ON mm.invoice_id = inv.invoice_id " +
                            "WHERE inv.action_id = ? AND mm.status = 'EXECUTED' AND date(mm.movement_date) <= date(?)";
        double confirmedExpenses = (double) db.executeQueryArray(sqlConfExp, actionId, safeDate).get(0)[0];
        dto.setConfirmedExpenses(confirmedExpenses);
        
        // Total Promised Remuneration
        String sqlTotalRem = "SELECT COALESCE(SUM(remuneration), 0.0) " +
                             "FROM Teacher_FormativeAction " +
                             "WHERE action_id = ?";
        double totalRemuneration = (double) db.executeQueryArray(sqlTotalRem, actionId).get(0)[0];
        dto.setTotalRemuneration(totalRemuneration);
        
        // Estimated Expenses: (Total promised remuneration) - (Confirmed Expenses)
        dto.setEstimatedExpenses(totalRemuneration - confirmedExpenses);
        
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
