package server;

/*
 * Represents worker information received
 * from the React frontend.
 */
public class WorkerRequest {

    private String projectId;
    private String workerName;
    private String trade;


//  Empty constructor required for JSON conversion.
    public WorkerRequest() {
    }


//  Returns the project the worker belongs to.
    public String getProjectId() {
        return projectId;
    }


//  Updates the worker's project.
    public void setProjectId(
            String projectId) {

        this.projectId =
                projectId;
    }


//  Returns the worker's name.
    public String getWorkerName() {
        return workerName;
    }


//  Updates the worker's name.
    public void setWorkerName(
            String workerName) {

        this.workerName =
                workerName;
    }


//  Returns the worker's trade.
    public String getTrade() {
        return trade;
    }


//  Updates the worker's trade.
    public void setTrade(
            String trade) {

        this.trade =
                trade;
    }
}
