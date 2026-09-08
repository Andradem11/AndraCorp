package applicationapi;
/*
 * Handles change orders and change requests
 * between contractors and clients.
 */
public interface ChangeOrderAPI {
//	Creates a change order for a project.
	String createChangeOrder(
            String projectId,
            String description);
	
//	Allows a client to request a project change.
	String requestChange(
            String projectId,
            String clientId,
            String description);
	
//	Allows a client to approve a change order.
	boolean approveChangeOrder(
            String changeOrderId,
            String clientId);
	
//	Allows a client to decline a change order.
	boolean declineChangeOrder(
            String changeOrderId,
            String clientId);
	
//	Retrieves change order information.
	String getChangeOrder(String changeOrderId);
}
