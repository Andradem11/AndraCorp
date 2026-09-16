package computemodel;

public class ReallocationResult {

    private String sourceTaskId;
    private String targetTaskId;
    private int workersMoved;
    private int sourceDelayDays;
    private int targetDaysSaved;

    public ReallocationResult(String sourceTaskId, String targetTaskId, int workersMoved,
                              int sourceDelayDays, int targetDaysSaved) {
        this.sourceTaskId = sourceTaskId;
        this.targetTaskId = targetTaskId;
        this.workersMoved = workersMoved;
        this.sourceDelayDays = sourceDelayDays;
        this.targetDaysSaved = targetDaysSaved;
    }
    
//  get method
    public String getSourceTaskId() {
        return sourceTaskId;
    }
    public String getTargetTaskId() {
        return targetTaskId;
    }
    public int getWorkersMoved() {
        return workersMoved;
    }
    public int getSourceDelayDays() {
        return sourceDelayDays;
    }
    public int getTargetDaysSaved() {
        return targetDaysSaved;
    }
}
