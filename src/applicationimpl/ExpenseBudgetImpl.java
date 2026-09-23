package applicationimpl;

import applicationapi.ExpenseBudgetAPI;
import model.Expense;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
 * Prototype implementation of expenses and budgets.
 * Calculates project spending and remaining budgets.
 */

public class ExpenseBudgetImpl
        implements ExpenseBudgetAPI {

//  Stores expenses for each project.
    private Map<String, List<Expense>> expenses =
            new HashMap<>();

//  Stores the budget for each project.
    private Map<String, Double> budgets =
            new HashMap<>();

//  Adds an expense to a project.
    @Override
    public String addExpense(
            String projectId,
            String description,
            double amount) {

        if (!Double.isFinite(amount) || amount < 0) {
            return null;
        }

        String expenseId =
                UUID.randomUUID().toString();

        Expense expense = new Expense(
                expenseId,
                projectId,
                description,
                amount);

//      Creates an expense list if one doesn't exist.
        expenses.computeIfAbsent(
                projectId,
                id -> new ArrayList<>()
        ).add(expense);

        return expenseId;
    }

//  Calculates the total project expenses.
    @Override
    public double getProjectExpenses(String projectId) {

        double total = 0;

        List<Expense> projectExpenses =
                expenses.get(projectId);

        if (projectExpenses == null) {
            return 0;
        }

        for (Expense expense : projectExpenses) {
            total += expense.getAmount();
        }

        return total;
    }

//  Updates the total project budget.
    @Override
    public boolean updateProjectBudget(
            String projectId,
            double newBudget) {

        if (!Double.isFinite(newBudget) ||
                newBudget < 0) {
            return false;
        }

        budgets.put(projectId, newBudget);

        return true;
    }

//  Calculates the remaining budget.
    @Override
    public double getRemainingBudget(String projectId) {

        double budget =
                budgets.getOrDefault(projectId, 0.0);

        double totalExpenses =
                getProjectExpenses(projectId);

        return budget - totalExpenses;
    }

}