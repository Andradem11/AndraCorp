package storageimpl;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import storageapi.StorageAPI;

/*
 * Implementation of the AndraCorp storage API.
 * Uses SQLite and JDBC to save and retrieve
 * project data.
 */
public class StorageImpl
        implements StorageAPI {

//  Saves basic user data in the database.
    @Override
    public boolean saveUser(
            String userId,
            String userData) {

        String sql = """
                INSERT OR REPLACE INTO users (
                    user_id,
                    name,
                    email,
                    role
                )
                VALUES (?, ?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

//          StorageAPI currently receives user data
//          as one String, so the data is stored
//          in the name field for now.
            statement.setString(
                    1,
                    userId);

            statement.setString(
                    2,
                    userData);

            statement.setString(
                    3,
                    "");

            statement.setString(
                    4,
                    "");

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not save user: "
                            + e.getMessage());

            return false;
        }
    }

//  Retrieves stored user data.
    @Override
    public String getUser(String userId) {

        String sql = """
                SELECT name
                FROM users
                WHERE user_id = ?;
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    userId);

            try (ResultSet result =
                         statement.executeQuery()) {

                if (result.next()) {
                    return result.getString("name");
                }

                return null;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Could not retrieve user: "
                            + e.getMessage());

            return null;
        }
    }

//  Saves basic project data in the database.
    @Override
    public boolean saveProject(
            String projectId,
            String projectData) {

        String sql = """
                INSERT OR REPLACE INTO projects (
                    project_id,
                    contractor_id,
                    project_name,
                    status,
                    progress,
                    budget
                )
                VALUES (?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

//          StorageAPI currently receives project data
//          as one String, so it is stored as
//          the project name for now.
            statement.setString(
                    1,
                    projectId);

            statement.setString(
                    2,
                    "storage");

            statement.setString(
                    3,
                    projectData);

            statement.setString(
                    4,
                    "ACTIVE");

            statement.setDouble(
                    5,
                    0);

            statement.setDouble(
                    6,
                    0);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not save project: "
                            + e.getMessage());

            return false;
        }
    }

//  Retrieves stored project data.
    @Override
    public String getProject(String projectId) {

        String sql = """
                SELECT project_name
                FROM projects
                WHERE project_id = ?;
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    projectId);

            try (ResultSet result =
                         statement.executeQuery()) {

                if (result.next()) {
                    return result.getString(
                            "project_name");
                }

                return null;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Could not retrieve project: "
                            + e.getMessage());

            return null;
        }
    }

//  Saves worker data in the database.
    @Override
    public boolean saveWorker(
            String workerId,
            String workerData) {

        String sql = """
                INSERT OR REPLACE INTO workers (
                    worker_id,
                    project_id,
                    worker_name
                )
                VALUES (?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

//          StorageAPI currently receives worker data
//          as one String.
            statement.setString(
                    1,
                    workerId);

            statement.setString(
                    2,
                    "storage");

            statement.setString(
                    3,
                    workerData);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not save worker: "
                            + e.getMessage());

            return false;
        }
    }

//  Saves hours worked by a worker.
    @Override
    public boolean saveWorkHours(
            String workerId,
            double hours) {

        if (!Double.isFinite(hours) ||
                hours < 0) {
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
                    "storage");

            statement.setDouble(
                    3,
                    hours);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not save work hours: "
                            + e.getMessage());

            return false;
        }
    }

//  Saves material data in the database.
    @Override
    public boolean saveMaterial(
            String materialId,
            String materialData) {

        String sql = """
                INSERT OR REPLACE INTO materials (
                    material_id,
                    project_id,
                    material_name,
                    quantity
                )
                VALUES (?, ?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    materialId);

            statement.setString(
                    2,
                    "storage");

            statement.setString(
                    3,
                    materialData);

            statement.setDouble(
                    4,
                    0);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not save material: "
                            + e.getMessage());

            return false;
        }
    }

