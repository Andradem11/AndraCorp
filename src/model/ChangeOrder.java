
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
    private double laborCostChange;
    private double materialCostChange;
    private double equipmentCostChange;
    private Double simulatedCostImpact;
    private Double finalCostImpact;
    private String declineReason;

    private String changeType;
    private String workforceAction;
    private Integer requestedWorkerCount;
    private String sourceArea;
    private String targetArea;
    private String materialPreference;


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

    public String getChangeType() { return changeType; }
    public String getWorkforceAction() { return workforceAction; }
    public Integer getRequestedWorkerCount() { return requestedWorkerCount; }
    public String getSourceArea() { return sourceArea; }
    public String getTargetArea() { return targetArea; }
    public String getMaterialPreference() { return materialPreference; }

    public void setChangeType(String value) { changeType = value; }
    public void setWorkforceAction(String value) { workforceAction = value; }
    public void setRequestedWorkerCount(Integer value) { requestedWorkerCount = value; }
    public void setSourceArea(String value) { sourceArea = value; }
    public void setTargetArea(String value) { targetArea = value; }
    public void setMaterialPreference(String value) { materialPreference = value; }

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

    // Cost simulation and contractor pricing.
    public double getLaborCostChange() {
        return laborCostChange;
    }
    public double getMaterialCostChange() {
        return materialCostChange;
    }
    public double getEquipmentCostChange() {
        return equipmentCostChange;
    }
    public Double getSimulatedCostImpact() {
        return simulatedCostImpact;
    }
    public Double getFinalCostImpact() {
        return finalCostImpact;
    }
    public String getDeclineReason() {
        return declineReason;
    }
    public void setLaborCostChange(double laborCostChange) {
        this.laborCostChange = laborCostChange;
    }
    public void setMaterialCostChange(double materialCostChange) {
        this.materialCostChange = materialCostChange;
    }
    public void setEquipmentCostChange(double equipmentCostChange) {
        this.equipmentCostChange = equipmentCostChange;
    }
    public void setSimulatedCostImpact(Double simulatedCostImpact) {
        this.simulatedCostImpact = simulatedCostImpact;
    }
    public void setFinalCostImpact(Double finalCostImpact) {
        this.finalCostImpact = finalCostImpact;
    }
    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
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
