package g54.si26.viewPendingPayments;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import g54.si26.DTOs.MoneyMovementDTO;
import g54.si26.utils.Database;

public class ViewPendingModel {
    private Database db = new Database();
    
    // Ruta a la base de datos (asegúrate de que el nombre coincide con el que crea tu app)
    private String dbUrl = "jdbc:sqlite:database.db";

    public List<MoneyMovementDTO> getPendingPayments(String typeFilter) {
        // Mapeamos exactamente a tu esquema: status, type, inscription_id, invoice_id
        String sql = "SELECT movement_id AS movementId, amount, movement_date AS movementDate, " +
                     "status, type AS relatedTo, inscription_id AS inscriptionId, invoice_id AS invoiceId " +
                     "FROM MoneyMovement " +
                     "WHERE status = 'PENDING'";
                     
        if (!"ALL".equals(typeFilter)) {
            sql += " AND type = '" + typeFilter + "'";
        }
        
        return db.executeQueryPojo(MoneyMovementDTO.class, sql);
    }

    public Object[] getPaymentDetailsForView(int paymentId) {
        // Consulta uniendo las tablas que me has pasado en el esquema
        String sql = 
            "SELECT p.name || ' ' || p.surname AS prof_name, mm.amount, fa.name AS fa_name, mm.type AS reason " +
            "FROM MoneyMovement mm " +
            "JOIN Inscription i ON mm.inscription_id = i.inscription_id " +
            "JOIN Professional p ON i.professional_id = p.professional_id " +
            "JOIN FormativeAction fa ON i.action_id = fa.action_id " +
            "WHERE mm.movement_id = ? " +
            "UNION " +
            "SELECT t.name AS prof_name, mm.amount, fa.name AS fa_name, mm.type AS reason " +
            "FROM MoneyMovement mm " +
            "JOIN Invoice inv ON mm.invoice_id = inv.invoice_id " +
            "JOIN Teacher t ON inv.teacher_id = t.teacher_id " +
            "JOIN FormativeAction fa ON inv.action_id = fa.action_id " +
            "WHERE mm.movement_id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, paymentId);
            pstmt.setInt(2, paymentId);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Object[]{
                    rs.getString("prof_name"),
                    rs.getDouble("amount"),
                    rs.getString("fa_name"),
                    rs.getString("reason")
                };
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo detalles del pago " + paymentId + ": " + e.getMessage());
        }
        return null; 
    }
}