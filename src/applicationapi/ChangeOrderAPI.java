
package applicationapi;

import java.util.List;

import model.ChangeOrder;

/*
 * Handles change orders between contractors and clients.
 * Both sides must approve the final proposal.
 */
public interface ChangeOrderAPI {

    // Creates a change request from either side.
    String createChangeOrder(
            String projectId,
            String requestedBy,
            String description);

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

    // Contractor approves the current proposal.
    boolean approveAsContractor(
            String changeOrderId,
            String contractorId);

    // Either side can decline a pending change order.
    boolean declineChangeOrder(
            String changeOrderId,
            String userId);

    // Retrieves a change order as a model object.
    ChangeOrder getChangeOrder(
            String changeOrderId);
    // Retrieves all change orders belonging to a project.
    List<ChangeOrder> getProjectChangeOrders(String projectId);
}
