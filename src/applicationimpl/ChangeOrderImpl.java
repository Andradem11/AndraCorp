package applicationimpl;

import applicationapi.ChangeOrderAPI;
import storageimpl.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/*
 * Implementation of change order management.
 * Uses SQLite to store contractor change orders
 * and client change requests.
 */
public class ChangeOrderImpl
        implements ChangeOrderAPI {

//  Creates a new change order for a project.
    @Override
    public String createChangeOrder(
            String projectId,
            String description) {

        String changeOrderId =
                UUID.randomUUID().toString();

        String sql = """
                INSERT INTO change_orders (
                    change_order_id,
                    project_id,
                    client_id,
                    description,
                    status,
                    estimated_cost_impact,
                    estimated_schedule_impact
                )
                VALUES (?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

//          Stores the change order in SQLite.
            statement.setString(
                    1,
                    changeOrderId);

            statement.setString(
                    2,
                    projectId);

            statement.setString(
                    3,
                    null);

            statement.setString(
                    4,
                    description);

            statement.setString(
                    5,
                    "PENDING");

            statement.setDouble(
                    6,
                    0);

            statement.setInt(
                    7,
                    0);

            statement.executeUpdate();

            return changeOrderId;

        } catch (SQLException e) {

            System.out.println(
                    "Could not create change order: "
                            + e.getMessage());

            return null;
        }
    }

//  Creates a change request submitted by a client.
    @Override
    public String requestChange(
            String projectId,
            String clientId,
            String description) {

        if (clientId == null ||
                clientId.isBlank()) {

            return null;
        }

        String changeOrderId =
                UUID.randomUUID().toString();

        String sql = """
                INSERT INTO change_orders (
                    change_order_id,
                    project_id,
                    client_id,
                    description,
                    status,
                    estimated_cost_impact,
                    estimated_schedule_impact
                )
                VALUES (?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

//          Stores the client's request in SQLite.
            statement.setString(
                    1,
                    changeOrderId);

            statement.setString(
                    2,
                    projectId);

            statement.setString(
                    3,
                    clientId);

            statement.setString(
                    4,
                    description);

            statement.setString(
                    5,
                    "PENDING");

            statement.setDouble(
                    6,
                    0);

            statement.setInt(
                    7,
                    0);

            statement.executeUpdate();

            return changeOrderId;

        } catch (SQLException e) {

            System.out.println(
                    "Could not request change: "
                            + e.getMessage());

            return null;
        }
    }

//  Approves a pending client change request.
    @Override
    public boolean approveChangeOrder(
            String changeOrderId,
            String clientId) {

        return updateStatus(
                changeOrderId,
                clientId,
                "APPROVED");
    }

//  Declines a pending client change request.
    @Override
    public boolean declineChangeOrder(
            String changeOrderId,
            String clientId) {

        return updateStatus(
                changeOrderId,
                clientId,
                "DECLINED");
    }

//  Updates the status of a pending change order.
    private boolean updateStatus(
            String changeOrderId,
            String clientId,
            String newStatus) {

        String sql = """
                UPDATE change_orders
                SET status = ?
                WHERE change_order_id = ?
                AND client_id = ?
                AND status = 'PENDING';
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    newStatus);

            statement.setString(
                    2,
                    changeOrderId);

            statement.setString(
                    3,
                    clientId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not update change order: "
                            + e.getMessage());

            return false;
        }
    }

//  Retrieves information about a change order.
    @Override
    public String getChangeOrder(
            String changeOrderId) {

        String sql = """
                SELECT *
                FROM change_orders
                WHERE change_order_id = ?;
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    changeOrderId);

            try (ResultSet result =
                         statement.executeQuery()) {

                if (!result.next()) {
                    return null;
                }

                return "Description: "
                        + result.getString(
                                "description")
                        + "\nStatus: "
                        + result.getString(
                                "status")
                        + "\nEstimated Cost Impact: "
                        + result.getDouble(
                                "estimated_cost_impact")
                        + "\nEstimated Schedule Impact: "
                        + result.getInt(
                                "estimated_schedule_impact");
            }

        } catch (SQLException e) {

            System.out.println(
                    "Could not retrieve change order: "
                            + e.getMessage());

            return null;
        }
    }
}