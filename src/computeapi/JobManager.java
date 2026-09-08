package computeapi;
/*
 * Its responsibility is to receive computation requests,
 * manage them, and return their results.
 */
public class JobManager {
//	Returns an ID that can be used to track the job.
	public String submitJob(ComputationJob job) {
        return null;
    }
	
//	Returns the current status of a computation job.
	public ComputationStatus getJobStatus(String jobId) {
        return null;
    }

//	Returns the final result of a completed job.
	public ComputationResult getResult(String jobId) {
        return null;
    }

}
