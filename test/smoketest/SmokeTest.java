package smoketest;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import applicationimpl.ProjectManagementImpl;
import applicationimpl.UserManagementImpl;
import applicationimpl.WorkforceManagementImpl;
import applicationimpl.ExpenseBudgetImpl;
import applicationimpl.ChangeOrderImpl;

/*
 * Smoke tests for Week 4.
 * Tests the basic functionality of the prototype implementations.
 */

public class SmokeTest {

//  Tests creating and retrieving a user.
    @Test
    public void testUserManagement() {

        UserManagementImpl users =
                new UserManagementImpl();

        String userId = users.createUser(
                "Marco", "marco@example.com", "CONTRACTOR");

        assertTrue(userId != null);
        assertTrue(users.getUser(userId).contains("Marco"));
    }

//  Tests creating a project and adding a client.
    @Test
    public void testProjectManagement() {

        ProjectManagementImpl projects =
                new ProjectManagementImpl();

        String projectId = projects.createProject(
                "CONTRACTOR-01", "Kitchen Renovation");

        assertTrue(projectId != null);

        assertTrue(projects.addClientToProject(
                projectId, "CLIENT-01"));

        assertTrue(projects.getProject(projectId)
                .contains("Kitchen Renovation"));
    }

//  Tests adding and assigning a worker.
    @Test
    public void testWorkforceManagement() {

        WorkforceManagementImpl workforce =
                new WorkforceManagementImpl();

        String workerId = workforce.addWorker(
                "PROJECT-01", "Worker 1", "Plumbing");

        assertTrue(workforce.assignWorker(
                workerId, "PROJECT-01",
                "Kitchen", "TASK-01"));

        assertTrue(workforce.recordWorkHours(
                workerId, "TASK-01", 8));

        assertTrue(workforce.getProjectWorkforce(
                "PROJECT-01").contains("Worker 1"));
    }

//  Tests project expenses and remaining budget.
    @Test
    public void testExpenseBudget() {

        ExpenseBudgetImpl budgets =
                new ExpenseBudgetImpl();

        assertTrue(budgets.updateProjectBudget(
                "PROJECT-01", 10000));

        budgets.addExpense(
                "PROJECT-01", "Materials", 1500);

        budgets.addExpense(
                "PROJECT-01", "Equipment", 500);

        assertEquals(2000,
                budgets.getProjectExpenses("PROJECT-01"));

        assertEquals(8000,
                budgets.getRemainingBudget("PROJECT-01"));
    }

//  Tests creating and approving a change request.
    @Test
    public void testChangeOrder() {

        ChangeOrderImpl changeOrders =
                new ChangeOrderImpl();

        String changeOrderId = changeOrders.requestChange(
                "PROJECT-01",
                "CLIENT-01",
                "Complete the kitchen sooner");

        assertTrue(changeOrders.approveChangeOrder(
                changeOrderId, "CLIENT-01"));

        assertTrue(changeOrders.getChangeOrder(changeOrderId)
                .contains("APPROVED"));
    }
}