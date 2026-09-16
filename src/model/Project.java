package model;

public class Project {

    private String projectId;
    private String contractorId;
    private String clientId;
    private String projectName;

    private String status;
    private double progress;
    private String estimatedCompletionDate;
    private double budget;

    public Project(String projectId,  String contractorId, String projectName) {
        this.projectId = projectId;
        this.contractorId = contractorId;
        this.projectName = projectName;
        this.status = "ACTIVE";
        this.progress = 0;
        this.budget = 0;
    }

//  get Method
    public String getProjectId() {
        return projectId;
    }
    public String getContractorId() {
        return contractorId;
    }
    public String getClientId() {
        return clientId;
    }
    public String getProjectName() {
        return projectName;
    }
    public String getStatus() {
        return status;
    }
    public double getProgress() {
        return progress;
    }
    public String getEstimatedCompletionDate() {
        return estimatedCompletionDate;
    }
    public double getBudget() {
        return budget;
    }

//  set Method
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setProgress(double progress) {
        this.progress = progress;
    }
    public void setEstimatedCompletionDate(String estimatedCompletionDate) {
        this.estimatedCompletionDate = estimatedCompletionDate;
    }
    public void setBudget(double budget) {
        this.budget = budget;
    }
}

