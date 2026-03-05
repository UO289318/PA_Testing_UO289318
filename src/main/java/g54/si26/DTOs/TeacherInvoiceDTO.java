package g54.si26.DTOs;

/**
 * DTO for teacher invoices pending payment.
 */
public class TeacherInvoiceDTO {
    private int invoiceId;
    private String teacherName;
    private String courseName;
    private double totalAmount;
    private String invoiceDate;
    private double amountPaid;

    public TeacherInvoiceDTO() {}

    // Getters and Setters
    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
    
    public double getPendingAmount() { return totalAmount - amountPaid; }
}
