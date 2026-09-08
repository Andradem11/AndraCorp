package computeapi;
/*
 * The Job Manager manages computation requests,
 * while the ComputationComponent performs the calculations
 * requested through the Compute Engine.
 */

public interface ComputationComponent {
//	Determines how the project schedule changes.
	ScheduleImpact calculateScheduleImpact(
			ProjectData project);
	
//	Calculates the cost impact of a proposed.
	CostImpact calculateCostImpact(
	        ProjectData project);
	
//	Simulates the effect of moving workers.
	ReallocationResult simulateReallocation(
	        ProjectData project,
	        ReallocationRequest request);
	
//	Estimates possible project delays.
	DelayForecast forecastDelay(
	        ProjectData project
	    );
}
