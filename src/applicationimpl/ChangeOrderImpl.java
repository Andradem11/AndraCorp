
package applicationimpl;

import applicationapi.ChangeOrderAPI;
import model.ChangeOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
 * Prototype implementation of change orders.
 * Handles client requests and approval decisions.
 */

public class ChangeOrderImpl
        implements ChangeOrderAPI {

//  Stores change orders using their IDs.
    private Map<String, ChangeOrder> changeOrders =
            new HashMap<>();

    // Stores the client associated with each request.
    private Map<String, String> clientRequests =
            new HashMap<>();

//  Creates a new change order.
    @Override
    public String createChangeOrder(
            String projectId,
            String description) {

        String changeOrderId =
                UUID.randomUUID().toString();

        ChangeOrder order = new ChangeOrder(
                changeOrderId,
                projectId,
                description);

        changeOrders.put(changeOrderId, order);

        return changeOrderId;
    }

//  Allows a client to request a project change.
    @Override
    public String requestChange(
            String projectId,
            String clientId,
            String description) {

        if (clientId == null || clientId.isBlank()) {
            return null;
        }

        String changeOrderId =
                createChangeOrder(projectId, description);

        clientRequests.put(changeOrderId, clientId);

        return changeOrderId;
    }

//  Approves a pending client change request.
    @Override
    public boolean approveChangeOrder(
            String changeOrderId,
            String clientId) {

        ChangeOrder order =
                changeOrders.get(changeOrderId);

        if (order == null ||
                !clientId.equals(
                        clientRequests.get(changeOrderId)) ||
                !order.getStatus().equals("PENDING")) {
            return false;
        }

        order.setStatus("APPROVED");

        return true;
    }

//  Declines a pending client change request.
    @Override
    public boolean declineChangeOrder(
            String changeOrderId,
            String clientId) {

        ChangeOrder order =
                changeOrders.get(changeOrderId);

        if (order == null ||
                !clientId.equals(
                        clientRequests.get(changeOrderId)) ||
                !order.getStatus().equals("PENDING")) {
            return false;
        }

        order.setStatus("DECLINED");

        return true;
    }

//  Retrieves basic change order information.
    @Override
    public String getChangeOrder(String changeOrderId) {

        ChangeOrder order =
                changeOrders.get(changeOrderId);

        if (order == null) {
            return null;
        }

        return "Change Order: "
                + order.getDescription()
                + "\nStatus: "
                + order.getStatus()
                + "\nEstimated Cost Impact: "
                + order.getEstimatedCostImpact()
                + "\nEstimated Schedule Impact: "
                + order.getEstimatedScheduleImpact();
    }

}