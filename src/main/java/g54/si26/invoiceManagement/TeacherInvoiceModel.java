package g54.si26.invoiceManagement;

import java.util.ArrayList;
import java.util.List;
import g54.si26.DTOs.TeacherInvoiceDTO;
import g54.si26.utils.Database;

public class TeacherInvoiceModel {
    private Database db = new Database();

    public List<TeacherInvoiceDTO> getPendingInvoicesList() {
        String sql = 
            "SELECT t.name, fa.name, tfa.remuneration " +
            "FROM Teacher t " +
            "JOIN Teacher_FormativeAction tfa ON t.teacher_id = tfa.teacher_id " +
            "JOIN FormativeAction fa ON tfa.action_id = fa.action_id " +
            "WHERE tfa.status IS NULL OR tfa.status != 'INVOICED'";

        List<Object[]> rows = db.executeQueryArray(sql);
        List<TeacherInvoiceDTO> list = new ArrayList<>();
        
        for (Object[] row : rows) {
            TeacherInvoiceDTO dto = new TeacherInvoiceDTO();
            dto.setTeacherName(row[0].toString());
            dto.setCourseName(row[1].toString());
            dto.setTotalAmount(Double.parseDouble(row[2].toString())); 
            list.add(dto);
        }
        return list;
    }

    public int[] getIds(String teacherName, String courseName) {
        String sql = "SELECT t.teacher_id, fa.action_id " +
                     "FROM Teacher t " +
                     "JOIN Teacher_FormativeAction tfa ON t.teacher_id = tfa.teacher_id " +
                     "JOIN FormativeAction fa ON tfa.action_id = fa.action_id " +
                     "WHERE t.name = ? AND fa.name = ?";
        List<Object[]> res = db.executeQueryArray(sql, teacherName, courseName);
        if (!res.isEmpty()) {
            return new int[]{
                Integer.parseInt(res.get(0)[0].toString()), 
                Integer.parseInt(res.get(0)[1].toString())
            };
        }
        return new int[]{-1, -1};
    }

    public void registerInvoice(int teacherId, int actionId, String date, double net, double vatAmount, double total) {
        String sqlInvoice = 
            "INSERT INTO Invoice (invoice_date, netAmount, vat, totalAmount, status, teacher_id, action_id) " +
            "VALUES (?, ?, ?, ?, 'RECEIVED', ?, ?)";
        db.executeUpdate(sqlInvoice, date, net, vatAmount, total, teacherId, actionId);

        String sqlUpdateStatus = "UPDATE Teacher_FormativeAction SET status = 'INVOICED' WHERE teacher_id = ? AND action_id = ?";
        db.executeUpdate(sqlUpdateStatus, teacherId, actionId);
    }

    public void updateCommitment(int teacherId, int actionId, double newNetAmount) {
        String sql = "UPDATE Teacher_FormativeAction SET remuneration = ? WHERE teacher_id = ? AND action_id = ?";
        db.executeUpdate(sql, newNetAmount, teacherId, actionId);
    }
}