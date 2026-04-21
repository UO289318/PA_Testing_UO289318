package g54.si26.secretaryStatusFA.dto;

public class FARegistrationDTO {
    private String professionalName;
    private String professionalEmail;
    private String registrationDate;
    private double fee;
    private String state;

    public FARegistrationDTO() {}

    public String getProfessionalName() { return professionalName; }
    public void setProfessionalName(String professionalName) { this.professionalName = professionalName; }

    public String getProfessionalEmail() { return professionalEmail; }
    public void setProfessionalEmail(String professionalEmail) { this.professionalEmail = professionalEmail; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