//  Saves equipment data in the database.
    @Override
    public boolean saveEquipment(
            String equipmentId,
            String equipmentData) {

        String sql = """
                INSERT OR REPLACE INTO equipment (
                    equipment_id,
                    project_id,
                    equipment_name
                )
                VALUES (?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    equipmentId);

            statement.setString(
                    2,
                    "storage");

            statement.setString(
                    3,
                    equipmentData);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not save equipment: "
                            + e.getMessage());

            return false;
        }
    }

//  Saves an expense amount in the database.
    @Override
    public boolean saveExpense(
            String expenseId,
            double amount) {

        if (!Double.isFinite(amount) ||
                amount < 0) {
            return false;
        }

        String sql = """
                INSERT OR REPLACE INTO expenses (
                    expense_id,
                    project_id,
                    description,
                    amount
                )
                VALUES (?, ?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    expenseId);

            statement.setString(
                    2,
                    "storage");

            statement.setString(
                    3,
                    "Stored Expense");

            statement.setDouble(
                    4,
                    amount);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not save expense: "
                            + e.getMessage());

            return false;
        }
    }

//  Saves change order data in the database.
    @Override
    public boolean saveChangeOrder(
            String changeOrderId,
            String changeOrderData) {

        String sql = """
                INSERT OR REPLACE INTO change_orders (
                    change_order_id,
                    project_id,
                    description,
                    status,
                    estimated_cost_impact,
                    estimated_schedule_impact
                )
                VALUES (?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    changeOrderId);

            statement.setString(
                    2,
                    "storage");

            statement.setString(
                    3,
                    changeOrderData);

            statement.setString(
                    4,
                    "PENDING");

            statement.setDouble(
                    5,
                    0);

            statement.setInt(
                    6,
                    0);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not save change order: "
                            + e.getMessage());

            return false;
        }
    }

//  Saves schedule task data in the database.
    @Override
    public boolean saveScheduleTask(
            String taskId,
            String taskData) {

        String sql = """
                INSERT OR REPLACE INTO schedule_tasks (
                    task_id,
                    project_id,
                    task_name
                )
                VALUES (?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    taskId);

            statement.setString(
                    2,
                    "storage");

            statement.setString(
                    3,
                    taskData);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not save schedule task: "
                            + e.getMessage());

            return false;
        }
    }

//  Saves a daily project report.
    @Override
    public boolean saveDailyReport(
            String reportId,
            String reportData) {

        String sql = """
                INSERT OR REPLACE INTO daily_reports (
                    report_id,
                    project_id,
                    report
                )
                VALUES (?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    reportId);

            statement.setString(
                    2,
                    "storage");

            statement.setString(
                    3,
                    reportData);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not save daily report: "
                            + e.getMessage());

            return false;
        }
    }

//  Saves the location of a project photo.
    @Override
    public boolean savePhoto(
            String photoId,
            String photoLocation) {

        String sql = """
                INSERT OR REPLACE INTO photos (
                    photo_id,
                    project_id,
                    photo_location
                )
                VALUES (?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    photoId);

            statement.setString(
                    2,
                    "storage");

            statement.setString(
                    3,
                    photoLocation);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not save photo: "
                            + e.getMessage());

            return false;
        }
    }

//  Saves a project payment.
    @Override
    public boolean savePayment(
            String paymentId,
            double amount) {

        if (!Double.isFinite(amount) ||
                amount < 0) {
            return false;
        }

        String sql = """
                INSERT OR REPLACE INTO payments (
                    payment_id,
                    project_id,
                    amount
                )
                VALUES (?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    paymentId);

            statement.setString(
                    2,
                    "storage");

            statement.setDouble(
                    3,
                    amount);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not save payment: "
                            + e.getMessage());

            return false;
        }
    }

//  Saves a user action for project auditing.
    @Override
    public boolean saveAuditLog(
            String userId,
            String action) {

        String sql = """
                INSERT INTO audit_logs (
                    user_id,
                    action
                )
                VALUES (?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    userId);

            statement.setString(
                    2,
                    action);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not save audit log: "
                            + e.getMessage());

            return false;
        }
    }    
    
    
}