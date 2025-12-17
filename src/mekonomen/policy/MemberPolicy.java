package mekonomen.policy;

import mekonomen.model.Member;

public class MemberPolicy implements PricePolicy {
    @Override public String getName() { return "Medlem (20% rabatt)"; }

    @Override
    public double calculateTotal(Member member, double basePricePerDay, int days) {
        double total = basePricePerDay * days;
        return total * 0.80;
    }
}
