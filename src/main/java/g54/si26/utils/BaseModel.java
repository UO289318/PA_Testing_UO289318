package g54.si26.utils;

public class BaseModel {
    protected Database db = new Database();

    public String getTemporalFaStatusSql(String simDate, String tableAlias){
        String safeDate = (simDate != null && !simDate.trim().isEmpty()) ? simDate.substring(0, 10) : "9999-12-31";
        return "CASE " +
               "  WHEN " + tableAlias + ".closureDate IS NOT NULL AND date('" + safeDate + "') >= date(" + tableAlias + ".closureDate) " +
               "       AND (" + tableAlias + ".reopenDate IS NULL OR date('" + safeDate + "') < date(" + tableAlias + ".reopenDate)) THEN 'CLOSED' " +
               "  WHEN (" + tableAlias + ".cancelDate IS NOT NULL AND date('" + safeDate + "') >= date(" + tableAlias + ".cancelDate) " +
               "       AND (" + tableAlias + ".reopenDate IS NULL OR date('" + safeDate + "') < date(" + tableAlias + ".reopenDate))) " +
               "       OR (" + tableAlias + ".status = 'CANCELLED' AND " + tableAlias + ".cancelDate IS NULL) THEN 'Cancelled' " + 
               "  WHEN date('" + safeDate + "') > date(" + tableAlias + ".endDate) THEN 'Finished' " +
               "  WHEN date('" + safeDate + "') >= date(" + tableAlias + ".startDate) AND date('" + safeDate + "') <= date(" + tableAlias + ".endDate) THEN 'In progress' " +
               "  WHEN date('" + safeDate + "') >= date(" + tableAlias + ".inscriptionPeriodStart) AND date('" + safeDate + "') <= date(" + tableAlias + ".inscriptionPeriodEnd) THEN 'Enrolment open' " +
               "  WHEN date('" + safeDate + "') < date(" + tableAlias + ".startDate) THEN 'Upcoming' " +
               "  ELSE " + tableAlias + ".status " +
               "END";
    }
}
