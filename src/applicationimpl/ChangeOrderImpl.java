
package applicationimpl;

import applicationapi.ChangeOrderAPI;
import model.ChangeOrder;
import storageimpl.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * Handles change orders between contractors and clients.
 * Both sides must approve the same proposal before it is finalized.
 */
public class ChangeOrderImpl implements ChangeOrderAPI {

    // Creates a change request from either the client or contractor.
    @Override
    public String createChangeOrder(
            String projectId,
            String requestedBy,
            String description) {

        if (projectId == null || projectId.isBlank()
                || description == null || description.isBlank()
                || requestedBy == null
                || (!requestedBy.equalsIgnoreCase("CLIENT")
                && !requestedBy.equalsIgnoreCase("CONTRACTOR"))) {
            return null;
        }

        String sql = """
                INSERT INTO change_orders (
                    change_order_id, project_id, client_id,
                    description, status, estimated_cost_impact,
                    estimated_schedule_impact, requested_by,
                    client_approved, contractor_approved, proposal_version
                )
                SELECT ?, project_id, client_id, ?, 'PENDING_REVIEW',
                       0, 0, ?, 0, 0, 1
                FROM projects
                WHERE project_id = ?
                  AND client_id IS NOT NULL
                  AND UPPER(status) = 'ACTIVE';
                """;

        String changeOrderId = UUID.randomUUID().toString();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, changeOrderId);
            statement.setString(2, description.trim());
            statement.setString(3, requestedBy.toUpperCase());
            statement.setString(4, projectId);

