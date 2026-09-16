package computemodel;

public class ComputationResult {

    private String jobId;
    private boolean successful;
    private String result;

    public ComputationResult(String jobId, boolean successful, String result) {
        this.jobId = jobId;
        this.successful = successful;
        this.result = result;
    }

//  get method
    public String getJobId() {
        return jobId;
    }
    public boolean isSuccessful() {
        return successful;
    }
    public String getResult() {
        return result;
    }
}
