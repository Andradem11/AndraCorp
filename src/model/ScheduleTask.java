package model;

public class ScheduleTask {

    private String taskId;
    private String projectId;
    private String taskName;
    private String area;
    private int assignedWorkers;
    private double estimatedLaborHours;
    private double actualLaborHours;
    private String startDate;
    private String estimatedCompletionDate;
    private double progress;
    private String dependencies;
    private String materialRequirements;
    private double laborCost;

    public ScheduleTask(String taskId, String projectId, String taskName, String area, int assignedWorkers, 
            			double estimatedLaborHours, String startDate, String estimatedCompletionDate) {
        this.taskId = taskId;
        this.projectId = projectId;
        this.taskName = taskName;
        this.area = area;
        this.assignedWorkers = assignedWorkers;
        this.estimatedLaborHours = estimatedLaborHours;
        this.startDate = startDate;
        this.estimatedCompletionDate = estimatedCompletionDate;
        this.actualLaborHours = 0;
        this.progress = 0;
        this.laborCost = 0;
    }
    
//  get method
    public String getTaskId() {
        return taskId;
    }
    public String getProjectId() {
        return projectId;
    }
    public String getTaskName() {
        return taskName;
    }
    public String getArea() {
        return area;
    }
    public int getAssignedWorkers() {
        return assignedWorkers;
    }
    public double getEstimatedLaborHours() {
        return estimatedLaborHours;
    }
    public double getActualLaborHours() {
        return actualLaborHours;
    }
    public String getStartDate() {
        return startDate;
    }
    public String getEstimatedCompletionDate() {
        return estimatedCompletionDate;
    }
    public double getProgress() {
        return progress;
    }
    public String getDependencies() {
        return dependencies;
    }
    public String getMaterialRequirements() {
        return materialRequirements;
    }
    public double getLaborCost() {
        return laborCost;
    }

//  set method
    public void setAssignedWorkers(int assignedWorkers) {
        this.assignedWorkers = assignedWorkers;
    }
    public void setEstimatedLaborHours(double estimatedLaborHours) {
        this.estimatedLaborHours = estimatedLaborHours;
    }
    public void setActualLaborHours(double actualLaborHours) {
        this.actualLaborHours = actualLaborHours;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public void setEstimatedCompletionDate(String estimatedCompletionDate) {
    	this.estimatedCompletionDate = estimatedCompletionDate;
    }
    public void setProgress(double progress) {
        this.progress = progress;
    }
    public void setDependencies(String dependencies) {
        this.dependencies = dependencies;
    }
    public void setMaterialRequirements(String materialRequirements) {
    	this.materialRequirements = materialRequirements;
    }
    public void setLaborCost(double laborCost) {
        this.laborCost = laborCost;
    }
}
