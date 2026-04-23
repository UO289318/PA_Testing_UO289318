package g54.si26.registerTeacher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import g54.si26.DTOs.TeacherDTO;
import g54.si26.utils.ApplicationException;
import g54.si26.utils.Database;

public class RegisterTeacherModel {

    private final Database db = new Database();

    public static class ValidationResult {
        public final List<String> errors = new ArrayList<>();
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }

    /**
     * Validates a teacher and returns all errors.
     */
    public ValidationResult validate(TeacherDTO teacher) {
        ValidationResult result = new ValidationResult();

        if (teacher.getName() == null || teacher.getName().trim().isEmpty()) {
            result.errors.add("Name must not be null, empty, or consist only of whitespace.");
        }
        if (teacher.getFiscalId() == null || teacher.getFiscalId().trim().isEmpty()) {
            result.errors.add("Fiscal ID must not be null or empty.");
        }
        if (teacher.getEmail() == null || teacher.getEmail().trim().isEmpty()) {
            result.errors.add("Email must not be null or empty.");
        } else if (!isValidEmail(teacher.getEmail())) {
            result.errors.add("Email must follow a valid format (e.g., user@domain.com).");
        }
        if (teacher.getPhone() == null || teacher.getPhone().trim().isEmpty()) {
            result.errors.add("Phone must not be null or empty.");
        }

        // Check duplicates only if basic fields are valid to avoid noise, 
        // but we can also check them here to show all errors.
        checkDuplicates(teacher, result);

        return result;
    }

    /**
     * Registers a new teacher in the database. 
     * Assumes validation has already been performed or performs it again.
     */
    public void registerTeacher(TeacherDTO teacher) {
        ValidationResult result = validate(teacher);
        if (result.hasErrors()) {
            throw new ApplicationException(String.join("\n", result.errors));
        }
        
        String sql = "INSERT INTO Teacher (name, fiscal_id, email, phone) VALUES (?, ?, ?, ?)";
        db.executeUpdate(sql, teacher.getName().trim(), teacher.getFiscalId().trim(), teacher.getEmail().trim(), teacher.getPhone().trim());
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        return pat.matcher(email).matches();
    }

    private void checkDuplicates(TeacherDTO teacher, ValidationResult result) {
        if (teacher.getFiscalId() != null && !teacher.getFiscalId().trim().isEmpty()) {
            String sqlFiscal = "SELECT COUNT(*) FROM Teacher WHERE fiscal_id = ?";
            List<Object[]> resFiscal = db.executeQueryArray(sqlFiscal, teacher.getFiscalId().trim());
            if (!resFiscal.isEmpty() && Integer.parseInt(resFiscal.get(0)[0].toString()) > 0) {
                result.errors.add("A teacher with this Fiscal ID already exists.");
            }
        }

        if (teacher.getEmail() != null && !teacher.getEmail().trim().isEmpty() && isValidEmail(teacher.getEmail())) {
            String sqlEmail = "SELECT COUNT(*) FROM Teacher WHERE email = ?";
            List<Object[]> resEmail = db.executeQueryArray(sqlEmail, teacher.getEmail().trim());
            if (!resEmail.isEmpty() && Integer.parseInt(resEmail.get(0)[0].toString()) > 0) {
                result.errors.add("A teacher with this Email already exists.");
            }
        }
        
        if (teacher.getPhone() != null && !teacher.getPhone().trim().isEmpty()) {
            String sqlPhone = "SELECT COUNT(*) FROM Teacher WHERE phone = ?";
            List<Object[]> resPhone = db.executeQueryArray(sqlPhone, teacher.getPhone().trim());
            if (!resPhone.isEmpty() && Integer.parseInt(resPhone.get(0)[0].toString()) > 0) {
                result.errors.add("A teacher with this Phone already exists.");
            }
        }
    }
}
