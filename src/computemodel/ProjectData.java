package computemodel;

import java.util.List;

import model.Expense;
import model.ScheduleTask;
import model.Worker;

public class ProjectData {

    private String projectId;
    private List<ScheduleTask> tasks;
    private List<Worker> workers;
    private List<Expense> expenses;

    public ProjectData(String projectId, List<ScheduleTask> tasks, List<Worker> workers, List<Expense> expenses) {
        this.projectId = projectId;
        this.tasks = tasks;
        this.workers = workers;
        this.expenses = expenses;
    }
    
//  get method
    public String getProjectId() {
        return projectId;
    }
    public List<ScheduleTask> getTasks() {
        return tasks;
    }
    public List<Worker> getWorkers() {
        return workers;
    }
    public List<Expense> getExpenses() {
        return expenses;
    }
}
