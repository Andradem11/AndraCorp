package model;

public class WorkHours {

    private String workerId;
    private String taskId;
    private double hours;

    public WorkHours(String workerId, String taskId, double hours) {
        this.workerId = workerId;
        this.taskId = taskId;
        this.hours = hours;
    }

//  get method
    public String getWorkerId() {
        return workerId;
    }
    public String getTaskId() {
        return taskId;
    }
    public double getHours() {
        return hours;
    }

//  set Method
    public void setHours(double hours) {
        this.hours = hours;
    }
}
