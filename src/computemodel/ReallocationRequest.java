package computemodel;

public class ReallocationRequest {

    private String sourceTaskId;
    private String targetTaskId;
    private int numberOfWorkers;

    public ReallocationRequest(String sourceTaskId, String targetTaskId, int numberOfWorkers) {
        this.sourceTaskId = sourceTaskId;
        this.targetTaskId = targetTaskId;
        this.numberOfWorkers = numberOfWorkers;
    }

//  get method
    public String getSourceTaskId() {
        return sourceTaskId;
    }
    public String getTargetTaskId() {
        return targetTaskId;
    }
    public int getNumberOfWorkers() {
        return numberOfWorkers;
    }
}
