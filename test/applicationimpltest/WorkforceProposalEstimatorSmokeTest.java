package applicationimpltest;

import applicationimpl.WorkforceProposalEstimator;

public class WorkforceProposalEstimatorSmokeTest {
    private static void check(boolean ok, String name) {
        if (!ok) throw new AssertionError(name);
        System.out.println("PASS: " + name);
    }
    public static void main(String[] args) {
        check(WorkforceProposalEstimator.calculate(2, 20, 60) == 2400, "two workers, twenty hours");
        check(WorkforceProposalEstimator.calculate(3, 1.5, 95) == 427.5, "fractional hours");
        check(WorkforceProposalEstimator.calculate(1, 1, 0) == 0, "zero hourly rate");
        for (double hours : new double[]{-1, 0, Double.NaN, Double.POSITIVE_INFINITY}) {
            try { WorkforceProposalEstimator.calculate(2, hours, 60); throw new AssertionError("Expected invalid hours"); }
            catch (IllegalArgumentException expected) { /* correct */ }
        }
        check(true, "invalid hours rejected");
        try { WorkforceProposalEstimator.calculate(0, 20, 60); throw new AssertionError("Expected invalid count"); }
        catch (IllegalArgumentException expected) { check(true, "invalid worker count rejected"); }
    }
}
