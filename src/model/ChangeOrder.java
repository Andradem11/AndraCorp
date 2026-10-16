
package model;

public class ChangeOrder {

    private String changeOrderId;
    private String projectId;
    private String clientId;
    private String description;
    private String status;
    private double estimatedCostImpact;
    private int estimatedScheduleImpact;

    private String requestedBy;
    private String proposedSolution;
    private boolean clientApproved;
    private boolean contractorApproved;
    private int proposalVersion;

    public ChangeOrder(
            String changeOrderId,
            String projectId,
            String description) {

        this.changeOrderId = changeOrderId;
        this.projectId = projectId;
        this.description = description;

        this.status = "PENDING_REVIEW";
        this.estimatedCostImpact = 0;
        this.estimatedScheduleImpact = 0;
        this.clientApproved = false;
        this.contractorApproved = false;
        this.proposalVersion = 1;
    }

    // Get methods
    public String getChangeOrderId() {
        return changeOrderId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public double getEstimatedCostImpact() {
        return estimatedCostImpact;
    }

    public int getEstimatedScheduleImpact() {
        return estimatedScheduleImpact;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public String getProposedSolution() {
        return proposedSolution;
    }

    public boolean isClientApproved() {
        return clientApproved;
    }

    public boolean isContractorApproved() {
        return contractorApproved;
    }

    public int getProposalVersion() {
        return proposalVersion;
    }

    // Set methods
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setEstimatedCostImpact(double estimatedCostImpact) {
        this.estimatedCostImpact = estimatedCostImpact;
    }

    public void setEstimatedScheduleImpact(int estimatedScheduleImpact) {
        this.estimatedScheduleImpact = estimatedScheduleImpact;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public void setProposedSolution(String proposedSolution) {
        this.proposedSolution = proposedSolution;
    }

    public void setClientApproved(boolean clientApproved) {
        this.clientApproved = clientApproved;
    }

    public void setContractorApproved(boolean contractorApproved) {
        this.contractorApproved = contractorApproved;
    }

    public void setProposalVersion(int proposalVersion) {
        this.proposalVersion = proposalVersion;
    }
}
