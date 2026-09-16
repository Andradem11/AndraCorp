package computemodel;

public class ComputationJob {

    private String jobId;
    private String projectId;
    private String computationType;

    public ComputationJob(String jobId, String projectId, String computationType) {
        this.jobId = jobId;
        this.projectId = projectId;
        this.computationType = computationType;
    }
    
//  get method
    public String getJobId() {
        return jobId;
    }
    public String getProjectId() {
        return projectId;
    }
    public String getComputationType() {
        return computationType;
    }
}
