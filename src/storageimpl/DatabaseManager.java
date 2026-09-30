package storageimpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * Handles the SQLite database connection
 * and creates the main AndraCorp tables.
 */
public class DatabaseManager {

//  Location of the local SQLite database file.
    private static final String DATABASE_URL =
            "jdbc:sqlite:andracorp.db";

//  Opens a connection to the AndraCorp database.
    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(DATABASE_URL);
    }

//  Creates the database tables if they do not already exist.
    public static void initializeDatabase() {

//      Stores contractor and client accounts.
        String usersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    role TEXT NOT NULL
                );
                """;

//      Stores project information.
        String projectsTable = """
                CREATE TABLE IF NOT EXISTS projects (
                    project_id TEXT PRIMARY KEY,
                    contractor_id TEXT NOT NULL,
                    client_id TEXT,
                    project_name TEXT NOT NULL,
                    status TEXT,
                    progress REAL,
                    estimated_completion_date TEXT,
                    budget REAL
                );
                """;

//      Stores workers assigned to projects.
        String workersTable = """
                CREATE TABLE IF NOT EXISTS workers (
                    worker_id TEXT PRIMARY KEY,
                    project_id TEXT NOT NULL,
                    worker_name TEXT NOT NULL,
                    trade TEXT,
                    area TEXT,
                    task_id TEXT
                );
                """;

//      Stores hours recorded by workers.
        String workHoursTable = """
                CREATE TABLE IF NOT EXISTS work_hours (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    worker_id TEXT NOT NULL,
                    task_id TEXT NOT NULL,
                    hours REAL NOT NULL
                );
                """;

//      Stores project expenses.
        String expensesTable = """
                CREATE TABLE IF NOT EXISTS expenses (
                    expense_id TEXT PRIMARY KEY,
                    project_id TEXT NOT NULL,
                    description TEXT,
                    amount REAL NOT NULL
                );
                """;

//      Stores project change orders and client requests.
        String changeOrdersTable = """
                CREATE TABLE IF NOT EXISTS change_orders (
                    change_order_id TEXT PRIMARY KEY,
                    project_id TEXT NOT NULL,
                    client_id TEXT,
                    description TEXT,
                    status TEXT,
                    estimated_cost_impact REAL,
                    estimated_schedule_impact INTEGER
                );
                """;
//      Stores materials used on projects.
        String materialsTable = """
                CREATE TABLE IF NOT EXISTS materials (
                    material_id TEXT PRIMARY KEY,
                    project_id TEXT NOT NULL,
                    material_name TEXT NOT NULL,
                    quantity REAL NOT NULL
                );
                """;

//      Stores equipment used on projects.
        String equipmentTable = """
                CREATE TABLE IF NOT EXISTS equipment (
                    equipment_id TEXT PRIMARY KEY,
                    project_id TEXT NOT NULL,
                    equipment_name TEXT NOT NULL
                );
                """;

//      Stores project areas, tasks, scheduling,
//      labor information, and task progress.
        String scheduleTasksTable = """
                CREATE TABLE IF NOT EXISTS schedule_tasks (
                    task_id TEXT PRIMARY KEY,
                    project_id TEXT NOT NULL,
                    task_name TEXT NOT NULL,
                    area TEXT,
                    assigned_workers INTEGER,
                    estimated_labor_hours REAL,
                    actual_labor_hours REAL,
                    start_date TEXT,
                    estimated_completion_date TEXT,
                    progress REAL,
                    dependencies TEXT,
                    material_requirements TEXT,
                    labor_cost REAL
                );
                """;

//      Stores daily project reports.
        String dailyReportsTable = """
                CREATE TABLE IF NOT EXISTS daily_reports (
                    report_id TEXT PRIMARY KEY,
                    project_id TEXT NOT NULL,
                    report TEXT
                );
                """;

//      Stores locations of project photos.
        String photosTable = """
                CREATE TABLE IF NOT EXISTS photos (
                    photo_id TEXT PRIMARY KEY,
                    project_id TEXT NOT NULL,
                    photo_location TEXT NOT NULL
                );
                """;

//      Stores project payments.
        String paymentsTable = """
                CREATE TABLE IF NOT EXISTS payments (
                    payment_id TEXT PRIMARY KEY,
                    project_id TEXT NOT NULL,
                    amount REAL NOT NULL
                );
                """;

//      Stores important user actions for
//      project history and auditing.
        String auditLogsTable = """
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id TEXT NOT NULL,
                    action TEXT NOT NULL
                );
                """;

//      Opens the database connection and creates each table.
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

        	statement.execute(usersTable);
        	statement.execute(projectsTable);
        	statement.execute(workersTable);
        	statement.execute(workHoursTable);
        	statement.execute(expensesTable);
        	statement.execute(changeOrdersTable);

        	statement.execute(materialsTable);
        	statement.execute(equipmentTable);
        	statement.execute(scheduleTasksTable);
        	statement.execute(dailyReportsTable);
        	statement.execute(photosTable);
        	statement.execute(paymentsTable);
        	statement.execute(auditLogsTable);

            System.out.println(
                    "AndraCorp database initialized successfully.");

        } catch (SQLException e) {

            System.out.println(
                    "Database initialization failed: "
                            + e.getMessage());
        }
    }
}