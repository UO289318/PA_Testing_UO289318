package g54.si26.financeConsulting;

import java.util.List;
import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.BaseModel;
import g54.si26.utils.Database;

public class FinancialConsultingModel extends BaseModel {

    // Gets the courses filtered by their status (All, Active, Not Active)
    public List<FormativeActionDTO> getFormativeActionsByStatus(String filter, String simDate) {
    		String statusSql = getTemporalFaStatusSql(simDate, "fa");
    		String sql = "SELECT * FROM (" +
    				" SELECT fa.action_id AS actionId, fa.name, (" + statusSql + ") AS status " +
    				"  FROM FormativeAction fa" + 
    				") WHERE 1=1";
        
    		if (filter!=null && !filter.equals("ALL") && !filter.equals("ACTIVE (Default)")) 
                sql += " AND status = '" + filter + "'";
    		else if ("ACTIVE (Default)".equals(filter))
    			sql += " AND status NOT IN ('CLOSED')";
            
        sql += " ORDER BY name ASC";
        
        return db.executeQueryPojo(FormativeActionDTO.class, sql);
    }

    public Object[] getCourseBasicData(int actionId, String simDate) {
    		String statusSql = getTemporalFaStatusSql(simDate, "fa");
        String sql = "SELECT fa.name, (" + statusSql + ") AS status,  fa.inscriptionPeriodStart, fa.inscriptionPeriodEnd, " +
                     "fa.startDate, fa.spots, " +
                     "(fa.spots - (SELECT COUNT(*) FROM Inscription i WHERE i.action_id = fa.action_id AND i.state IN ('RECEIVED', 'CONFIRMED'))) as freeSpots " +
                     "FROM FormativeAction fa WHERE fa.action_id = ?";
        
        List<Object[]> rows = db.executeQueryArray(sql, actionId);
        if (rows.isEmpty()) {
            throw new ApplicationException("The Formative Action could not be found.");
        }
        return rows.get(0);
    }

    public List<Object[]> getMovements(int actionId) {
        String sql =
            // 1. INGRESOS (Pagos de profesionales/alumnos)
        		"SELECT mm.movement_date as date, " +
                "('Enrollment Payment - ' || p.name || ' ' || p.surname) as concept, " +
                "mm.amount as amount, " + // ABS para asegurar que devuelve el valor absoluto
                "1 as is_income " + 
                "FROM MoneyMovement mm " +
                "INNER JOIN Inscription i ON mm.inscription_id = i.inscription_id " +
                "INNER JOIN Professional p ON i.professional_id = p.professional_id " +
                "WHERE i.action_id = ? AND mm.type = 'PAYMENT' " + // <-- Filtro corregido aquí
            
            "UNION ALL " +
            
            // 2. GASTOS (Pagos realizados a profesores)
            "SELECT mm.movement_date as date, " +
            "('Teacher Payment - ' || t.name) as concept, " +
            "mm.amount as amount, " + // ABS para que no salga "- €-500" en la vista
            "0 as is_income " + 
            "FROM MoneyMovement mm " +
            "INNER JOIN Invoice inv ON mm.invoice_id = inv.invoice_id " +
            "INNER JOIN Teacher t ON inv.teacher_id = t.teacher_id " +
            "WHERE inv.action_id = ? AND mm.type = 'PAYMENT' " +
            
            "ORDER BY date ASC";

        return db.executeQueryArray(sql, actionId, actionId);
        }
        }