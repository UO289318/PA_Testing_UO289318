package g54.si26.cancelFormativeActions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.DTOs.TeacherDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;
import g54.si26.utils.Util;

public class ModelCancelFormativeAction {
	
	private Database db = new Database();
	
	public List<FormativeActionDTO> getCancelableActions(Date simulatedDate) {
		String simulatedDateStr = Util.dateToIsoString(simulatedDate);
		String statusSql = getTemporalFaStatusSql(simulatedDateStr, "FormativeAction");
		// Allow cancellation if not closed, even if already cancelled (to allow fixing errors)
		String sql = "SELECT * FROM (" +
                "  SELECT action_id AS actionId, name, startDate, endDate, " +
                "  (" + statusSql + ") AS status " +
                "  FROM FormativeAction " +
                ") WHERE status != 'CLOSED' " +
                "AND status != 'Cancelled' " + 
                "AND endDate >= ? " +
                "ORDER BY startDate ASC";
		
		return db.executeQueryPojo(FormativeActionDTO.class, sql, simulatedDateStr);
	}

	public List<Object[]> getTeachersWithRemuneration(int actionId) {
		String sql = "SELECT t.teacher_id, t.name, tfa.remuneration " +
					 "FROM Teacher t " +
					 "JOIN Teacher_FormativeAction tfa ON t.teacher_id = tfa.teacher_id " +
					 "WHERE tfa.action_id = ?";
		return db.executeQueryArray(sql, actionId);
	}

	public void cancelAction(int actionId, double courseCompletionPct, Map<Integer, Double> teacherCompletionPcts, Date simulatedDate) {
		String simulatedDateStr = Util.dateToIsoString(simulatedDate);
		try (Connection conn = db.getConnection()) {
			conn.setAutoCommit(false);
			try {
				// 1. Update FormativeAction status
				try (PreparedStatement pstmt = conn.prepareStatement("UPDATE FormativeAction SET status = 'CANCELLED' WHERE action_id = ?")) {
					pstmt.setInt(1, actionId);
					pstmt.executeUpdate();
				}

				// 2. Update Inscriptions: Recalculate based on Fee table to avoid compounding
				String sqlUpdateInscriptions = 
					"UPDATE Inscription " +
					"SET applied_fee = (SELECT f.amount FROM Fee f " +
					"                  JOIN Professional p ON Inscription.professional_id = p.professional_id " +
					"                  WHERE f.action_id = Inscription.action_id " +
					"                  AND f.community_id = p.community_id) * (? / 100.0) " +
					"WHERE action_id = ?";
				try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateInscriptions)) {
					pstmt.setDouble(1, courseCompletionPct);
					pstmt.setInt(2, actionId);
					pstmt.executeUpdate();
				}

				updateInscriptionStatesAfterCancellation(conn, actionId);

				// 3. Update Teachers Remuneration and Invoices
				for (Map.Entry<Integer, Double> entry : teacherCompletionPcts.entrySet()) {
					int teacherId = entry.getKey();
					double teacherPct = entry.getValue();

					// WARNING: Since we can't touch DB, if we re-cancel, we are using the CURRENT remuneration as base.
					// The UI will show the current value so the Training Manager knows what's happening.
					double currentRem = 0;
					try (PreparedStatement pstmt = conn.prepareStatement("SELECT remuneration FROM Teacher_FormativeAction WHERE action_id = ? AND teacher_id = ?")) {
						pstmt.setInt(1, actionId);
						pstmt.setInt(2, teacherId);
						try (ResultSet rs = pstmt.executeQuery()) {
							if (rs.next()) currentRem = rs.getDouble("remuneration");
						}
					}

					double newRem = currentRem * (teacherPct / 100.0);
					double netAmount = newRem / 1.21;
					double vat = newRem - netAmount;

					try (PreparedStatement pstmt = conn.prepareStatement("UPDATE Teacher_FormativeAction SET remuneration = ? WHERE action_id = ? AND teacher_id = ?")) {
						pstmt.setDouble(1, newRem);
						pstmt.setInt(2, actionId);
						pstmt.setInt(3, teacherId);
						pstmt.executeUpdate();
					}

					// Invoice management
					int invoiceId = -1;
					try (PreparedStatement pstmt = conn.prepareStatement("SELECT invoice_id FROM Invoice WHERE action_id = ? AND teacher_id = ?")) {
						pstmt.setInt(1, actionId);
						pstmt.setInt(2, teacherId);
						try (ResultSet rs = pstmt.executeQuery()) {
							if (rs.next()) invoiceId = rs.getInt("invoice_id");
						}
					}

					if (invoiceId != -1) {
						try (PreparedStatement pstmt = conn.prepareStatement("UPDATE Invoice SET totalAmount = ?, netAmount = ?, vat = ? WHERE invoice_id = ?")) {
							pstmt.setDouble(1, newRem);
							pstmt.setDouble(2, netAmount);
							pstmt.setDouble(3, vat);
							pstmt.setInt(4, invoiceId);
							pstmt.executeUpdate();
						}
					} else {
						try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Invoice (invoice_date, netAmount, vat, totalAmount, status, teacher_id, action_id) VALUES (?, ?, ?, ?, 'PENDING', ?, ?)")) {
							pstmt.setString(1, simulatedDateStr);
							pstmt.setDouble(2, netAmount);
							pstmt.setDouble(3, vat);
							pstmt.setDouble(4, newRem);
							pstmt.setInt(5, teacherId);
							pstmt.setInt(6, actionId);
							pstmt.executeUpdate();
						}
					}
					
					updateInvoiceStatusAfterCancellation(conn, actionId, teacherId);
				}

