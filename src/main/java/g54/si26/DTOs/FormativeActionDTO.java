package g54.si26.DTOs;

/* Pa consultas en el sql es necesario poner el alias dl profe y d la accion
 * Since action_id and teacher_id are foreign keys, they must be referred as using alias
String sql = "SELECT "
+ "action_id AS actionId, "   
+ "name, objectives, mainContents, spots, "
+ "startDate, endDate, numberOfHours, "
+ "inscriptionPeriodStart, inscriptionPeriodEnd, "
+ "location, fee, status, initialPayment, "
+ "teacher_id AS teacherId "  
+ "FROM FormativeAction";
*/



public class FormativeActionDTO {
	
	
	private int actionId;           
	private String name;            
	private String objectives;      
	private String mainContents;    
	private int spots;              
	private String startDate;       
	private String endDate;         
	private int numberOfHours;      
	private String inscriptionPeriodStart; 
	private String inscriptionPeriodEnd;   
	private String location;        
	private double fee;             
	private String status;          
	private double initialPayment;  
	private int teacherId;
	private int availableSpots;
	private int unhandledRegistrations;
	private String teacherInvoicesStatus;

	
	public FormativeActionDTO(){}


	public int getActionId() {
		return actionId;
	}


	public void setActionId(int actionId) {
		this.actionId = actionId;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getObjectives() {
		return objectives;
	}


	public void setObjectives(String objectives) {
		this.objectives = objectives;
	}


	public String getMainContents() {
		return mainContents;
	}


	public void setMainContents(String mainContents) {
		this.mainContents = mainContents;
	}


	public int getSpots() {
		return spots;
	}


	public void setSpots(int spots) {
		this.spots = spots;
	}


	public String getStartDate() {
		return startDate;
	}


	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}


	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}


	public int getNumberOfHours() {
		return numberOfHours;
	}


	public void setNumberOfHours(int numberOfHours) {
		this.numberOfHours = numberOfHours;
	}


	public String getInscriptionPeriodStart() {
		return inscriptionPeriodStart;
	}


	public void setInscriptionPeriodStart(String inscriptionPeriodStart) {
		this.inscriptionPeriodStart = inscriptionPeriodStart;
	}


	public String getInscriptionPeriodEnd() {
		return inscriptionPeriodEnd;
	}


	public void setInscriptionPeriodEnd(String inscriptionPeriodEnd) {
		this.inscriptionPeriodEnd = inscriptionPeriodEnd;
	}


	public String getLocation() {
		return location;
	}


	public void setLocation(String location) {
		this.location = location;
	}


	public double getFee() {
		return fee;
	}


	public void setFee(double fee) {
		this.fee = fee;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public double getInitialPayment() {
		return initialPayment;
	}


	public void setInitialPayment(double initialPayment) {
		this.initialPayment = initialPayment;
	}


	public int getTeacherId() {
		return teacherId;
	}


	public void setTeacherId(int teacherId) {
		this.teacherId = teacherId;
	}


	public int getAvailableSpots() {
		return availableSpots;
	}


	public void setAvailableSpots(int availableSpots) {
		this.availableSpots = availableSpots;
	}
	
	
	public int getUnhandledRegistrations() {
		return unhandledRegistrations;
	}


	public void setUnhandledRegistrations(int unhandledRegistrations) {
		this.unhandledRegistrations = unhandledRegistrations;
	}


	public String getTeacherInvoicesStatus() {
		return teacherInvoicesStatus;
	}


	public void setTeacherInvoicesStatus(String teacherInvoicesStatus) {
		this.teacherInvoicesStatus = teacherInvoicesStatus;
	}


	public String getEnrolmentPeriod(){
	    return this.inscriptionPeriodStart + " - " + this.inscriptionPeriodEnd;
	}
	
	public String getAvailabilityStatus() {
        if (this.availableSpots > 0) 
            return String.valueOf(this.spots);
        else 
            return "Full";
    }
	
	// El getter que tú hiciste (calculando +1 día)
    public String getEndDate() {
        if (this.startDate == null || this.startDate.isEmpty()) return "";
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(this.startDate.substring(0, 10));
            return date.plusDays(1).toString();
        } catch (Exception e) {
            return this.startDate; 
        }
    }

}
