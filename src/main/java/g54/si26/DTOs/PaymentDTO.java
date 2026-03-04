package g54.si26.DTOs;

/**
 * Data Transfer Object for the Payment table.
 */
public class PaymentDTO {
    private int paymentId;
    private double amountPaid;
    private int inscriptionId;
    private String paymentDate;

    public PaymentDTO() {}

    // Getters and Setters
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }

    public int getInscriptionId() { return inscriptionId; }
    public void setInscriptionId(int inscriptionId) { this.inscriptionId = inscriptionId; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
}
