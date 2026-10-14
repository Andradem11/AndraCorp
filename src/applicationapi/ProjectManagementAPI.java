
package applicationapi;

import java.util.List;
import model.Project;

/*
 * This interface is responsible for:
 * Contractors will mainly create and modify projects.
 * Clients will mainly retrieve project information.
 */
public interface ProjectManagementAPI {
    // Creates a new construction project for a contractor.
    String createProject(
            String contractorId,
            String projectName);

    // Connects a client account to a specific project.
    boolean addClientToProject(
            String projectId,
            String clientId);

    // Updates general project information.
    boolean updateProject(Project project);

    // Updates the current status of a project.
    boolean updateProjectStatus(
            String projectId,
            String status);

    // Removes a project from the system.
    boolean deleteProject(String projectId);

    // Retrieves information about a specific project.
    String getProject(String projectId);

    // Returns all projects stored in the system.
    List<Project> getAllProjects();
}
