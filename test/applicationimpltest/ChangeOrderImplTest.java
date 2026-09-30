package applicationimpltest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import applicationimpl.ChangeOrderImpl;
import storageimpl.DatabaseManager;

/*
 * Tests change order management
 * using the SQLite database.
 */
public class ChangeOrderImplTest {

    private ChangeOrderImpl changeOrderManager;

//  Creates the database and change order manager
//  before each test.
    @BeforeEach
    public void setUp() {

        DatabaseManager.initializeDatabase();

        changeOrderManager =
                new ChangeOrderImpl();
    }

//  Tests creating and retrieving a change order.
    @Test
    public void testCreateChangeOrder() {

        String changeOrderId =
                changeOrderManager.createChangeOrder(
                        "project1",
                        "Add new kitchen cabinets");

        assertNotNull(changeOrderId);

        String changeOrder =
                changeOrderManager.getChangeOrder(
                        changeOrderId);

        assertNotNull(changeOrder);

        assertTrue(
                changeOrder.contains(
                        "Add new kitchen cabinets"));

        assertTrue(
                changeOrder.contains("PENDING"));
    }

//  Tests a client requesting a project change.
    @Test
    public void testClientRequest() {

        String changeOrderId =
                changeOrderManager.requestChange(
                        "project2",
                        "client1",
                        "Finish kitchen earlier");

        assertNotNull(changeOrderId);

        String changeOrder =
                changeOrderManager.getChangeOrder(
                        changeOrderId);

        assertTrue(
                changeOrder.contains(
                        "Finish kitchen earlier"));
    }

//  Tests approving a client change request.
    @Test
    public void testApproveChangeOrder() {

        String changeOrderId =
                changeOrderManager.requestChange(
                        "project3",
                        "client2",
                        "Add another worker");

        boolean approved =
                changeOrderManager.approveChangeOrder(
                        changeOrderId,
                        "client2");

        assertTrue(approved);

        String changeOrder =
                changeOrderManager.getChangeOrder(
                        changeOrderId);

        assertTrue(
                changeOrder.contains("APPROVED"));
    }

//  Tests declining a client change request.
    @Test
    public void testDeclineChangeOrder() {

        String changeOrderId =
                changeOrderManager.requestChange(
                        "project4",
                        "client3",
                        "Change flooring material");

        boolean declined =
                changeOrderManager.declineChangeOrder(
                        changeOrderId,
                        "client3");

        assertTrue(declined);

        String changeOrder =
                changeOrderManager.getChangeOrder(
                        changeOrderId);

        assertTrue(
                changeOrder.contains("DECLINED"));
    }

//  Tests that the wrong client cannot approve
//  another client's change request.
    @Test
    public void testWrongClientCannotApprove() {

        String changeOrderId =
                changeOrderManager.requestChange(
                        "project5",
                        "client4",
                        "Add more lighting");

        boolean approved =
                changeOrderManager.approveChangeOrder(
                        changeOrderId,
                        "wrongClient");

        assertFalse(approved);

        String changeOrder =
                changeOrderManager.getChangeOrder(
                        changeOrderId);

        assertTrue(
                changeOrder.contains("PENDING"));
    }
}
