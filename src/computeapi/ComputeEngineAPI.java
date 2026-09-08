package computeapi;
/*
 * The Compute Engine analyzes project information 
 * and produces estimates or simulations related to:
 * - Project scheduling
 * - Project costs
 * - Worker reallocation
 * - Possible project delays
 */
public interface ComputeEngineAPI {
//	Calculates how changing the number of workers affects completion date
	ScheduleImpact calculateScheduleImpact(
	        String projectId,
	        String taskId,
	        int workerChange);
	
//	Calculates additional labor costs caused by adding workers to a task.
	CostImpact calculateCostImpact(
	        String projectId,
	        String taskId,
	        int additionalWorkers);
	
//	Simulates moving existing workers from one project task to another.
	ReallocationResult simulateWorkerReallocation(
	        String projectId,
	        String sourceTaskId,
	        String targetTaskId,
	        int numberOfWorkers);
	
//	estimates whether the current project schedule may experience delays.
	DelayForecast forecastDelay(
	        String projectId);
}
