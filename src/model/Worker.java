package model;

public class Worker {

    private String workerId;
    private String projectId;
    private String workerName;
    private String trade;
    private String area;
    private String taskId;

    public Worker(String workerId, String projectId, String workerName, String trade) {
        this.workerId = workerId;
        this.projectId = projectId;
        this.workerName = workerName;
        this.trade = trade;
    }

//  Get Method
    public String getWorkerId() {
        return workerId;
    }
    public String getProjectId() {
        return projectId;
    }
    public String getWorkerName() {
        return workerName;
    }
    public String getTrade() {
        return trade;
    }
    public String getArea() {
        return area;
    }
    public String getTaskId() {
        return taskId;
    }

//  set Method
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    public void setTrade(String trade) {
        this.trade = trade;
    }
    public void setArea(String area) {
        this.area = area;
    }
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
