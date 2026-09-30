package applicationimpltest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import applicationimpl.ProjectManagementImpl;
import model.Project;
import storageimpl.DatabaseManager;

/*
 * Tests project management using
 * the SQLite database.
 */
public class ProjectManagementImplTest {

    private ProjectManagementImpl projectManager;

//  Creates the database and project manager before each test.
    @BeforeEach
    public void setUp() {

        DatabaseManager.initializeDatabase();

        projectManager =
                new ProjectManagementImpl();
    }

//  Tests creating and retrieving a project.
    @Test
    public void testCreateAndGetProject() {

        String projectId =
                projectManager.createProject(
                        "contractor1",
                        "House Renovation");

        assertNotNull(projectId);

        String project =
                projectManager.getProject(projectId);

        assertNotNull(project);
        assertTrue(
                project.contains("House Renovation"));
        assertTrue(
                project.contains("ACTIVE"));
    }

//  Tests connecting a client to a project.
    @Test
    public void testAddClientToProject() {

        String projectId =
                projectManager.createProject(
                        "contractor1",
                        "Kitchen Remodel");

        boolean added =
                projectManager.addClientToProject(
                        projectId,
                        "client1");

        assertTrue(added);
    }

//  Tests updating project information.
    @Test
    public void testUpdateProject() {

        String projectId =
                projectManager.createProject(
                        "contractor1",
                        "Bathroom Remodel");

        Project project = new Project(
                projectId,
                "contractor1",
                "Bathroom Remodel");

        project.setProgress(50);
        project.setBudget(25000);
        project.setEstimatedCompletionDate(
                "2026-11-15");

        boolean updated =
                projectManager.updateProject(project);

        assertTrue(updated);

        String result =
                projectManager.getProject(projectId);

        assertNotNull(result);
        assertTrue(
                result.contains("50.0"));
        assertTrue(
                result.contains("25000.0"));
    }

//  Tests deleting a project.
    @Test
    public void testDeleteProject() {

        String projectId =
                projectManager.createProject(
                        "contractor1",
                        "Basement Remodel");

        boolean deleted =
                projectManager.deleteProject(projectId);

        assertTrue(deleted);

        assertNull(
                projectManager.getProject(projectId));
    }
}