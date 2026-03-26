package g54.si26.DTOs;

public class InscriptionSummaryDTO {
    private int inscriptionId;
    private String professionalName;
    private String courseName;
    private double appliedFee;
    private double totalPaid;
    private double balance; // totalPaid - appliedFee
    private String state;

    public InscriptionSummaryDTO() {}

    public int getInscriptionId() { return inscriptionId; }
    public void setInscriptionId(int inscriptionId) { this.inscriptionId = inscriptionId; }

    public String getProfessionalName() { return professionalName; }
    public void setProfessionalName(String professionalName) { this.professionalName = professionalName; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public double getAppliedFee() { return appliedFee; }
    public void setAppliedFee(double appliedFee) { this.appliedFee = appliedFee; }

    public double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDisplayStatus() {
        if (totalPaid < appliedFee - 0.001) return "Underpaid";
        if (Math.abs(totalPaid - appliedFee) < 0.001) return "Paid";
        return "Overpaid";
    }
}
