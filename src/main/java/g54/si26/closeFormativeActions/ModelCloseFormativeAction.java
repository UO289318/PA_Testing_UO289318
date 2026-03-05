package g54.si26.closeFormativeActions; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import g54.si26.DTOs.*;
import g54.si26.utils.Database;
import g54.si26.utils.Util;

public class ModelCloseFormativeAction {

    private Database db = new Database();

    //Loads the table taking into account the current date.
    public List<FormativeActionDTO> getUnclosedCourses(Date simulatedDate){
        	List<FormativeActionDTO> courses = new ArrayList<>();
        	String simulatedDateStr = Util.dateToIsoString(simulatedDate) + " 23:59:59";
        
        	//SQL takig into account the current date
        	String sql = "SELECT fa.action_id AS actionId, fa.name, fa.startDate, fa.status, " +
                     "date(fa.startDate, '+1 day') AS calculatedEndDate, " +
                     "(SELECT count(*) FROM Inscription i WHERE i.action_id = fa.action_id AND i.state IN ('RECEIVED', 'CANCELLED') AND i.inscription_date <= ?) AS unhandledCount, " +
                     "(SELECT inv.status FROM Invoice inv WHERE inv.action_id = fa.action_id AND inv.invoice_date <= ? LIMIT 1) AS invoiceStatus " +
                     "FROM FormativeAction fa " +
                     "WHERE fa.status != 'CLOSED' OR ? < date(fa.startDate, '+1 day')";

        	try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
             
        		pstmt.setString(1, simulatedDateStr); 
        		pstmt.setString(2, simulatedDateStr); 
        		pstmt.setString(3, simulatedDateStr); 
            
        		try(ResultSet rs = pstmt.executeQuery()) {
        			while (rs.next()){
        				//Loads data
        				FormativeActionDTO dto = new FormativeActionDTO();
        				dto.setActionId(rs.getInt("actionId"));
        				dto.setName(rs.getString("name"));
        				dto.setStartDate(rs.getString("startDate"));
                    
        				String dbStatus = rs.getString("status");
        				String calcEndDateStr = rs.getString("calculatedEndDate");
        				Date endDate = Util.isoStringToDate(calcEndDateStr);
                    
        				if("CLOSED".equals(dbStatus) && endDate != null && simulatedDate.before(endDate)) 
        					dto.setStatus("ACTIVE");
        				else 
        					dto.setStatus(dbStatus);
                    
        				dto.setUnhandledRegistrations(rs.getInt("unhandledCount"));
                    
        				String invStatus = rs.getString("invoiceStatus");
        				dto.setTeacherInvoicesStatus(invStatus != null ? invStatus : "NO INVOICE");
        				courses.add(dto);
        			}
        		}
        	} catch (SQLException e){
        		e.printStackTrace();
        	}
        	return courses;
    	}

    
    //Validation of the course: 1 Blocked and 3 Warning scenarios.
    	public CloseValidationDTO validateClosure(int actionId, Date simulatedDate){
    		CloseValidationDTO result = new CloseValidationDTO();
    		String simulatedDateStr = Util.dateToIsoString(simulatedDate) + " 23:59:59";

    		try (Connection conn = db.getConnection()){
    			// BLOCKED If it's before the endDate
    			String sqlDate = "SELECT date(startDate, '+1 day') AS calculatedEndDate FROM FormativeAction WHERE action_id = ?";
    			try (PreparedStatement pstmt = conn.prepareStatement(sqlDate)){
    				pstmt.setInt(1, actionId);
    				ResultSet rs = pstmt.executeQuery();
    				if(rs.next()){
    					String calcDateStr = rs.getString("calculatedEndDate");
                    
    					if(calcDateStr == null)
    						result.addError("CRITICAL: This course has no valid Start Date in the database.");
    					else{
    						Date endDate = Util.isoStringToDate(calcDateStr);
    						if(endDate != null && simulatedDate.before(endDate))
    							result.addError("The course cannot be closed before its end date (" + calcDateStr + ").");
    						if(endDate == null)
    							result.addError("CRITICAL: The course dates are corrupted.");
    					}
    				}
    			}

    			// WARNING 1: Registrations unhandled (Ignorando las del futuro)
    			String sqlReg = "SELECT count(*) AS unhandled FROM Inscription WHERE action_id = ? AND state IN ('RECEIVED', 'CANCELLED') AND inscription_date <= ?";
    			try (PreparedStatement pstmt = conn.prepareStatement(sqlReg)){
    				pstmt.setInt(1, actionId);
    				pstmt.setString(2, simulatedDateStr);
    				ResultSet rs = pstmt.executeQuery();
    				if(rs.next() && rs.getInt("unhandled") > 0)
    					result.addWarning("There are unhandled professional registrations (RECEIVED or CANCELLED state).");
    			}

            // Invoice section
            // Check if there exists any invoice sent until the simulated date
    			String sqlTotalInv = "SELECT count(*) AS total_inv FROM Invoice WHERE action_id = ? AND invoice_date <= ?";
    			boolean invoiceExists = false;
            
    			try (PreparedStatement pstmtTotal = conn.prepareStatement(sqlTotalInv)){
    				pstmtTotal.setInt(1, actionId);
    				pstmtTotal.setString(2, simulatedDateStr);
    				ResultSet rsTotal = pstmtTotal.executeQuery();
    				if(rsTotal.next() && rsTotal.getInt("total_inv") > 0)
    					invoiceExists = true;
            }

    			if(!invoiceExists)
    				// WARNING 2: No invoice has been sent (hasta la fecha simulada)
    				result.addWarning("The Invoice has not been sent to the Teacher (Not Generated).");
    			else{
    				//WARNING 3: The invoice is sent but pending (hasta la fecha simulada)
    				String sqlNotPaid = "SELECT count(*) AS pending_count FROM Invoice WHERE action_id = ? AND status = 'PENDING' AND invoice_date <= ?";
    				try (PreparedStatement pstmtPending = conn.prepareStatement(sqlNotPaid)){
    					pstmtPending.setInt(1, actionId);
    					pstmtPending.setString(2, simulatedDateStr);
    					ResultSet rsPending = pstmtPending.executeQuery();
    					if(rsPending.next() && rsPending.getInt("pending_count") > 0)
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
    		try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
    			pstmt.setInt(1, actionId);
    			return pstmt.executeUpdate() > 0;
    		} catch (SQLException e){
    			e.printStackTrace();
    			return false;
    		}
    	}
}