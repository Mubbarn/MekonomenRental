package mekonomen.policy;

import mekonomen.model.Member;

public class StandardPolicy implements PricePolicy {
    @Override public String getName() { return "Standard"; }

    @Override
    public double calculateTotal(Member member, double basePricePerDay, int days) {
        return basePricePerDay * days;
    }
}