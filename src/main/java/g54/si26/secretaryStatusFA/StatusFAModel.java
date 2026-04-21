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
        String sql = 
            "SELECT " +
            "    fa.action_id AS actionId, " +
            "    fa.name AS name, " +
            "    fa.status AS status, " +
            "    fa.inscriptionPeriodStart AS inscriptionPeriodStart, " +
            "    fa.inscriptionPeriodEnd AS inscriptionPeriodEnd, " +
            "    fa.startDate AS startDate, " +
            "    fa.endDate AS endDate, " +
            "    fa.spots AS totalSpots, " +
            "    (SELECT COUNT(*) FROM Inscription i WHERE i.action_id = fa.action_id AND i.state = 'CONFIRMED') AS confirmedPlaces " +
            "FROM FormativeAction fa " +
            "ORDER BY fa.startDate DESC";
        
        return db.executeQueryPojo(FAStatusDTO.class, sql);
    }

    /**
     * Retrieves detailed financial metrics for a specific formative action.
     */
    public FAStatusDTO getFADetail(int actionId) {
        // We get the basic data first
        String sqlBasic = 
            "SELECT " +
            "    fa.action_id AS actionId, " +
            "    fa.name AS name, " +
            "    fa.status AS status, " +
            "    fa.inscriptionPeriodStart AS inscriptionPeriodStart, " +
            "    fa.inscriptionPeriodEnd AS inscriptionPeriodEnd, " +
            "    fa.startDate AS startDate, " +
            "    fa.endDate AS endDate, " +
            "    fa.spots AS totalSpots, " +
            "    (SELECT COUNT(*) FROM Inscription i WHERE i.action_id = fa.action_id AND i.state = 'CONFIRMED') AS confirmedPlaces " +
            "FROM FormativeAction fa " +
            "WHERE fa.action_id = ?";
        
        List<FAStatusDTO> results = db.executeQueryPojo(FAStatusDTO.class, sqlBasic, actionId);
        if (results.isEmpty()) return null;
        
        FAStatusDTO dto = results.get(0);
        
        // Confirmed Income: Sum of actual verified payments made by the enrolled professionals
        String sqlIncome = "SELECT COALESCE(SUM(mm.amount), 0.0) " +
                           "FROM MoneyMovement mm " +
                           "JOIN Inscription i ON mm.inscription_id = i.inscription_id " +
                           "WHERE i.action_id = ? AND mm.status = 'EXECUTED'";
        double confirmedIncome = (double) db.executeQueryArray(sqlIncome, actionId).get(0)[0];
        dto.setConfirmedIncome(confirmedIncome);
        
        // Confirmed Expenses: Sum of actual money movements (payments) already transferred to the teachers
        String sqlConfExp = "SELECT COALESCE(SUM(ABS(mm.amount)), 0.0) " +
                            "FROM MoneyMovement mm " +
                            "JOIN Invoice inv ON mm.invoice_id = inv.invoice_id " +
                            "WHERE inv.action_id = ? AND mm.status = 'EXECUTED'";
        double confirmedExpenses = (double) db.executeQueryArray(sqlConfExp, actionId).get(0)[0];
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
    public List<FARegistrationDTO> getFARegistrations(int actionId) {
        String sql = 
            "SELECT " +
            "    p.name || ' ' || p.surname AS professionalName, " +
            "    p.email AS professionalEmail, " +
            "    i.inscription_date AS registrationDate, " +
            "    i.applied_fee AS fee, " +
            "    i.state AS state " +
            "FROM Inscription i " +
            "JOIN Professional p ON i.professional_id = p.professional_id " +
            "WHERE i.action_id = ? " +
            "ORDER BY i.inscription_date DESC";
        
        return db.executeQueryPojo(FARegistrationDTO.class, sql, actionId);
    }
}
