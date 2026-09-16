package model;

public class AuditLog {

    private String userId;
    private String action;

    public AuditLog(String userId, String action) {
        this.userId = userId;
        this.action = action;
    }

//  get method
    public String getUserId() {
        return userId;
    }
    public String getAction() {
        return action;
    }
}
