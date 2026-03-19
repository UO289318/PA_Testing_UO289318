package g54.si26.DTOs;

/* Select the Professional (no filter):
"SELECT professional_id AS professionalId, name, surname, "
+ "phone, email FROM Professional"


* Select the professional (Filter example)
"SELECT professional_id AS professionalId, name, surname, phone, email "
+ "FROM Professional "
+ "WHERE email = ?"
*/
public class ProfessionalDTO {
	private int professionalId;
	private String name;
	private String surname;
	private String phone;
	private String email;
	private Integer communityId;
	public ProfessionalDTO() {}

	public ProfessionalDTO(int professionalId, String name, String surname, String phone, String email) {
		this.professionalId = professionalId;
		this.name = name;
		this.surname = surname;
		this.phone = phone;
		this.email = email;
	}

	public int getProfessionalId() {
		return professionalId;
	}

	public void setProfessionalId(int professionalId) {
		this.professionalId = professionalId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getCommunityId() {
		return communityId;
	}

	public void setCommunityId(Integer communityId) {
		this.communityId = communityId;
	}

}
