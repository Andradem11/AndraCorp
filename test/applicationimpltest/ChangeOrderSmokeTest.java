package applicationimpltest;

import model.ChangeOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import applicationimpl.ChangeOrderImpl;
import storageimpl.DatabaseManager;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Change Order smoke tests. Each test uses its own temporary SQLite database.
 */
public class ChangeOrderSmokeTest {

    @TempDir
    Path temporaryDirectory;

    private ChangeOrderImpl changeOrders;

    @BeforeEach
    void setUp() throws Exception {
        Path databaseFile = temporaryDirectory.resolve("change-order-test.db");
        System.setProperty("andracorp.database.url", "jdbc:sqlite:" + databaseFile);
        DatabaseManager.initializeDatabase();
        changeOrders = new ChangeOrderImpl();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO projects (project_id, contractor_id, client_id,
                                           project_name, status, progress, budget)
                     VALUES (?, ?, ?, ?, ?, ?, ?)
                     """)) {
            statement.setString(1, "project-1");
            statement.setString(2, "contractor-1");
            statement.setString(3, "client-1");
            statement.setString(4, "Bathroom Remodel");
            statement.setString(5, "ACTIVE");
            statement.setDouble(6, 50);
            statement.setDouble(7, 27000);
            statement.executeUpdate();
        }
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("andracorp.database.url");
    }

    @Test
    void clientRequestSimulationAndApproval() {
        String id = changeOrders.createChangeOrder("project-1", "CLIENT", "Change floor tile");
        assertNotNull(id);
        assertEquals("PENDING_REVIEW", changeOrders.getChangeOrder(id).getStatus());

        assertTrue(changeOrders.simulateClientChange(id, 400, 250, 100));
        ChangeOrder simulated = changeOrders.getChangeOrder(id);
        assertEquals(750, simulated.getSimulatedCostImpact(), 0.001);
        assertEquals("PENDING_REVIEW", simulated.getStatus());

        assertTrue(changeOrders.submitContractorProposal(
                id, "contractor-1", "Use upgraded tile", 900, 2));
        ChangeOrder proposed = changeOrders.getChangeOrder(id);
        assertEquals("PENDING_APPROVAL", proposed.getStatus());
        assertEquals(900, proposed.getFinalCostImpact(), 0.001);
        assertEquals(750, proposed.getSimulatedCostImpact(), 0.001);

        assertTrue(changeOrders.approveAsClient(id, "client-1"));
        assertEquals("APPROVED", changeOrders.getChangeOrder(id).getStatus());
        assertFalse(changeOrders.approveAsClient(id, "client-1"));
        assertEquals(27000, getBudget(), 0.001);
    }

    @Test
    void contractorRequestCanBeDeclinedWithReason() {
        String id = changeOrders.createChangeOrder("project-1", "CONTRACTOR", "Replace damaged framing");
        assertNotNull(id);
        assertFalse(changeOrders.simulateClientChange(id, 200, 100, 0));
        assertTrue(changeOrders.submitContractorProposal(
                id, "contractor-1", "Replace damaged studs", 650, 1));
        assertTrue(changeOrders.declineAsClient(id, "client-1", "Please offer another option"));
        ChangeOrder declined = changeOrders.getChangeOrder(id);
        assertEquals("DECLINED", declined.getStatus());
        assertEquals("Please offer another option", declined.getDeclineReason());
        assertFalse(changeOrders.approveAsClient(id, "client-1"));
        assertEquals(27000, getBudget(), 0.001);
    }

    @Test
    void wrongUserCannotSubmitOrApprove() {
        String id = changeOrders.createChangeOrder("project-1", "CLIENT", "Add lighting");
        assertNotNull(id);
        assertFalse(changeOrders.submitContractorProposal(id, "other-contractor", "Add lights", 500, 1));
        assertTrue(changeOrders.submitContractorProposal(id, "contractor-1", "Add lights", 500, 1));
        assertFalse(changeOrders.approveAsClient(id, "other-client"));
        assertFalse(changeOrders.declineAsClient(id, "other-client", "No"));
        assertEquals("PENDING_APPROVAL", changeOrders.getChangeOrder(id).getStatus());
    }

    @Test
    void negativeCostSimulationAndInvalidInputs() {
        String id = changeOrders.createChangeOrder("project-1", "CLIENT", "Remove unused equipment");
        assertNotNull(id);
        assertTrue(changeOrders.simulateClientChange(id, -100, -50, -25));
        assertEquals(-175, changeOrders.getChangeOrder(id).getSimulatedCostImpact(), 0.001);
        assertFalse(changeOrders.simulateClientChange(id, Double.NaN, 0, 0));
        assertFalse(changeOrders.simulateClientChange(id, Double.POSITIVE_INFINITY, 0, 0));
        assertNull(changeOrders.createChangeOrder("missing-project", "CLIENT", "Change tile"));
        assertNull(changeOrders.createChangeOrder("project-1", "INVALID", "Change tile"));
    }

    @Test
    void pendingOrdersCanBeListedAndCannotBeApprovedEarly() {
        String id = changeOrders.createChangeOrder("project-1", "CLIENT", "Move a wall");
        assertNotNull(id);
        assertFalse(changeOrders.approveAsClient(id, "client-1"));
        assertFalse(changeOrders.declineAsClient(id, "client-1", "Not yet"));
        assertEquals(1, changeOrders.getProjectChangeOrders("project-1").size());
        assertTrue(changeOrders.declineChangeOrder(id, "contractor-1"));
        assertEquals("DECLINED", changeOrders.getChangeOrder(id).getStatus());
        assertFalse(changeOrders.submitContractorProposal(id, "contractor-1", "New plan", 100, 1));
    }

    private double getBudget() {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT budget FROM projects WHERE project_id = ?")) {
            statement.setString(1, "project-1");
            try (var result = statement.executeQuery()) {
                assertTrue(result.next());
                return result.getDouble("budget");
            }
        } catch (Exception exception) {
            throw new AssertionError("Could not read project budget", exception);
        }
    }
}
