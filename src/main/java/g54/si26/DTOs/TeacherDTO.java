package g54.si26.DTOs;

/*
 * String sql = "SELECT "
 *     + "teacher_id AS teacherId, "
 *     + "name, fiscal_id AS fiscalId, email, phone "
 *     + "FROM Teacher";
 */
public class TeacherDTO {

    private int    teacherId;   
    private String name;        
    private String fiscalId;    
    private String email;       
    private String phone;       

    public TeacherDTO() {}


    public int getTeacherId() {
		return teacherId;
	}



	public void setTeacherId(int teacherId) {
		this.teacherId = teacherId;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getFiscalId() {
		return fiscalId;
	}



	public void setFiscalId(String fiscalId) {
		this.fiscalId = fiscalId;
	}



	public String getEmail() {
		return email;
	}



	public void setEmail(String email) {
		this.email = email;
	}



	public String getPhone() {
		return phone;
	}



	public void setPhone(String phone) {
		this.phone = phone;
	}



	/** Returns "name (email)" – useful for populating combo boxes in the UI. */
    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}