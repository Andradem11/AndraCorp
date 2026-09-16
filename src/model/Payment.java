package model;

public class Payment {

    private String paymentId;
    private String projectId;
    private double amount;

    public Payment(String paymentId, String projectId, double amount) {
        this.paymentId = paymentId;
        this.projectId = projectId;
        this.amount = amount;
    }

//  get method
    public String getPaymentId() {
        return paymentId;
    }
    public String getProjectId() {
        return projectId;
    }
    public double getAmount() {
        return amount;
    }
}

