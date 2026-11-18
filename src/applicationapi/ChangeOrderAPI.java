
package applicationapi;

import java.util.List;
import java.util.Map;

import model.ChangeOrder;

/*
 * Handles change orders between contractors and clients.
 * Contractor confirms the price; client makes the final decision.
 */
public interface ChangeOrderAPI {

    // Creates a change request from either side.
    String createChangeOrder(
            String projectId,
            String requestedBy,
            String description);

    // Creates a typed request while preserving the original creation method.
    String createStructuredChangeOrder(
            String projectId, String requestedBy, String description,
            String changeType, String workforceAction, Integer requestedWorkerCount,
            String sourceArea, String targetArea, String materialPreference);

    // Simulates a client request without changing the project budget.
    boolean simulateClientChange(
            String changeOrderId,
            double laborCostChange,
            double materialCostChange,
            double equipmentCostChange);

    // Contractor verifies the price before the client can approve it.
    boolean submitContractorProposal(
            String changeOrderId,
            String contractorId,
            String proposedSolution,
            double finalCostImpact,
            int scheduleImpact);

    // Client suggests another option without ending the change order.
    boolean submitClientCounterproposal(
            String changeOrderId, String clientId, String suggestion);

    // Contractor provides or revises a proposal.
    boolean proposeChange(
            String changeOrderId,
            String proposedSolution,
            double estimatedCostImpact,
            int estimatedScheduleImpact);

    // Client approves the current proposal.
    boolean approveAsClient(
            String changeOrderId,
            String clientId);

    // Client declines the proposal and explains why.
    boolean declineAsClient(
            String changeOrderId, String clientId, String reason);

    // Legacy method, no longer needed for the new workflow.
    boolean approveAsContractor(
            String changeOrderId,
            String contractorId);

    // Contractor can decline a request before sending a proposal.
    boolean declineChangeOrder(
            String changeOrderId,
            String userId);

    // Retrieves the saved negotiation timeline.
    List<Map<String, Object>> getChangeOrderHistory(String changeOrderId);

    // Retrieves a change order as a model object.
    ChangeOrder getChangeOrder(
            String changeOrderId);
    
    // Retrieves all change orders belonging to a project.
    List<ChangeOrder> getProjectChangeOrders(String projectId);
}
