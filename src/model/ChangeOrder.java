package model;

public class ChangeOrder {

    private String changeOrderId;
    private String projectId;
    private String description;
    private String status;
    private double estimatedCostImpact;
    private int estimatedScheduleImpact;

    public ChangeOrder(String changeOrderId, String projectId, String description) {
        this.changeOrderId = changeOrderId;
        this.projectId = projectId;
        this.description = description;

        this.status = "PENDING";
        this.estimatedCostImpact = 0;
        this.estimatedScheduleImpact = 0;
    }
    
//  get method
    public String getChangeOrderId() {
        return changeOrderId;
    }
    public String getProjectId() {
        return projectId;
    }
    public String getDescription() {
        return description;
    }
    public String getStatus() {
        return status;
    }
    public double getEstimatedCostImpact() {
        return estimatedCostImpact;
    }
    public int getEstimatedScheduleImpact() {
        return estimatedScheduleImpact;
    }

//  set method
    public void setDescription(String description) {
        this.description = description;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setEstimatedCostImpact(double estimatedCostImpact) {
        this.estimatedCostImpact = estimatedCostImpact;
    }
    public void setEstimatedScheduleImpact(int estimatedScheduleImpact) {
        this.estimatedScheduleImpact = estimatedScheduleImpact;
    }
}
