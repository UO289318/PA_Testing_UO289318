package g54.si26.DTOs;

public class FormativeActionManagementDTO {
    
    private int actionId;
    private String name;
    private String status;
    private String enrolmentPeriod;
    private int totalPlaces;
    private int placesLeft;
    private String actionDate;
    private double income;
    private double expenses;
    private double balance;
    private int reservedPlaces;
    private int confirmedPlaces;

    public FormativeActionManagementDTO() {}

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEnrolmentPeriod() {
		return enrolmentPeriod;
	}

	public void setEnrolmentPeriod(String enrolmentPeriod) {
		this.enrolmentPeriod = enrolmentPeriod;
	}

	public int getTotalPlaces() {
		return totalPlaces;
	}

	public void setTotalPlaces(int totalPlaces) {
		this.totalPlaces = totalPlaces;
	}

	public int getPlacesLeft() {
		return placesLeft;
	}

	public void setPlacesLeft(int placesLeft) {
		this.placesLeft = placesLeft;
	}

	public String getActionDate() {
		return actionDate;
	}

	public void setActionDate(String actionDate) {
		this.actionDate = actionDate;
	}

	public double getIncome() {
		return income;
	}

	public void setIncome(double income) {
		this.income = income;
	}

	public double getExpenses() {
		return expenses;
	}

	public void setExpenses(double expenses) {
		this.expenses = expenses;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public int getReservedPlaces() {
		return reservedPlaces;
	}

	public void setReservedPlaces(int reservedPlaces) {
		this.reservedPlaces = reservedPlaces;
	}

	public int getConfirmedPlaces() {
		return confirmedPlaces;
	}

	public void setConfirmedPlaces(int confirmedPlaces) {
		this.confirmedPlaces = confirmedPlaces;
	}
	

    
}