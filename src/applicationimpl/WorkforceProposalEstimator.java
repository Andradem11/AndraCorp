package applicationimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Estimates additional labor without recording expenses or changing assignments. */
public final class WorkforceProposalEstimator {
    private WorkforceProposalEstimator() {}

    public static double calculate(int workers, double hoursPerWorker, double hourlyRate) {
        if (workers < 1 || workers > 1000 || !Double.isFinite(hoursPerWorker)
                || hoursPerWorker <= 0 || !Double.isFinite(hourlyRate) || hourlyRate < 0) {
            throw new IllegalArgumentException("Invalid workforce estimate inputs.");
        }
        double total = workers * hoursPerWorker * hourlyRate;
        if (!Double.isFinite(total) || total > 1_000_000_000) {
            throw new IllegalArgumentException("Estimated cost exceeds the supported range.");
        }
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
