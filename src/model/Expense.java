package model;

public class Expense {

    private String expenseId;
    private String projectId;
    private String description;
    private double amount;

    public Expense(String expenseId, String projectId, String description, double amount) {
        this.expenseId = expenseId;
        this.projectId = projectId;
        this.description = description;
        this.amount = amount;
    }

//  get method
    public String getExpenseId() {
        return expenseId;
    }
    public String getProjectId() {
        return projectId;
    }
    public String getDescription() {
        return description;
    }
    public double getAmount() {
        return amount;
    }
}
