package applicationapi;
/*
 * This interface is responsible for:
 * Handling workers, trades, assignments, and work hours.
 * Allowing the contractor to manage the work-force.
 */
public interface WorkforceManagementAPI {
	
//	Adds a worker to a project and records their trade.
	String addWorker(
			String projectId,
			String workerName,
			String trade);
	
//	Assigns a worker to a specific area and task.
	boolean assignWorker(
			String workerId,
			String projectId,
			String area,
			String task);
	
//	Relocating workers to another project area or task.
	boolean reassignWorker(
	        String workerId,
	        String newArea,
	        String newTask);
	
//	Records the number of hours a worker spent.
	boolean recordWorkHours(
	        String workerId,
	        String taskId,
	        double hours);
	
//	Retrieves summarized work-force information
	String getProjectWorkforce(String projectId);
}
