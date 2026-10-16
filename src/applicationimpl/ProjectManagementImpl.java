package applicationimpl;

import applicationapi.ProjectManagementAPI;
import model.Project;
import storageimpl.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * Implementation of project management.
 * Uses SQLite to store and retrieve project information.
 */
public class ProjectManagementImpl
        implements ProjectManagementAPI {

//  Creates a new construction project.
    @Override
    public String createProject(
            String contractorId,
            String projectName) {

        String projectId =
                UUID.randomUUID().toString();

        Project project = new Project(
                projectId,
                contractorId,
                projectName);

        String sql = """
                INSERT INTO projects (
                    project_id,
                    contractor_id,
                    client_id,
                    project_name,
                    status,
                    progress,
                    estimated_completion_date,
                    budget
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

//          Stores the project information in SQLite.
            statement.setString(
                    1,
                    project.getProjectId());

            statement.setString(
                    2,
                    project.getContractorId());

            statement.setString(
                    3,
                    project.getClientId());

            statement.setString(
                    4,
                    project.getProjectName());

            statement.setString(
                    5,
                    project.getStatus());

            statement.setDouble(
                    6,
                    project.getProgress());

            statement.setString(
                    7,
                    project.getEstimatedCompletionDate());

            statement.setDouble(
                    8,
                    project.getBudget());

            statement.executeUpdate();

            return projectId;

        } catch (SQLException e) {

            System.out.println(
                    "Could not create project: "
                            + e.getMessage());

            return null;
        }
    }


//  Connects a client to a project.
    @Override
    public boolean addClientToProject(
            String projectId,
            String clientId) {

        String sql = """
                UPDATE projects
                SET client_id = ?
                WHERE project_id = ?;
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    clientId);

            statement.setString(
                    2,
                    projectId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not add client to project: "
                            + e.getMessage());

            return false;
        }
    }


//  Updates an existing project.
    @Override
    public boolean updateProject(Project project) {

        if (project == null) {
            return false;
        }

        String sql = """
                UPDATE projects
                SET contractor_id = ?,
                    client_id = ?,
                    project_name = ?,
                    status = ?,
                    progress = ?,
                    estimated_completion_date = ?,
                    budget = ?
                WHERE project_id = ?;
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    project.getContractorId());

            statement.setString(
                    2,
                    project.getClientId());

            statement.setString(
                    3,
                    project.getProjectName());

            statement.setString(
                    4,
                    project.getStatus());

            statement.setDouble(
                    5,
                    project.getProgress());

            statement.setString(
                    6,
                    project.getEstimatedCompletionDate());

            statement.setDouble(
                    7,
                    project.getBudget());

            statement.setString(
                    8,
                    project.getProjectId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not update project: "
                            + e.getMessage());

            return false;
        }
    }
    

//  Updates the status of an existing project.
    @Override
    public boolean updateProjectStatus(
            String projectId,
            String status) {

        if (projectId == null || projectId.isBlank() || status == null) {
            return false;
        }

        String updatedStatus = status.trim().toUpperCase();

        if (!updatedStatus.equals("ACTIVE")
                && !updatedStatus.equals("ON_HOLD")
                && !updatedStatus.equals("COMPLETED")) {
            return false;
        }

        String sql = """
                UPDATE projects
                SET status = ?
                WHERE project_id = ?
                AND status != 'ARCHIVED';
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, updatedStatus);
            statement.setString(2, projectId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Could not update project status: " + e.getMessage());
            return false;
        }
    }

//  Archives a project without deleting its stored information.
    @Override
    public boolean deleteProject(String projectId) {

        if (projectId == null || projectId.isBlank()) {
            return false;
        }

        String sql = """
                UPDATE projects
                SET status = 'ARCHIVED'
                WHERE project_id = ?
                AND status != 'ARCHIVED';
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, projectId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Could not archive project: " + e.getMessage());
            return false;
        }
    }
    

//  Retrieves a specific project from SQLite.
    @Override
    public Project getProject(String projectId) {

        if (projectId == null || projectId.isBlank()) {
            return null;
        }

        String sql = """
                SELECT *
                FROM projects
                WHERE project_id = ?;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, projectId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapProject(result);
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not retrieve project: " + e.getMessage());
        }

        return null;
    }
    

    // Retrieves all projects assigned to a specific client.
    @Override
    public List<Project> getProjectsByClient(String clientId) {

        List<Project> projects = new ArrayList<>();

        if (clientId == null || clientId.isBlank()) {
            return projects;
        }

        String sql = """
                SELECT *
                FROM projects
                WHERE client_id = ?
                ORDER BY project_name;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, clientId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    projects.add(mapProject(result));
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not retrieve client projects: " + e.getMessage());
        }

        return projects;
    }

//  Retrieves all projects stored in SQLite.
    @Override
    public List<Project> getAllProjects() {

        List<Project> projects = new ArrayList<>();

        String sql = """
                SELECT *
                FROM projects;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                projects.add(mapProject(result));
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not retrieve projects: " + e.getMessage());
        }

        return projects;
    }


//  Converts a SQLite result into a Project object.
    private Project mapProject(ResultSet result) throws SQLException {

        Project project = new Project(
                result.getString("project_id"),
                result.getString("contractor_id"),
                result.getString("project_name")
        );

        project.setClientId(result.getString("client_id"));
        project.setStatus(result.getString("status"));
        project.setProgress(result.getDouble("progress"));
        project.setEstimatedCompletionDate(
                result.getString("estimated_completion_date")
        );
        project.setBudget(result.getDouble("budget"));

        return project;
    }

}