package g54.si26.cancelEnrollment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;
import g54.si26.utils.Util;

public class ModelCancelEnrollment {

    private Database db = new Database();

    public List<Object[]> getActiveEnrollments(int professionalId) {
        String sql = 
            "SELECT i.inscription_id, fa.name, fa.startDate, fa.endDate, i.applied_fee, i.state " +
            "FROM Inscription i " +
            "JOIN FormativeAction fa ON i.action_id = fa.action_id " +
            "WHERE i.professional_id = ? AND i.state = 'CONFIRMED' " +
            "ORDER BY fa.startDate ASC";
            
        return db.executeQueryArray(sql, professionalId);
    }

    public void cancelEnrollment(int inscriptionId, Date simulatedDate, double refundAmount, String reason) {
        String simulatedDateStr = Util.dateToIsoString(simulatedDate);
        
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false); // Iniciar Transacción
            try {
                // 1. Actualizar Inscription a CANCELLED
                try (PreparedStatement pstmt = conn.prepareStatement("UPDATE Inscription SET state = 'CANCELLED', cancellation_date = ? WHERE inscription_id = ?")) {
                    pstmt.setString(1, simulatedDateStr);
                    pstmt.setInt(2, inscriptionId);
                    pstmt.executeUpdate();
                }

                // 2. Si hay reembolso, insertar en MoneyMovement
                if (refundAmount > 0) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO MoneyMovement (movement_date, amount, status, type, inscription_id) VALUES (?, ?, 'CONFIRMED', 'REFUND', ?)")) {
                        pstmt.setString(1, simulatedDateStr);
                        pstmt.setDouble(2, -refundAmount); // Negativo como salida de caja
                        pstmt.setInt(3, inscriptionId);
                        pstmt.executeUpdate();
                    }
                }

                conn.commit(); // Confirmar Transacción
            } catch (SQLException e) {
                conn.rollback(); // Deshacer en caso de error
                throw new ApplicationException("Error applying cancellation logic: " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new ApplicationException("DB Error: " + e.getMessage());
        }
    }
}