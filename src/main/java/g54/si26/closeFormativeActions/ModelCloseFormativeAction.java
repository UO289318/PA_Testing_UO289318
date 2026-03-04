package g54.si26.closeFormativeActions; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import g54.si26.DTOs.CloseValidationDTO;
import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.utils.Database;
import g54.si26.utils.Util;

public class ModelCloseFormativeAction {

    private Database db = new Database();

    public List<FormativeActionDTO> getUnclosedCourses(){
        List<FormativeActionDTO> courses = new ArrayList<>();
        
        // Check for Formative Actinos not closed
        String sql = "SELECT fa.action_id AS actionId, fa.name, fa.startDate, fa.status, " +
                     "(SELECT count(*) FROM Inscription i WHERE i.action_id = fa.action_id AND i.state = 'RECEIVED') AS unhandledCount, " +
                     "(SELECT inv.status FROM Invoice inv WHERE inv.action_id = fa.action_id LIMIT 1) AS invoiceStatus " +
                     "FROM FormativeAction fa WHERE fa.status != 'CLOSED'";

        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()){

            while (rs.next()){
                FormativeActionDTO dto = new FormativeActionDTO();
                dto.setActionId(rs.getInt("actionId"));
                dto.setName(rs.getString("name"));
                dto.setStartDate(rs.getString("startDate"));
                dto.setStatus(rs.getString("status"));
                
                dto.setUnhandledRegistrations(rs.getInt("unhandledCount"));
                
                // Pass the invoice status (no invoice if it doe not exist).
                String invStatus = rs.getString("invoiceStatus");
                dto.setTeacherInvoicesStatus(invStatus != null ? invStatus : "NO INVOICE");

                courses.add(dto);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return courses;
    }

    

    public CloseValidationDTO validateClosure(int actionId, Date simulatedDate){
        CloseValidationDTO result = new CloseValidationDTO();

        try (Connection conn = db.getConnection()){
            
        	// BLOCKED If it's before the endDate
            String sqlDate = "SELECT date(startDate, '+1 day') AS calculatedEndDate FROM FormativeAction WHERE action_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDate)){
                pstmt.setInt(1, actionId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()){
                    String calcDateStr = rs.getString("calculatedEndDate");
                    
                    // For the null date
                    if (calcDateStr == null)
                         result.addError("CRITICAL: This course has no valid Start Date in the database.");
                    else {
                         Date endDate = Util.isoStringToDate(calcDateStr);
                         // Fix from tests
                         if (endDate != null && simulatedDate.before(endDate))
                             result.addError("The course cannot be closed before its end date (" + calcDateStr + ").");
                         else if (endDate == null)
                             result.addError("CRITICAL: The course dates are corrupted.");
                         
                    }
                }
            }

            // WARNING 1: Registrations unhandled (Unhandled Professionals)
            String sqlReg = "SELECT count(*) AS unhandled FROM Inscription WHERE action_id = ? AND state = 'RECEIVED'";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlReg)){
                pstmt.setInt(1, actionId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt("unhandled") > 0){
                    result.addWarning("There are unhandled professional registrations (RECEIVED state).");
                }
            }

            // Invoice section
            
            // Check if there exists any invoice count(*)
            String sqlTotalInv = "SELECT count(*) AS total_inv FROM Invoice WHERE action_id = ?";
            boolean invoiceExists = false;
            
            try (PreparedStatement pstmtTotal = conn.prepareStatement(sqlTotalInv)){
                pstmtTotal.setInt(1, actionId);
                ResultSet rsTotal = pstmtTotal.executeQuery();
                if (rsTotal.next() && rsTotal.getInt("total_inv") > 0){
                    invoiceExists = true;
                }
            }

            if (!invoiceExists)
                // WARNING 2: No invoice has been sent
                result.addWarning("The Invoice has not been sent to the Teacher (Not Generated).");
            else{
                // WARNING 3: The invoice is sent but pending
                String sqlNotPaid = "SELECT count(*) AS pending_count FROM Invoice WHERE action_id = ? AND status = 'PENDING'";
                try (PreparedStatement pstmtPending = conn.prepareStatement(sqlNotPaid)){
                    pstmtPending.setInt(1, actionId);
                    ResultSet rsPending = pstmtPending.executeQuery();
                    if (rsPending.next() && rsPending.getInt("pending_count") > 0)
                        result.addWarning("The Invoice has been generated/sent but is NOT PAID yet (PENDING state).");
                    
                }
            }

        } catch (SQLException e){
            e.printStackTrace();
            result.addError("Database connection error: " + e.getMessage());
        }

        return result;
    }

    public boolean executeClosure(int actionId){
        String sql = "UPDATE FormativeAction SET status = 'CLOSED' WHERE action_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, actionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
}