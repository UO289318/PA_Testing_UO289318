package g54.si26.secretaryStatusFA.dto;

public class FAStatusDTO {
    private int actionId;
    private String name;
    private String status;
    private String inscriptionPeriodStart;
    private String inscriptionPeriodEnd;
    private String startDate;
    private String endDate;
    private int totalSpots;
    private int confirmedPlaces;
    
    // Financials
    private double confirmedIncome;
    private double confirmedExpenses;
    private double totalRemuneration;
    private double estimatedExpenses;

    public FAStatusDTO() {}

    // Basic Data Getters/Setters
    public int getActionId() { return actionId; }
    public void setActionId(int actionId) { this.actionId = actionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getInscriptionPeriodStart() { return inscriptionPeriodStart; }
    public void setInscriptionPeriodStart(String inscriptionPeriodStart) { this.inscriptionPeriodStart = inscriptionPeriodStart; }

    public String getInscriptionPeriodEnd() { return inscriptionPeriodEnd; }
    public void setInscriptionPeriodEnd(String inscriptionPeriodEnd) { this.inscriptionPeriodEnd = inscriptionPeriodEnd; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public int getTotalSpots() { return totalSpots; }
    public void setTotalSpots(int totalSpots) { this.totalSpots = totalSpots; }

    public int getConfirmedPlaces() { return confirmedPlaces; }
    public void setConfirmedPlaces(int confirmedPlaces) { this.confirmedPlaces = confirmedPlaces; }

    // Financial Getters/Setters
    public double getConfirmedIncome() { return confirmedIncome; }
    public void setConfirmedIncome(double confirmedIncome) { this.confirmedIncome = confirmedIncome; }

    public double getConfirmedExpenses() { return confirmedExpenses; }
    public void setConfirmedExpenses(double confirmedExpenses) { this.confirmedExpenses = confirmedExpenses; }

    public double getTotalRemuneration() { return totalRemuneration; }
    public void setTotalRemuneration(double totalRemuneration) { this.totalRemuneration = totalRemuneration; }

    public double getEstimatedExpenses() { return estimatedExpenses; }
    public void setEstimatedExpenses(double estimatedExpenses) { this.estimatedExpenses = estimatedExpenses; }

    public int getPlacesLeft() {
        return totalSpots - confirmedPlaces;
    }
}
