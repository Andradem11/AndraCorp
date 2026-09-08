package storageapi;

public interface StorageAPI {
//	Stores user information.
	boolean saveUser(
            String userId,
            String userData);
	
//	Retrieves user information.
	String getUser(String userId);
	
//	Stores project information.
	boolean saveProject(
            String projectId,
            String projectData);
	
//	Retrieves project information.
	String getProject(String projectId);
	
//	Stores worker information.
	boolean saveWorker(
            String workerId,
            String workerData);
	
//	Stores work-hour information.
	boolean saveWorkHours(
            String workerId,
            double hours);
	
//	Stores material information.
	boolean saveMaterial(
            String materialId,
            String materialData);
	
//	Stores equipment information.
	boolean saveEquipment(
            String equipmentId,
            String equipmentData);
	
//	Stores project expense information.
	boolean saveExpense(
            String expenseId,
            double amount);
	
//	Stores change order information.
	boolean saveChangeOrder(
            String changeOrderId,
            String changeOrderData);
	
//	Stores schedule or task information.
	boolean saveScheduleTask(
            String taskId,
            String taskData);
	
//	Stores daily report information.
	boolean saveDailyReport(
            String reportId,
            String reportData);
	
//	Stores the location of a project photo.
	boolean savePhoto(
            String photoId,
            String photoLocation);
	
//	Stores payment information.
	boolean savePayment(
            String paymentId,
            double amount);
	
//	Stores an action in the system audit log.
	boolean saveAuditLog(
            String userId,
            String action);
}