				conn.commit();
			} catch (SQLException e) {
				conn.rollback();
				throw new ApplicationException("Error: " + e.getMessage());
			}
		} catch (SQLException e) {
			throw new ApplicationException("DB Error: " + e.getMessage());
		}
	}

	private void updateInscriptionStatesAfterCancellation(Connection conn, int actionId) throws SQLException {
		String sqlSelect = "SELECT inscription_id, applied_fee AS fee, " +
						   "(SELECT COALESCE(SUM(amount), 0) FROM MoneyMovement WHERE inscription_id = i.inscription_id) AS netBalance " +
						   "FROM Inscription i WHERE i.action_id = ?";
		
		try (PreparedStatement pstmt = conn.prepareStatement(sqlSelect)) {
			pstmt.setInt(1, actionId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					int id = rs.getInt("inscription_id");
					double fee = rs.getDouble("fee");
					double netBalance = rs.getDouble("netBalance");
					
					String newState = (netBalance > fee + 0.001) ? "PENDING_COMPENSATION" : "CANCELLED";
					
					try (PreparedStatement pstmtUpd = conn.prepareStatement("UPDATE Inscription SET state = ? WHERE inscription_id = ?")) {
						pstmtUpd.setString(1, newState);
						pstmtUpd.setInt(2, id);
						pstmtUpd.executeUpdate();
					}
				}
			}
		}
	}

	private void updateInvoiceStatusAfterCancellation(Connection conn, int actionId, int teacherId) throws SQLException {
		String sqlSelect = "SELECT invoice_id, totalAmount, " +
						   "(SELECT COALESCE(SUM(amount), 0) FROM MoneyMovement WHERE invoice_id = i.invoice_id) AS netBalance " +
						   "FROM Invoice i WHERE i.action_id = ? AND i.teacher_id = ?";
		
		try (PreparedStatement pstmt = conn.prepareStatement(sqlSelect)) {
			pstmt.setInt(1, actionId);
			pstmt.setInt(2, teacherId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					int id = rs.getInt("invoice_id");
					double total = rs.getDouble("totalAmount");
					double netBalance = rs.getDouble("netBalance");
					String newStatus = (Math.abs(netBalance) >= total - 0.001) ? "PAID" : "PENDING";
					try (PreparedStatement pstmtUpd = conn.prepareStatement("UPDATE Invoice SET status = ? WHERE invoice_id = ?")) {
						pstmtUpd.setString(1, newStatus);
						pstmtUpd.setInt(2, id);
						pstmtUpd.executeUpdate();
					}
				}
			}
		}
	}
	private String getTemporalFaStatusSql(String simDate, String tableAlias) {
        String safeDate = (simDate!=null && !simDate.trim().isEmpty()) ? simDate.substring(0, 10) : "9999-12-31";
        return "CASE " +
               "  WHEN " + tableAlias + ".status = 'CANCELLED' THEN 'Cancelled' " +
               "  WHEN " + tableAlias + ".status = 'CLOSED' AND " + tableAlias + ".closureDate IS NOT NULL AND date('" + safeDate + "') >= date(" + tableAlias + ".closureDate) THEN 'CLOSED' " +
               "  WHEN date('" + safeDate + "') > date(" + tableAlias + ".endDate) THEN 'Finished' " +
               "  WHEN date('" + safeDate + "') >= date(" + tableAlias + ".startDate) AND date('" + safeDate + "') <= date(" + tableAlias + ".endDate) THEN 'In progress' " +
               "  WHEN date('" + safeDate + "') >= date(" + tableAlias + ".inscriptionPeriodStart) AND date('" + safeDate + "') <= date(" + tableAlias + ".inscriptionPeriodEnd) THEN 'Enrolment open' " +
               "  WHEN date('" + safeDate + "') < date(" + tableAlias + ".startDate) THEN 'Upcoming' " +
               "  ELSE " + tableAlias + ".status " +
               "END";
    }
}
