package g54.si26.DTOs;

public class MoneyMovementDTO {
    private int movementId;
    private double amount;
    private String movementDate;
    private String status;
    private String type;
    private Integer inscriptionId;
    private Integer invoiceId;
    private String relatedTo;

    public MoneyMovementDTO() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRelatedTo() { return relatedTo; }
    public void setRelatedTo(String relatedTo) { this.relatedTo = relatedTo; }

    public int getMovementId() { return movementId; }
    public void setMovementId(int movementId) { this.movementId = movementId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getMovementDate() { return movementDate; }
    public void setMovementDate(String movementDate) { this.movementDate = movementDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getInscriptionId() { return inscriptionId; }
    public void setInscriptionId(Integer inscriptionId) { this.inscriptionId = inscriptionId; }

    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }
}
