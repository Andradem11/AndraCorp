package computemodel;

public class DelayForecast {

    private boolean delayExpected;
    private int estimatedDelayDays;

    public DelayForecast(boolean delayExpected, int estimatedDelayDays) {
        this.delayExpected = delayExpected;
        this.estimatedDelayDays = estimatedDelayDays;
    }

    
    public boolean isDelayExpected() {
        return delayExpected;
    }
    public int getEstimatedDelayDays() {
        return estimatedDelayDays;
    }
}
