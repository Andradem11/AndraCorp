package applicationapi;
//Handles project expenses and budget information.
public interface ExpenseBudgetAPI {
//	Adds an expense to a project.
	String addExpense(
            String projectId,
            String description,
            double amount);
	
//	Returns the project's current budget.
	double getProjectExpenses(String projectId);
	
//	Updates the project's total budget.
	boolean updateProjectBudget(
            String projectId,
            double newBudget);
	
//	Returns the amount remaining.
	double getRemainingBudget(String projectId);
}
