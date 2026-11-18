package storageimpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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

        String databaseUrl = System.getProperty(
                "andracorp.database.url",
                DATABASE_URL
        );

        return DriverManager.getConnection(databaseUrl);
    }

//  Creates the database tables if they do not already exist.
    public static void initializeDatabase() {

//      Stores contractor and client accounts.
    	String usersTable = """
    	        CREATE TABLE IF NOT EXISTS users (
    	            user_id TEXT PRIMARY KEY,
    	            name TEXT NOT NULL,
    	            email TEXT NOT NULL,
    	            phone_number TEXT,
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
                    task_id TEXT,
                    is_active INTEGER NOT NULL DEFAULT 1,
                    hourly_rate REAL
                );
                """;

//      Stores hours recorded by workers, including the date worked.
        String workHoursTable = """
                CREATE TABLE IF NOT EXISTS work_hours (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    worker_id TEXT NOT NULL,
                    task_id TEXT NOT NULL,
                    hours REAL NOT NULL,
                    work_date TEXT,
                    hourly_rate REAL,
                    labor_cost REAL
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

     // Stores change orders requested by clients or contractors.
	    String changeOrdersTable = """
	            CREATE TABLE IF NOT EXISTS change_orders (
	                change_order_id TEXT PRIMARY KEY,
	                project_id TEXT NOT NULL,
	                client_id TEXT,
	                description TEXT,
	                status TEXT NOT NULL DEFAULT 'PENDING_REVIEW',
	                estimated_cost_impact REAL DEFAULT 0,
	                estimated_schedule_impact INTEGER DEFAULT 0,
	                requested_by TEXT,
	                proposed_solution TEXT,
	                client_approved INTEGER NOT NULL DEFAULT 0,
	                contractor_approved INTEGER NOT NULL DEFAULT 0,
	                proposal_version INTEGER NOT NULL DEFAULT 1,
                labor_cost_change REAL NOT NULL DEFAULT 0,
                material_cost_change REAL NOT NULL DEFAULT 0,
                equipment_cost_change REAL NOT NULL DEFAULT 0,
                simulated_cost_impact REAL,
                final_cost_impact REAL,
                decline_reason TEXT,
                change_type TEXT NOT NULL DEFAULT 'GENERAL',
                workforce_action TEXT,
                requested_worker_count INTEGER,
                source_area TEXT,
                target_area TEXT,
                material_preference TEXT
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
			
			// Adds the phone_number column to existing databases.
			boolean hasPhoneColumn = false;
			
			try (ResultSet result = statement.executeQuery(
			        "PRAGMA table_info(users)")) {			
			    while (result.next()) {
			        if ("phone_number".equalsIgnoreCase(
			                result.getString("name"))) {
			            hasPhoneColumn = true;
			            break;
			        }
			    }
			}
			if (!hasPhoneColumn) {
			    statement.executeUpdate(
			            "ALTER TABLE users " +
			            "ADD COLUMN phone_number TEXT"
			    );
			}
			
			statement.execute(projectsTable);
        	statement.execute(workersTable);

        	// Adds the is_active column to existing databases.
        	boolean hasActiveColumn = false;
        	try (ResultSet result = statement.executeQuery(
        	        "PRAGMA table_info(workers)")) {
        	    while (result.next()) {
        	        if ("is_active".equalsIgnoreCase(
        	                result.getString("name"))) {
        	            hasActiveColumn = true;
        	            break;
        	        }
        	    }
        	}
        	if (!hasActiveColumn) {
        	    statement.executeUpdate(
        	        "ALTER TABLE workers " +
        	        "ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1"
        	    );
        	}

        	         // Trade rates are contractor-defined client billing estimates.
         statement.execute("""
                 CREATE TABLE IF NOT EXISTS trade_rates (
                     trade TEXT PRIMARY KEY,
                     hourly_rate REAL NOT NULL CHECK(hourly_rate >= 0)
                 );
                 """);
         boolean hasHourlyRate = false;
         try (ResultSet result = statement.executeQuery("PRAGMA table_info(workers)")) {
             while (result.next()) {
                 if ("hourly_rate".equalsIgnoreCase(result.getString("name"))) {
                     hasHourlyRate = true;
                     break;
                 }
             }
         }
         if (!hasHourlyRate) {
             statement.executeUpdate("ALTER TABLE workers ADD COLUMN hourly_rate REAL");
         }

statement.execute(workHoursTable);
        	// Adds the work_date column to existing databases.
        	boolean hasWorkDateColumn = false;
        	try (ResultSet result = statement.executeQuery(
        	        "PRAGMA table_info(work_hours)")) {
        	    while (result.next()) {
        	        if ("work_date".equalsIgnoreCase(
        	                result.getString("name"))) {
        	            hasWorkDateColumn = true;
        	            break;
        	        }
        	    }
        	}
        	if (!hasWorkDateColumn) {
        	    statement.executeUpdate(
        	            "ALTER TABLE work_hours " +
        	            "ADD COLUMN work_date TEXT"
        	    );
        	}
     
            // Preserve existing hours; new entries snapshot their rate and cost.
            for (String column : new String[]{"hourly_rate", "labor_cost"}) {
                boolean exists = false;
                try (ResultSet columns = statement.executeQuery("PRAGMA table_info(work_hours)")) {
                    while (columns.next()) {
                        if (column.equalsIgnoreCase(columns.getString("name"))) {
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists) {
                    statement.executeUpdate("ALTER TABLE work_hours ADD COLUMN " + column + " REAL");
                }
            }

        	statement.execute(expensesTable);
        	statement.execute(changeOrdersTable);
        	// Adds new change order columns to existing databases.
        	String[][] changeOrderColumns = {
        	    {"requested_by", "TEXT"},
        	    {"proposed_solution", "TEXT"},
        	    {"client_approved", "INTEGER NOT NULL DEFAULT 0"},
        	    {"contractor_approved", "INTEGER NOT NULL DEFAULT 0"},
        	    {"proposal_version", "INTEGER NOT NULL DEFAULT 1"},
                {"labor_cost_change", "REAL NOT NULL DEFAULT 0"},
                {"material_cost_change", "REAL NOT NULL DEFAULT 0"},
                {"equipment_cost_change", "REAL NOT NULL DEFAULT 0"},
                {"simulated_cost_impact", "REAL"},
                {"final_cost_impact", "REAL"},
                {"decline_reason", "TEXT"},
                {"change_type", "TEXT NOT NULL DEFAULT 'GENERAL'"},
                {"workforce_action", "TEXT"},
                {"requested_worker_count", "INTEGER"},
                {"source_area", "TEXT"},
                {"target_area", "TEXT"},
                {"material_preference", "TEXT"}
        	};
        	for (String[] column : changeOrderColumns) {
        	    boolean columnExists = false;
        	    try (ResultSet result = statement.executeQuery(
        	            "PRAGMA table_info(change_orders)")) {
        	        while (result.next()) {
        	            if (column[0].equalsIgnoreCase(
        	                    result.getString("name"))) {
        	                columnExists = true;
        	                break;
        	            }
        	        }
        	    }
        	    if (!columnExists) {
        	        statement.executeUpdate(
        	                "ALTER TABLE change_orders ADD COLUMN "
        	                        + column[0] + " " + column[1]
        	        );
        	    }
        	}

        	        // Stores each negotiation action without overwriting earlier responses.
        statement.execute("""
                CREATE TABLE IF NOT EXISTS change_order_history (
                    history_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    change_order_id TEXT NOT NULL,
                    proposal_version INTEGER NOT NULL,
                    actor_role TEXT NOT NULL,
                    action TEXT NOT NULL,
                    message TEXT,
                    cost_impact REAL,
                    schedule_impact INTEGER,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                );
                """);

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
