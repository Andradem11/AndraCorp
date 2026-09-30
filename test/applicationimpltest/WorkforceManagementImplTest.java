package applicationimpltest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import applicationimpl.WorkforceManagementImpl;
import storageimpl.DatabaseManager;

/*
 * Tests workforce management using
 * the SQLite database.
 */
public class WorkforceManagementImplTest {

    private WorkforceManagementImpl workforceManager;

//  Creates the database and workforce manager before each test.
    @BeforeEach
    public void setUp() {

        DatabaseManager.initializeDatabase();

        workforceManager =
                new WorkforceManagementImpl();
    }

//  Tests adding and retrieving a worker.
    @Test
    public void testAddWorker() {

        String workerId =
                workforceManager.addWorker(
                        "project1",
                        "Worker One",
                        "Plumbing");

        assertNotNull(workerId);

        String workforce =
                workforceManager.getProjectWorkforce(
                        "project1");

        assertTrue(
                workforce.contains("Worker One"));

        assertTrue(
                workforce.contains("Plumbing"));
    }

//  Tests assigning a worker to an area and task.
    @Test
    public void testAssignWorker() {

        String workerId =
                workforceManager.addWorker(
                        "project2",
                        "Worker Two",
                        "Electrical");

        boolean assigned =
                workforceManager.assignWorker(
                        workerId,
                        "project2",
                        "Kitchen",
                        "Electrical");

        assertTrue(assigned);

        String workforce =
                workforceManager.getProjectWorkforce(
                        "project2");

        assertTrue(
                workforce.contains("Kitchen"));

        assertTrue(
                workforce.contains("Electrical"));
    }

//  Tests moving a worker to another area and task.
    @Test
    public void testReassignWorker() {

        String workerId =
                workforceManager.addWorker(
                        "project3",
                        "Worker Three",
                        "Carpentry");

        workforceManager.assignWorker(
                workerId,
                "project3",
                "Kitchen",
                "Cabinets");

        boolean reassigned =
                workforceManager.reassignWorker(
                        workerId,
                        "Living Room",
                        "Trim");

        assertTrue(reassigned);

        String workforce =
                workforceManager.getProjectWorkforce(
                        "project3");

        assertTrue(
                workforce.contains("Living Room"));

        assertTrue(
                workforce.contains("Trim"));
    }

//  Tests recording valid work hours.
    @Test
    public void testRecordWorkHours() {

        String workerId =
                workforceManager.addWorker(
                        "project4",
                        "Worker Four",
                        "Painting");

        boolean recorded =
                workforceManager.recordWorkHours(
                        workerId,
                        "painting-task",
                        8.0);

        assertTrue(recorded);
    }

//  Tests that invalid work hours are rejected.
    @Test
    public void testInvalidWorkHours() {

        String workerId =
                workforceManager.addWorker(
                        "project5",
                        "Worker Five",
                        "Sheetrock");

        boolean recorded =
                workforceManager.recordWorkHours(
                        workerId,
                        "sheetrock-task",
                        -5);

        assertFalse(recorded);
    }
}