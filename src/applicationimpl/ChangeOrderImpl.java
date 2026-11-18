
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
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;

/*
 * Handles change orders between contractors and clients.
 * Both sides must approve the same proposal before it is finalized.
 */
public class ChangeOrderImpl implements ChangeOrderAPI {

    // Creates a change request from either the client or contractor.
    @Override
    public String createChangeOrder(String projectId, String requestedBy, String description) {
        return createStructuredChangeOrder(projectId, requestedBy, description,
                "GENERAL", null, null, null, null, null);
    }

    @Override
    public String createStructuredChangeOrder(
            String projectId, String requestedBy, String description,
            String changeType, String workforceAction, Integer requestedWorkerCount,
            String sourceArea, String targetArea, String materialPreference) {

        if (projectId == null || projectId.isBlank()
                || description == null || description.isBlank()
                || requestedBy == null
                || (!requestedBy.equalsIgnoreCase("CLIENT")
                && !requestedBy.equalsIgnoreCase("CONTRACTOR"))) {
            return null;
        }

        String type = changeType == null ? "GENERAL" : changeType.trim().toUpperCase();
        if (!type.equals("GENERAL") && !type.equals("MATERIAL")
                && !type.equals("WORKFORCE")) {
            return null;
        }

        String action = workforceAction == null ? null : workforceAction.trim().toUpperCase();
        if (type.equals("WORKFORCE")) {
            if (action == null || (!action.equals("ADD_WORKERS")
                    && !action.equals("REALLOCATE"))
                    || requestedWorkerCount == null || requestedWorkerCount <= 0
                    || requestedWorkerCount > 100) {
                return null;
            }
            if (action.equals("REALLOCATE")
                    && (sourceArea == null || sourceArea.isBlank()
                    || targetArea == null || targetArea.isBlank()
                    || sourceArea.trim().equalsIgnoreCase(targetArea.trim()))) {
                return null;
            }
        } else if (action != null || requestedWorkerCount != null
                || sourceArea != null || targetArea != null) {
            return null;
        }
        if (!type.equals("MATERIAL") && materialPreference != null) {
            return null;
        }

        String sql = """
                INSERT INTO change_orders (
                    change_order_id, project_id, client_id,
                    description, status, estimated_cost_impact,
                    estimated_schedule_impact, requested_by,
                    client_approved, contractor_approved, proposal_version,
                    change_type, workforce_action, requested_worker_count,
                    source_area, target_area, material_preference
                )
                SELECT ?, project_id, client_id, ?, 'PENDING_REVIEW',
                       0, 0, ?, 0, 0, 1, ?, ?, ?, ?, ?, ?
                FROM projects
                WHERE project_id = ?
                  AND client_id IS NOT NULL
                  AND UPPER(status) = 'ACTIVE';
                """;

        String id = UUID.randomUUID().toString();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.setString(2, description.trim());
            statement.setString(3, requestedBy.toUpperCase());
            statement.setString(4, type);
            statement.setString(5, action);
            if (requestedWorkerCount == null) statement.setNull(6, java.sql.Types.INTEGER);
            else statement.setInt(6, requestedWorkerCount);
            statement.setString(7, sourceArea == null ? null : sourceArea.trim());
            statement.setString(8, targetArea == null ? null : targetArea.trim());
            statement.setString(9, materialPreference == null ? null : materialPreference.trim());
            statement.setString(10, projectId);
            return statement.executeUpdate() == 1 ? id : null;
        } catch (SQLException e) {
            System.out.println("Could not create change order: " + e.getMessage());
            return null;
        }
    }

    // Calculates a preliminary cost estimate for a client request.
    // Positive values increase cost; negative values reduce cost.
    @Override
    public boolean simulateClientChange(
            String changeOrderId,
            double laborCostChange,
            double materialCostChange,
            double equipmentCostChange) {
        if (changeOrderId == null || changeOrderId.isBlank()
                || !Double.isFinite(laborCostChange)
                || !Double.isFinite(materialCostChange)
                || !Double.isFinite(equipmentCostChange)) {
            return false;
        }
        double simulatedCost = laborCostChange + materialCostChange + equipmentCostChange;
        if (!Double.isFinite(simulatedCost)) {
            return false;
        }
        String sql = """
                UPDATE change_orders
                SET labor_cost_change = ?,
                    material_cost_change = ?,
                    equipment_cost_change = ?,
                    simulated_cost_impact = ?
                WHERE change_order_id = ?
                  AND requested_by = 'CLIENT'
                  AND status = 'PENDING_REVIEW'
                  AND EXISTS (
                      SELECT 1 FROM projects p
                      WHERE p.project_id = change_orders.project_id
                        AND UPPER(p.status) = 'ACTIVE'
                  );
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, laborCostChange);
            statement.setDouble(2, materialCostChange);
            statement.setDouble(3, equipmentCostChange);
            statement.setDouble(4, simulatedCost);
            statement.setString(5, changeOrderId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Could not simulate change order: " + e.getMessage());
            return false;
        }
    }

    // Contractor confirms the final cost and sends the proposal to the client.
    @Override
    public boolean submitContractorProposal(
            String changeOrderId,
            String contractorId,
            String proposedSolution,
            double finalCostImpact,
            int scheduleImpact) {
        if (changeOrderId == null || changeOrderId.isBlank()
                || contractorId == null || contractorId.isBlank()
                || proposedSolution == null || proposedSolution.isBlank()
                || !Double.isFinite(finalCostImpact)) {
            return false;
        }
        String sql = """
                UPDATE change_orders
                SET proposed_solution = ?,
                    final_cost_impact = ?,
                    estimated_cost_impact = ?,
                    estimated_schedule_impact = ?,
                    contractor_approved = 1,
                    client_approved = 0,
                    decline_reason = NULL,
                    proposal_version = proposal_version + 1,
                    status = 'PENDING_APPROVAL'
                WHERE change_order_id = ?
                  AND status IN ('PENDING_REVIEW', 'COUNTERPROPOSAL_PENDING')
                  AND EXISTS (
                      SELECT 1 FROM projects p
                      WHERE p.project_id = change_orders.project_id
                        AND p.contractor_id = ?
                        AND UPPER(p.status) = 'ACTIVE'
                  );
                """;
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, proposedSolution.trim());
            statement.setDouble(2, finalCostImpact);
            statement.setDouble(3, finalCostImpact);
            statement.setInt(4, scheduleImpact);
            statement.setString(5, changeOrderId);
            statement.setString(6, contractorId);
            if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                writeHistory(connection, changeOrderId, "CONTRACTOR", "PROPOSAL", proposedSolution);
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Could not submit contractor proposal: " + e.getMessage());
            return false;
        }
    }

    // Client asks for another material or solution. Contractor must price it.
    @Override
    public boolean submitClientCounterproposal(
            String changeOrderId, String clientId, String suggestion) {
        if (changeOrderId == null || changeOrderId.isBlank()
                || clientId == null || clientId.isBlank()
                || suggestion == null || suggestion.isBlank()) {
            return false;
        }

        String update = """
                UPDATE change_orders
                SET status = 'COUNTERPROPOSAL_PENDING',
                    client_approved = 0,
                    contractor_approved = 0,
                    decline_reason = NULL,
                    proposal_version = proposal_version + 1
                WHERE change_order_id = ?
                  AND status = 'PENDING_APPROVAL'
                  AND EXISTS (
                      SELECT 1 FROM projects p
                      WHERE p.project_id = change_orders.project_id
                        AND p.client_id = ?
                        AND UPPER(p.status) = 'ACTIVE'
                  );
                """;
        String history = """
                INSERT INTO change_order_history (
                    change_order_id, proposal_version, actor_role,
                    action, message
                )
                SELECT change_order_id, proposal_version, 'CLIENT',
                       'COUNTERPROPOSAL', ?
                FROM change_orders WHERE change_order_id = ?;
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(update);
                 PreparedStatement log = connection.prepareStatement(history)) {
                statement.setString(1, changeOrderId);
                statement.setString(2, clientId);
                if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                log.setString(1, suggestion.trim());
                log.setString(2, changeOrderId);
                if (log.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Could not submit counterproposal: " + e.getMessage());
            return false;
        }
    }

    // Kept for compatibility; use submitContractorProposal with a contractor ID.
    @Override
    public boolean proposeChange(
            String changeOrderId,
            String proposedSolution,
            double estimatedCostImpact,
            int estimatedScheduleImpact) {
        return false;
    }

    // Client accepts the contractor's final proposal.
    @Override
    public boolean approveAsClient(String changeOrderId, String clientId) {
        if (changeOrderId == null || changeOrderId.isBlank()
                || clientId == null || clientId.isBlank()) {
            return false;
        }

        String sql = """
                UPDATE change_orders
                SET client_approved = 1,
                    status = 'APPROVED'
                WHERE change_order_id = ?
                  AND status = 'PENDING_APPROVAL'
                  AND contractor_approved = 1
                  AND proposed_solution IS NOT NULL
                  AND final_cost_impact IS NOT NULL
                  AND EXISTS (
                      SELECT 1 FROM projects p
                      WHERE p.project_id = change_orders.project_id
                        AND p.client_id = ?
                        AND UPPER(p.status) = 'ACTIVE'
                  );
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, changeOrderId);
            statement.setString(2, clientId);
            if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                writeHistory(connection, changeOrderId, "CLIENT", "APPROVED", null);
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Could not approve change order: " + e.getMessage());
            return false;
        }
    }

    // The contractor already confirms the proposal when submitting it.
    // Kept for compatibility with the older interface.
    @Override
    public boolean approveAsContractor(String changeOrderId, String contractorId) {
        return false;
    }

    // Client declines a proposal and can explain why.
    @Override
    public boolean declineAsClient(
            String changeOrderId, String clientId, String reason) {
        if (changeOrderId == null || changeOrderId.isBlank()
                || clientId == null || clientId.isBlank()
                || reason == null || reason.isBlank()) {
            return false;
        }

        String sql = """
                UPDATE change_orders
                SET status = 'DECLINED',
                    client_approved = 0,
                    decline_reason = ?
                WHERE change_order_id = ?
                  AND status = 'PENDING_APPROVAL'
                  AND EXISTS (
                      SELECT 1 FROM projects p
                      WHERE p.project_id = change_orders.project_id
                        AND p.client_id = ?
                        AND UPPER(p.status) = 'ACTIVE'
                  );
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reason.trim());
            statement.setString(2, changeOrderId);
            statement.setString(3, clientId);
            if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                writeHistory(connection, changeOrderId, "CLIENT", "DECLINED", reason);
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Could not decline change order: " + e.getMessage());
            return false;
        }
    }

    // Contractor can reject a client request before proposing a price.
    // Client declines use declineAsClient so their reason is recorded.
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
                  AND status = 'PENDING_REVIEW'
                  AND EXISTS (
                      SELECT 1 FROM projects p
                      WHERE p.project_id = change_orders.project_id
                        AND p.contractor_id = ?
                        AND UPPER(p.status) = 'ACTIVE'
                  );
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, changeOrderId);
            statement.setString(2, userId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Could not decline change order: " + e.getMessage());
            return false;
        }
    }

    // Records a completed negotiation action in the same database transaction.
    private void writeHistory(Connection connection, String id, String role,
            String action, String message) throws SQLException {
        String sql = """
                INSERT INTO change_order_history (
                    change_order_id, proposal_version, actor_role, action,
                    message, cost_impact, schedule_impact
                )
                SELECT change_order_id, proposal_version, ?, ?, ?,
                       final_cost_impact, estimated_schedule_impact
                FROM change_orders WHERE change_order_id = ?;
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role);
            statement.setString(2, action);
            statement.setString(3, message);
            statement.setString(4, id);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Could not save change order history.");
            }
        }
    }

    // Returns the timeline for one change order, oldest action first.
    @Override
    public List<Map<String, Object>> getChangeOrderHistory(String changeOrderId) {
        List<Map<String, Object>> history = new ArrayList<>();
        if (changeOrderId == null || changeOrderId.isBlank()) {
            return history;
        }
        String sql = """
                SELECT history_id, proposal_version, actor_role, action,
                       message, cost_impact, schedule_impact, created_at
                FROM change_order_history
                WHERE change_order_id = ? ORDER BY history_id ASC;
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, changeOrderId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("historyId", result.getLong("history_id"));
                    item.put("proposalVersion", result.getInt("proposal_version"));
                    item.put("actorRole", result.getString("actor_role"));
                    item.put("action", result.getString("action"));
                    item.put("message", result.getString("message"));
                    item.put("costImpact", result.getObject("cost_impact"));
                    item.put("scheduleImpact", result.getObject("schedule_impact"));
                    item.put("createdAt", result.getString("created_at"));
                    history.add(item);
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not load change order history: " + e.getMessage());
        }
        return history;
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

                loadSimulationFields(result, changeOrder);

                return changeOrder;
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not retrieve change order: " + e.getMessage());
            return null;
        }
    }

    private void loadSimulationFields(ResultSet result, ChangeOrder changeOrder)
            throws SQLException {
        changeOrder.setLaborCostChange(result.getDouble("labor_cost_change"));
        changeOrder.setMaterialCostChange(result.getDouble("material_cost_change"));
        changeOrder.setEquipmentCostChange(result.getDouble("equipment_cost_change"));
        double simulated = result.getDouble("simulated_cost_impact");
        changeOrder.setSimulatedCostImpact(result.wasNull() ? null : simulated);
        double finalCost = result.getDouble("final_cost_impact");
        changeOrder.setFinalCostImpact(result.wasNull() ? null : finalCost);
        changeOrder.setDeclineReason(result.getString("decline_reason"));
        changeOrder.setChangeType(result.getString("change_type"));
        changeOrder.setWorkforceAction(result.getString("workforce_action"));
        int count = result.getInt("requested_worker_count");
        changeOrder.setRequestedWorkerCount(result.wasNull() ? null : count);
        changeOrder.setSourceArea(result.getString("source_area"));
        changeOrder.setTargetArea(result.getString("target_area"));
        changeOrder.setMaterialPreference(result.getString("material_preference"));
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

                    loadSimulationFields(result, changeOrder);

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
