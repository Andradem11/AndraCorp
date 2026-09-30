package storageimpltest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import storageimpl.DatabaseManager;
import storageimpl.StorageImpl;

/*
 * Tests the SQLite implementation
 * of the AndraCorp Storage API.
 */
public class StorageImplTest {

    private StorageImpl storage;

//  Creates the database and storage component
//  before each test.
    @BeforeEach
    public void setUp() {

        DatabaseManager.initializeDatabase();

        storage =
                new StorageImpl();
    }

//  Tests saving and retrieving user data.
    @Test
    public void testSaveUser() {

        boolean saved =
                storage.saveUser(
                        "storage-user-1",
                        "Test User");

        assertTrue(saved);

        String user =
                storage.getUser(
                        "storage-user-1");

        assertEquals(
                "Test User",
                user);
    }

//  Tests saving and retrieving project data.
    @Test
    public void testSaveProject() {

        boolean saved =
                storage.saveProject(
                        "storage-project-1",
                        "Kitchen Renovation");

        assertTrue(saved);

        String project =
                storage.getProject(
                        "storage-project-1");

        assertEquals(
                "Kitchen Renovation",
                project);
    }

//  Tests saving worker and work hour data.
    @Test
    public void testSaveWorkerAndHours() {

        boolean workerSaved =
                storage.saveWorker(
                        "storage-worker-1",
                        "Test Worker");

        assertTrue(workerSaved);

        boolean hoursSaved =
                storage.saveWorkHours(
                        "storage-worker-1",
                        8.0);

        assertTrue(hoursSaved);
    }

//  Tests saving material and equipment data.
    @Test
    public void testSaveMaterialAndEquipment() {

        boolean materialSaved =
                storage.saveMaterial(
                        "storage-material-1",
                        "Lumber");

        boolean equipmentSaved =
                storage.saveEquipment(
                        "storage-equipment-1",
                        "Excavator");

        assertTrue(materialSaved);
        assertTrue(equipmentSaved);
    }

//  Tests saving expense and change order data.
    @Test
    public void testSaveExpenseAndChangeOrder() {

        boolean expenseSaved =
                storage.saveExpense(
                        "storage-expense-1",
                        1500);

        boolean changeOrderSaved =
                storage.saveChangeOrder(
                        "storage-change-1",
                        "Add kitchen cabinets");

        assertTrue(expenseSaved);
        assertTrue(changeOrderSaved);
    }

//  Tests saving schedule and report data.
    @Test
    public void testSaveScheduleAndReport() {

        boolean taskSaved =
                storage.saveScheduleTask(
                        "storage-task-1",
                        "Install Flooring");

        boolean reportSaved =
                storage.saveDailyReport(
                        "storage-report-1",
                        "Flooring work started.");

        assertTrue(taskSaved);
        assertTrue(reportSaved);
    }

//  Tests saving project photo and payment data.
    @Test
    public void testSavePhotoAndPayment() {

        boolean photoSaved =
                storage.savePhoto(
                        "storage-photo-1",
                        "photos/kitchen.jpg");

        boolean paymentSaved =
                storage.savePayment(
                        "storage-payment-1",
                        5000);

        assertTrue(photoSaved);
        assertTrue(paymentSaved);
    }

//  Tests saving an audit log.
    @Test
    public void testSaveAuditLog() {

        boolean saved =
                storage.saveAuditLog(
                        "storage-user-1",
                        "Created project");

        assertTrue(saved);
    }

//  Tests that invalid amounts are rejected.
    @Test
    public void testInvalidAmounts() {

        assertFalse(
                storage.saveExpense(
                        "invalid-expense",
                        -100));

        assertFalse(
                storage.savePayment(
                        "invalid-payment",
                        -500));

        assertFalse(
                storage.saveWorkHours(
                        "storage-worker-1",
                        -8));
    }
}