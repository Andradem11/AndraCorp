package server;

/*
 * Represents project information received
 * from the React frontend.
 */
public class ProjectRequest {

    private String contractorId;
    private String projectName;
    private double budget;


//  Empty constructor required for JSON conversion.
    public ProjectRequest() {
    }


//  Returns the contractor ID.
    public String getContractorId() {
        return contractorId;
    }


//  Updates the contractor ID.
    public void setContractorId(
            String contractorId) {

        this.contractorId =
                contractorId;
    }


//  Returns the project name.
    public String getProjectName() {
        return projectName;
    }


//  Updates the project name.
    public void setProjectName(
            String projectName) {

        this.projectName =
                projectName;
    }


//  Returns the project budget.
    public double getBudget() {
        return budget;
    }


//  Updates the project budget.
    public void setBudget(
            double budget) {

        this.budget =
                budget;
    }
}