package applicationimpl;

import applicationapi.WorkforceManagementAPI;
import model.Worker;
import storageimpl.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * Implementation of workforce management.
 * Uses SQLite to store workers, assignments,
 * and recorded work hours.
 */
public class WorkforceManagementImpl
        implements WorkforceManagementAPI {


//  Adds a worker to a project.
    @Override
    public String addWorker(
            String projectId,
            String workerName,
            String trade) {

        String workerId =
                UUID.randomUUID().toString();

        String sql = """
                INSERT INTO workers (
                    worker_id,
                    project_id,
                    worker_name,
                    trade,
                    area,
                    task_id
                )
                VALUES (?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

//          Stores the worker information in SQLite.
            statement.setString(
                    1,
                    workerId);

            statement.setString(
                    2,
                    projectId);

            statement.setString(
                    3,
                    workerName);

            statement.setString(
                    4,
                    trade);

            statement.setString(
                    5,
                    null);

            statement.setString(
                    6,
                    null);

            statement.executeUpdate();

            return workerId;

        } catch (SQLException e) {

            System.out.println(
                    "Could not add worker: "
                            + e.getMessage());

            return null;
        }
    }


//  Assigns a worker to an area and task.
    @Override
    public boolean assignWorker(
            String workerId,
            String projectId,
            String area,
            String task) {

        String sql = """
                UPDATE workers
                SET area = ?,
                    task_id = ?
                WHERE worker_id = ?
                AND project_id = ?;
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    area);

            statement.setString(
                    2,
                    task);

            statement.setString(
                    3,
                    workerId);

            statement.setString(
                    4,
                    projectId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not assign worker: "
                            + e.getMessage());

            return false;
        }
    }


//  Moves a worker to another area or task.
    @Override
    public boolean reassignWorker(
            String workerId,
            String newArea,
            String newTask) {

        String sql = """
                UPDATE workers
                SET area = ?,
                    task_id = ?
                WHERE worker_id = ?;
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    newArea);

            statement.setString(
                    2,
                    newTask);

            statement.setString(
                    3,
                    workerId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not reassign worker: "
                            + e.getMessage());

            return false;
        }
    }


//  Records the hours worked on a task.
    @Override
    public boolean recordWorkHours(
            String workerId,
            String taskId,
            double hours) {

        if (taskId == null ||
                taskId.isBlank() ||
                !Double.isFinite(hours) ||
                hours <= 0) {

            return false;
        }


//      Makes sure the worker exists before recording hours.
        if (!workerExists(workerId)) {
            return false;
        }

        String sql = """
                INSERT INTO work_hours (
                    worker_id,
                    task_id,
                    hours
                )
                VALUES (?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    workerId);

            statement.setString(
                    2,
                    taskId);

            statement.setDouble(
                    3,
                    hours);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not record work hours: "
                            + e.getMessage());

            return false;
        }
    }


//  Retrieves workers assigned to a project.
    @Override
    public String getProjectWorkforce(
            String projectId) {

        String sql = """
                SELECT *
                FROM workers
                WHERE project_id = ?;
                """;

        StringBuilder result =
                new StringBuilder();

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    projectId);

            try (ResultSet workers =
                         statement.executeQuery()) {

                while (workers.next()) {

                    result.append("Worker: ")
                            .append(
                                    workers.getString(
                                            "worker_name"))

                            .append("\nTrade: ")
                            .append(
                                    workers.getString(
                                            "trade"))

                            .append("\nArea: ")
                            .append(
                                    workers.getString(
                                            "area"))

                            .append("\nTask: ")
                            .append(
                                    workers.getString(
                                            "task_id"))

                            .append("\n\n");
                }
            }

            return result.toString();

        } catch (SQLException e) {

            System.out.println(
                    "Could not retrieve workforce: "
                            + e.getMessage());

            return "";
        }
    }


    /*
     * Retrieves all workers currently
     * stored in the SQLite database.
     */
    @Override
    public List<Worker> getAllWorkers() {

        List<Worker> workers =
                new ArrayList<>();

        String sql = """
                SELECT *
                FROM workers;
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet result =
                     statement.executeQuery()) {


//          Goes through every worker returned
//          from the SQLite database.
            while (result.next()) {

                Worker worker =
                        new Worker(
                                result.getString(
                                        "worker_id"),

                                result.getString(
                                        "project_id"),

                                result.getString(
                                        "worker_name"),

                                result.getString(
                                        "trade"));


//              Loads the worker's current
//              area and task assignment.
                worker.setArea(
                        result.getString(
                                "area"));

                worker.setTaskId(
                        result.getString(
                                "task_id"));


//              Adds the worker to the list
//              returned to the backend server.
                workers.add(worker);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Could not retrieve workers: "
                            + e.getMessage());
        }

        return workers;
    }


//  Checks whether a worker exists in the database.
    private boolean workerExists(
            String workerId) {

        String sql = """
                SELECT worker_id
                FROM workers
                WHERE worker_id = ?;
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    workerId);

            try (ResultSet result =
                         statement.executeQuery()) {

                return result.next();
            }

        } catch (SQLException e) {

            System.out.println(
                    "Could not check worker: "
                            + e.getMessage());

            return false;
        }
    }
}