package computemodel;

public class ScheduleImpact {

    private int originalDays;
    private int estimatedDays;
    private int daysChanged;

    public ScheduleImpact(int originalDays, int estimatedDays, int daysChanged) {
        this.originalDays = originalDays;
        this.estimatedDays = estimatedDays;
        this.daysChanged = daysChanged;
    }

//  get method
    public int getOriginalDays() {
        return originalDays;
    }
    public int getEstimatedDays() {
        return estimatedDays;
    }
    public int getDaysChanged() {
        return daysChanged;
    }
}
