package applicationimpltest;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import applicationimpl.ChangeOrderImpl;
import model.ChangeOrder;
import storageimpl.DatabaseManager;

public class ChangeOrderNegotiationSmokeTest {

    private Path testDatabase;
    private ChangeOrderImpl manager;

    @BeforeEach
    void setUp() throws Exception {
        testDatabase = Files.createTempFile("andracorp-negotiation-", ".db");
        System.setProperty("andracorp.database.url", "jdbc:sqlite:" + testDatabase);
        DatabaseManager.initializeDatabase();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO projects (
                         project_id, contractor_id, client_id,
                         project_name, status, progress, budget
                     ) VALUES ('project-1', 'contractor-1', 'client-1',
                               'Bathroom Remodel', 'ACTIVE', 0, 27000)
                     """)) {
            statement.executeUpdate();
        }
        manager = new ChangeOrderImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        System.clearProperty("andracorp.database.url");
        if (testDatabase != null) {
            Files.deleteIfExists(testDatabase);
        }
    }

    private String newProposal() {
        String id = manager.createChangeOrder(
                "project-1", "CONTRACTOR", "Replace 4-inch lights");
        assertNotNull(id);
        assertTrue(manager.submitContractorProposal(
                id, "contractor-1", "Use 5-inch lights", 100, -2));
        return id;
    }

    @Test
    void counterproposalResetsApprovalsAndAllowsRevisedProposal() {
        String id = newProposal();
        ChangeOrder before = manager.getChangeOrder(id);
        assertTrue(before.isContractorApproved());
        assertFalse(before.isClientApproved());

        assertTrue(manager.submitClientCounterproposal(
                id, "client-1", "Use these other 4-inch lights"));
        ChangeOrder counter = manager.getChangeOrder(id);
        assertEquals("COUNTERPROPOSAL_PENDING", counter.getStatus());
        assertFalse(counter.isContractorApproved());
        assertFalse(counter.isClientApproved());
        assertEquals(before.getProposalVersion() + 1, counter.getProposalVersion());
        assertFalse(manager.approveAsClient(id, "client-1"));

        assertTrue(manager.submitContractorProposal(
                id, "contractor-1", "Install client-selected lights", 250, 1));
        ChangeOrder revised = manager.getChangeOrder(id);
        assertEquals("PENDING_APPROVAL", revised.getStatus());
        assertTrue(revised.isContractorApproved());
        assertFalse(revised.isClientApproved());
        assertEquals(counter.getProposalVersion() + 1, revised.getProposalVersion());
        assertTrue(manager.approveAsClient(id, "client-1"));
        assertEquals("APPROVED", manager.getChangeOrder(id).getStatus());
        assertFalse(manager.approveAsClient(id, "client-1"));
    }

    @Test
    void negotiationHistoryKeepsEventsInOrder() {
        String id = newProposal();
        assertTrue(manager.submitClientCounterproposal(
                id, "client-1", "Can we use different lights?"));
        assertTrue(manager.submitContractorProposal(
                id, "contractor-1", "Alternative lights", -50, 0));
        assertTrue(manager.approveAsClient(id, "client-1"));

        List<Map<String, Object>> history = manager.getChangeOrderHistory(id);
        assertEquals(4, history.size());
        assertEquals("PROPOSAL", history.get(0).get("action"));
        assertEquals("COUNTERPROPOSAL", history.get(1).get("action"));
        assertEquals("Can we use different lights?", history.get(1).get("message"));
        assertEquals("PROPOSAL", history.get(2).get("action"));
        assertEquals("APPROVED", history.get(3).get("action"));
        assertEquals("CLIENT", history.get(3).get("actorRole"));
    }

    @Test
    void unauthorizedClientCannotCounterpropose() {
        String id = newProposal();
        assertFalse(manager.submitClientCounterproposal(
                id, "another-client", "Different lights"));
        assertEquals("PENDING_APPROVAL", manager.getChangeOrder(id).getStatus());
        assertEquals(1, manager.getChangeOrderHistory(id).size());
    }

    @Test
    void invalidCounterproposalsDoNotChangeHistory() {
        String id = newProposal();
        assertFalse(manager.submitClientCounterproposal(id, "client-1", "  "));
        assertFalse(manager.submitClientCounterproposal("missing", "client-1", "Alternative"));
        assertEquals(1, manager.getChangeOrderHistory(id).size());
        assertEquals("PENDING_APPROVAL", manager.getChangeOrder(id).getStatus());
    }

    @Test
    void declineIsRecordedAndPreventsFurtherApproval() {
        String id = newProposal();
        assertTrue(manager.declineAsClient(id, "client-1", "Keep the original lights"));
        assertEquals("DECLINED", manager.getChangeOrder(id).getStatus());
        assertFalse(manager.approveAsClient(id, "client-1"));
        assertFalse(manager.submitClientCounterproposal(id, "client-1", "Another option"));
        List<Map<String, Object>> history = manager.getChangeOrderHistory(id);
        assertEquals(2, history.size());
        assertEquals("DECLINED", history.get(1).get("action"));
        assertEquals("Keep the original lights", history.get(1).get("message"));
    }
}
