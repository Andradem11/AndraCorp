package applicationimpl;

import applicationapi.WorkforceManagementAPI;
import model.Worker;
import model.WorkHours;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
 * Prototype implementation of workforce management.
 * Handles workers, assignments, and work hours.
 */

public class WorkforceManagementImpl
        implements WorkforceManagementAPI {

//  Stores workers using their IDs.
    private Map<String, Worker> workers =
            new HashMap<>();

//  Stores recorded work hours.
    private List<WorkHours> workHours =
            new ArrayList<>();

//  Adds a worker to a project.
    @Override
    public String addWorker(
            String projectId,
            String workerName,
            String trade) {

        String workerId =
                UUID.randomUUID().toString();

        Worker worker = new Worker(
                workerId,
                projectId,
                workerName,
                trade);

        workers.put(workerId, worker);

        return workerId;
    }

//  Assigns a worker to an area and task.
    @Override
    public boolean assignWorker(
            String workerId,
            String projectId,
            String area,
            String task) {

        Worker worker = workers.get(workerId);

        if (worker == null ||
                !worker.getProjectId().equals(projectId)) {
            return false;
        }

        worker.setArea(area);
        worker.setTaskId(task);

        return true;
    }

//  Moves a worker to another area or task.
    @Override
    public boolean reassignWorker(
            String workerId,
            String newArea,
            String newTask) {

        Worker worker = workers.get(workerId);

        if (worker == null) {
            return false;
        }

        worker.setArea(newArea);
        worker.setTaskId(newTask);

        return true;
    }

//  Records the hours worked on a task.
    @Override
    public boolean recordWorkHours(
            String workerId,
            String taskId,
            double hours) {

        Worker worker = workers.get(workerId);

        if (worker == null ||
                taskId == null ||
                taskId.isBlank() ||
                !Double.isFinite(hours) ||
                hours <= 0) {
            return false;
        }

        WorkHours record = new WorkHours(
                workerId,
                taskId,
                hours);

        workHours.add(record);

        return true;
    }

//  Retrieves workers assigned to a project.
    @Override
    public String getProjectWorkforce(String projectId) {

        StringBuilder result = new StringBuilder();

        for (Worker worker : workers.values()) {

            if (worker.getProjectId().equals(projectId)) {

                result.append("Worker: ")
                      .append(worker.getWorkerName())
                      .append("\nTrade: ")
                      .append(worker.getTrade())
                      .append("\nArea: ")
                      .append(worker.getArea())
                      .append("\nTask: ")
                      .append(worker.getTaskId())
                      .append("\n\n");
            }
        }

        return result.toString();
    }

}