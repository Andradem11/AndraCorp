package applicationimpl;

import applicationapi.ProjectManagementAPI;
import model.Project;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
 * Prototype implementation of project management.
 * Uses temporary storage until the database is implemented.
 */

public class ProjectManagementImpl
        implements ProjectManagementAPI {

//  Stores projects using their IDs.
    private Map<String, Project> projects =
            new HashMap<>();

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

        projects.put(projectId, project);

        return projectId;
    }

//  Connects a client to a project.
    @Override
    public boolean addClientToProject(
            String projectId,
            String clientId) {

        Project project = projects.get(projectId);

        if (project == null) {
            return false;
        }

        project.setClientId(clientId);

        return true;
    }

//  Updates an existing project.
    @Override
    public boolean updateProject(Project project) {

        if (project == null ||
                !projects.containsKey(
                        project.getProjectId())) {
            return false;
        }

        projects.put(
                project.getProjectId(),
                project);

        return true;
    }

//  Deletes a project from temporary storage.
    @Override
    public boolean deleteProject(String projectId) {

        return projects.remove(projectId) != null;
    }

//  Retrieves basic project information.
    @Override
    public String getProject(String projectId) {

        Project project = projects.get(projectId);

        if (project == null) {
            return null;
        }

        return "Project: " + project.getProjectName()
                + "\nStatus: " + project.getStatus()
                + "\nProgress: " + project.getProgress()
                + "\nBudget: " + project.getBudget();
    }

}