package applicationimpl;

import applicationapi.ExpenseBudgetAPI;
import storageimpl.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/*
 * Implementation of expenses and budgets.
 * Uses SQLite to store project expenses
 * and manage project budgets.
 */
public class ExpenseBudgetImpl
        implements ExpenseBudgetAPI {

//  Adds an expense to a project.
    @Override
    public String addExpense(
            String projectId,
            String description,
            double amount) {

        if (!Double.isFinite(amount) ||
                amount < 0) {
            return null;
        }

        String expenseId =
                UUID.randomUUID().toString();

        String sql = """
                INSERT INTO expenses (
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

//          Stores the expense information in SQLite.
            statement.setString(
                    1,
                    expenseId);

            statement.setString(
                    2,
                    projectId);

            statement.setString(
                    3,
                    description);

            statement.setDouble(
                    4,
                    amount);

            statement.executeUpdate();

            return expenseId;

        } catch (SQLException e) {

            System.out.println(
                    "Could not add expense: "
                            + e.getMessage());

            return null;
        }
    }

//  Calculates the total expenses for a project.
    @Override
    public double getProjectExpenses(
            String projectId) {

        String sql = """
                SELECT SUM(amount) AS total
                FROM expenses
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
                    return result.getDouble("total");
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "Could not retrieve project expenses: "
                            + e.getMessage());
        }

        return 0;
    }

//  Updates the project's total budget.
    @Override
    public boolean updateProjectBudget(
            String projectId,
            double newBudget) {

        if (!Double.isFinite(newBudget) ||
                newBudget < 0) {
            return false;
        }

        String sql = """
                UPDATE projects
                SET budget = ?
                WHERE project_id = ?;
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setDouble(
                    1,
                    newBudget);

            statement.setString(
                    2,
                    projectId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not update project budget: "
                            + e.getMessage());

            return false;
        }
    }

//  Calculates the remaining project budget.
    @Override
    public double getRemainingBudget(
            String projectId) {

        String sql = """
                SELECT budget
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

                if (!result.next()) {
                    return 0;
                }

                double budget =
                        result.getDouble("budget");

                double expenses =
                        getProjectExpenses(projectId);

                return budget - expenses;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Could not calculate remaining budget: "
                            + e.getMessage());

            return 0;
        }
    }
}