            return statement.executeUpdate() == 1 ? changeOrderId : null;

        } catch (SQLException e) {
            System.out.println("Could not create change order: " + e.getMessage());
            return null;
        }
    }

    // Contractor submits a solution or revises an existing proposal.
    @Override
    public boolean proposeChange(
            String changeOrderId,
            String proposedSolution,
            double estimatedCostImpact,
            int estimatedScheduleImpact) {

        if (changeOrderId == null || changeOrderId.isBlank()
                || proposedSolution == null || proposedSolution.isBlank()
                || !Double.isFinite(estimatedCostImpact)) {
            return false;
        }

        String sql = """
                UPDATE change_orders
                SET proposed_solution = ?,
                    estimated_cost_impact = ?,
                    estimated_schedule_impact = ?,
                    client_approved = 0,
                    contractor_approved = 0,
                    proposal_version = proposal_version + 1,
                    status = 'PENDING_APPROVAL'
                WHERE change_order_id = ?
                  AND status IN ('PENDING_REVIEW', 'PENDING_APPROVAL');
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, proposedSolution.trim());
            statement.setDouble(2, estimatedCostImpact);
            statement.setInt(3, estimatedScheduleImpact);
            statement.setString(4, changeOrderId);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Could not save proposal: " + e.getMessage());
            return false;
        }
    }

    // Client approves the current proposal.
    @Override
    public boolean approveAsClient(String changeOrderId, String clientId) {
        return updateApproval(changeOrderId, clientId, "CLIENT");
    }

    // Contractor approves the current proposal.
    @Override
    public boolean approveAsContractor(String changeOrderId, String contractorId) {
        return updateApproval(changeOrderId, contractorId, "CONTRACTOR");
    }

    // Records approval from one side and checks if both have approved.
    private boolean updateApproval(
            String changeOrderId, String userId, String role) {

        if (changeOrderId == null || changeOrderId.isBlank()
                || userId == null || userId.isBlank()) {
            return false;
        }

        String column = role.equals("CLIENT")
                ? "client_approved" : "contractor_approved";

        String sql = """
                UPDATE change_orders
                SET %s = 1,
                    status = CASE
                        WHEN %s = 1 THEN 'APPROVED'
                        ELSE 'PENDING_APPROVAL'
                    END
                WHERE change_order_id = ?
                  AND status = 'PENDING_APPROVAL'
                  AND proposed_solution IS NOT NULL
                  AND EXISTS (
                      SELECT 1
                      FROM projects p
                      WHERE p.project_id = change_orders.project_id
                        AND UPPER(p.status) = 'ACTIVE'
                        AND %s
                  );
                """.formatted(
                column,
                role.equals("CLIENT")
                        ? "contractor_approved" : "client_approved",
                role.equals("CLIENT")
                        ? "p.client_id = ?"
                        : "p.contractor_id = ?"
        );

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, changeOrderId);
            statement.setString(2, userId);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Could not approve change order: " + e.getMessage());
            return false;
        }
    }

    // Either assigned party can decline a pending change order.
    @Override
    public boolean declineChangeOrder(String changeOrderId, String userId) {

        if (changeOrderId == null || changeOrderId.isBlank()
                || userId == null || userId.isBlank()) {
            return false;
        }

        String sql = """
                UPDATE change_orders
                SET status = 'DECLINED'
                WHERE change_order_id = ?
                  AND status IN ('PENDING_REVIEW', 'PENDING_APPROVAL')
                  AND EXISTS (
                      SELECT 1 FROM projects p
                      WHERE p.project_id = change_orders.project_id
                        AND (p.client_id = ? OR p.contractor_id = ?)
                  );
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, changeOrderId);
            statement.setString(2, userId);
            statement.setString(3, userId);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Could not decline change order: " + e.getMessage());
            return false;
        }
    }

    // Retrieves change order information using the ChangeOrder model.
    @Override
    public ChangeOrder getChangeOrder(String changeOrderId) {

        if (changeOrderId == null || changeOrderId.isBlank()) {
            return null;
        }

        String sql = """
                SELECT *
                FROM change_orders
                WHERE change_order_id = ?;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, changeOrderId);

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    return null;
                }

                // Creates a ChangeOrder object from SQLite.
                ChangeOrder changeOrder = new ChangeOrder(
                        result.getString("change_order_id"),
                        result.getString("project_id"),
                        result.getString("description")
                );

                // Loads the remaining database information.
                changeOrder.setClientId(
                        result.getString("client_id"));

                changeOrder.setStatus(
                        result.getString("status"));

                changeOrder.setEstimatedCostImpact(
                        result.getDouble("estimated_cost_impact"));

                changeOrder.setEstimatedScheduleImpact(
                        result.getInt("estimated_schedule_impact"));

                changeOrder.setRequestedBy(
                        result.getString("requested_by"));

                changeOrder.setProposedSolution(
                        result.getString("proposed_solution"));

                changeOrder.setClientApproved(
                        result.getInt("client_approved") == 1);

                changeOrder.setContractorApproved(
                        result.getInt("contractor_approved") == 1);

                changeOrder.setProposalVersion(
                        result.getInt("proposal_version"));

                return changeOrder;
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not retrieve change order: " + e.getMessage());
            return null;
        }
    }

    // Retrieves all change orders belonging to a project.
    @Override
    public List<ChangeOrder> getProjectChangeOrders(String projectId) {

        List<ChangeOrder> changeOrders = new ArrayList<>();

        if (projectId == null || projectId.isBlank()) {
            return changeOrders;
        }

        String sql = """
                SELECT *
                FROM change_orders
                WHERE project_id = ?
                ORDER BY rowid DESC;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, projectId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    ChangeOrder changeOrder = new ChangeOrder(
                            result.getString("change_order_id"),
                            result.getString("project_id"),
                            result.getString("description")
                    );

                    changeOrder.setClientId(
                            result.getString("client_id"));

                    changeOrder.setStatus(
                            result.getString("status"));

                    changeOrder.setEstimatedCostImpact(
                            result.getDouble("estimated_cost_impact"));

                    changeOrder.setEstimatedScheduleImpact(
                            result.getInt("estimated_schedule_impact"));

                    changeOrder.setRequestedBy(
                            result.getString("requested_by"));

                    changeOrder.setProposedSolution(
                            result.getString("proposed_solution"));

                    changeOrder.setClientApproved(
                            result.getInt("client_approved") == 1);

                    changeOrder.setContractorApproved(
                            result.getInt("contractor_approved") == 1);

                    changeOrder.setProposalVersion(
                            result.getInt("proposal_version"));

                    changeOrders.add(changeOrder);
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not retrieve project change orders: "
                            + e.getMessage());
        }

        return changeOrders;
    }


}
