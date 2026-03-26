package g54.si26.DTOs;

/**
 * Data Transfer Object for the Payment table.
 */
public class PaymentDTO {
    private int paymentId;
    private double amount;
    private String paymentDate;
    private String status;
    private String type;
    private Integer inscriptionId;
    private Integer invoiceId;

    public PaymentDTO() {}

    // Getters and Setters
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getInscriptionId() { return inscriptionId; }
    public void setInscriptionId(Integer inscriptionId) { this.inscriptionId = inscriptionId; }

    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }
}
