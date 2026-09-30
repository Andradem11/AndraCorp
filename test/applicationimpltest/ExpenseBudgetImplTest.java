package applicationimpltest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import applicationimpl.ExpenseBudgetImpl;
import applicationimpl.ProjectManagementImpl;
import storageimpl.DatabaseManager;

/*
 * Tests expenses and budgets using
 * the SQLite database.
 */
public class ExpenseBudgetImplTest {

    private ProjectManagementImpl projectManager;
    private ExpenseBudgetImpl expenseManager;
    private String projectId;

//  Creates a project before each test.
    @BeforeEach
    public void setUp() {

        DatabaseManager.initializeDatabase();

        projectManager =
                new ProjectManagementImpl();

        expenseManager =
                new ExpenseBudgetImpl();

        projectId =
                projectManager.createProject(
                        "contractor1",
                        "Expense Test Project");

        assertNotNull(projectId);
    }

//  Tests adding an expense to a project.
    @Test
    public void testAddExpense() {

        String expenseId =
                expenseManager.addExpense(
                        projectId,
                        "Lumber",
                        1500);

        assertNotNull(expenseId);

        assertEquals(
                1500,
                expenseManager.getProjectExpenses(
                        projectId),
                0.001);
    }

//  Tests multiple project expenses.
    @Test
    public void testTotalExpenses() {

        expenseManager.addExpense(
                projectId,
                "Lumber",
                1500);

        expenseManager.addExpense(
                projectId,
                "Concrete",
                2500);

        assertEquals(
                4000,
                expenseManager.getProjectExpenses(
                        projectId),
                0.001);
    }

//  Tests updating a project's budget.
    @Test
    public void testUpdateBudget() {

        boolean updated =
                expenseManager.updateProjectBudget(
                        projectId,
                        20000);

        assertTrue(updated);
    }

//  Tests calculating the remaining budget.
    @Test
    public void testRemainingBudget() {

        expenseManager.updateProjectBudget(
                projectId,
                20000);

        expenseManager.addExpense(
                projectId,
                "Materials",
                5000);

        expenseManager.addExpense(
                projectId,
                "Equipment Rental",
                2000);

        double remaining =
                expenseManager.getRemainingBudget(
                        projectId);

        assertEquals(
                13000,
                remaining,
                0.001);
    }

//  Tests rejecting an invalid expense.
    @Test
    public void testInvalidExpense() {

        String expenseId =
                expenseManager.addExpense(
                        projectId,
                        "Invalid Expense",
                        -500);

        assertNull(expenseId);
    }
}