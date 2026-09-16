package computemodel;

public class CostImpact {

    private double originalCost;
    private double estimatedCost;
    private double additionalCost;

    public CostImpact(double originalCost, double estimatedCost, double additionalCost) {
        this.originalCost = originalCost;
        this.estimatedCost = estimatedCost;
        this.additionalCost = additionalCost;
    }

//  get method
    public double getOriginalCost() {
        return originalCost;
    }
    public double getEstimatedCost() {
        return estimatedCost;
    }
    public double getAdditionalCost() {
        return additionalCost;
    }
}
