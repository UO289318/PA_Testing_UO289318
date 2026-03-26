package g54.si26.DTOs;

/**
 * Data Transfer Object for the Secretary's Pending Payments Grid.
 * Represents an enrollment and its associated course and professional details.
 */
public class EnrollmentRecordDTO {
    private int inscriptionId;
    private String courseName;
    private String professionalName;
    private String professionalEmail;
    private double fee;
    private String registrationDate;
    private double totalPaid;
    private double netBalance;
    private String state;

    public EnrollmentRecordDTO() {}

    // Getters and Setters
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getInscriptionId() { return inscriptionId; }
    public void setInscriptionId(int inscriptionId) { this.inscriptionId = inscriptionId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getProfessionalName() { return professionalName; }
    public void setProfessionalName(String professionalName) { this.professionalName = professionalName; }

    public String getProfessionalEmail() { return professionalEmail; }
    public void setProfessionalEmail(String professionalEmail) { this.professionalEmail = professionalEmail; }

    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

    public double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }

    public double getNetBalance() { return netBalance; }
    public void setNetBalance(double netBalance) { this.netBalance = netBalance; }
}
