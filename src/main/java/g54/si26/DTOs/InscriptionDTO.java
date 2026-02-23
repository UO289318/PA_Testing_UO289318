package g54.si26.DTOs;

/*
 * Check for if a professional has an inscriptino
 "SELECT inscription_id AS inscriptionId "
+ "FROM Inscription "
+ "WHERE professional_id = ? AND action_id = ?"
 */
public class InscriptionDTO {

	private int inscriptionId;       // BD: inscription_id
	private String inscriptionDate;  // BD: inscription_date
	private double fee;              // BD: fee
	private String state;            // BD: state
	private int professionalId;      // BD: professional_id
	private int actionId;
	
	public InscriptionDTO() {}

	public int getInscriptionId() {
		return inscriptionId;
	}

	public void setInscriptionId(int inscriptionId) {
		this.inscriptionId = inscriptionId;
	}

	public String getInscriptionDate() {
		return inscriptionDate;
	}

	public void setInscriptionDate(String inscriptionDate) {
		this.inscriptionDate = inscriptionDate;
	}

	public double getFee() {
		return fee;
	}

	public void setFee(double fee) {
		this.fee = fee;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getProfessionalId() {
		return professionalId;
	}

	public void setProfessionalId(int professionalId) {
		this.professionalId = professionalId;
	}

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
	}
	
	
	
}
