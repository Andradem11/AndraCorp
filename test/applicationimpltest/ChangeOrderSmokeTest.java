package applicationimpltest;

import model.ChangeOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import applicationimpl.ChangeOrderImpl;
import storageimpl.DatabaseManager;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ChangeOrderSmokeTest {

    @Test
    void testChangeOrderWorkflow(@TempDir Path tempDir) throws Exception {

        String oldUrl = System.getProperty("andracorp.database.url");

        System.setProperty(
                "andracorp.database.url",
                "jdbc:sqlite:" + tempDir.resolve("test.db")
        );

        try {
            DatabaseManager.initializeDatabase();

            String projectId = UUID.randomUUID().toString();
            String contractorId = "test-contractor";
            String clientId = "test-client";

            // Create a test project in the temporary database.
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("""
                         INSERT INTO projects (
                             project_id, contractor_id, client_id,
                             project_name, status, progress, budget
                         )
                         VALUES (?, ?, ?, ?, 'ACTIVE', 0, 10000)
                         """)) {

                statement.setString(1, projectId);
                statement.setString(2, contractorId);
                statement.setString(3, clientId);
                statement.setString(4, "Test Kitchen Remodel");
                statement.executeUpdate();
            }

            ChangeOrderImpl manager = new ChangeOrderImpl();

            // Test 1: Client requests a change.
            String orderId = manager.createChangeOrder(
                    projectId,
                    "CLIENT",
                    "Finish the kitchen three days earlier"
            );

            assertNotNull(orderId);

            ChangeOrder order = manager.getChangeOrder(orderId);
            assertNotNull(order);
            assertEquals("PENDING_REVIEW", order.getStatus());
            assertEquals("CLIENT", order.getRequestedBy());

            // Test 2: Contractor submits a proposal.
            assertTrue(manager.proposeChange(
                    orderId,
                    "Add two workers to finish earlier",
                    1200.00,
                    -3
            ));

            order = manager.getChangeOrder(orderId);
            assertEquals("PENDING_APPROVAL", order.getStatus());
            assertEquals(1200.00, order.getEstimatedCostImpact(), 0.001);
            assertEquals(-3, order.getEstimatedScheduleImpact());

            // Test 3: Client approves first.
            assertTrue(manager.approveAsClient(orderId, clientId));

            order = manager.getChangeOrder(orderId);
            assertTrue(order.isClientApproved());
            assertFalse(order.isContractorApproved());
            assertEquals("PENDING_APPROVAL", order.getStatus());

            // Test 4: Revision resets approvals.
            int previousVersion = order.getProposalVersion();

            assertTrue(manager.proposeChange(
                    orderId,
                    "Reallocate two existing workers instead",
                    0.00,
                    -2
            ));

            order = manager.getChangeOrder(orderId);
            assertFalse(order.isClientApproved());
            assertFalse(order.isContractorApproved());
            assertEquals(previousVersion + 1, order.getProposalVersion());

            // Test 5: Both parties approve the revised proposal.
            assertTrue(manager.approveAsContractor(orderId, contractorId));

            order = manager.getChangeOrder(orderId);
            assertEquals("PENDING_APPROVAL", order.getStatus());

            assertTrue(manager.approveAsClient(orderId, clientId));

            order = manager.getChangeOrder(orderId);
            assertEquals("APPROVED", order.getStatus());
            assertTrue(order.isClientApproved());
            assertTrue(order.isContractorApproved());

            // Approved orders cannot be revised or declined.
            assertFalse(manager.proposeChange(
                    orderId, "Another proposal", 500, 1
            ));
            assertFalse(manager.declineChangeOrder(orderId, clientId));

            // Test 6: A separate pending request can be declined.
            String declinedId = manager.createChangeOrder(
                    projectId,
                    "CONTRACTOR",
                    "Change the kitchen flooring"
            );

            assertNotNull(declinedId);
            assertTrue(manager.declineChangeOrder(declinedId, clientId));

            ChangeOrder declined = manager.getChangeOrder(declinedId);
            assertEquals("DECLINED", declined.getStatus());

        } finally {
            if (oldUrl == null) {
                System.clearProperty("andracorp.database.url");
            } else {
                System.setProperty("andracorp.database.url", oldUrl);
            }
        }
    }
}
