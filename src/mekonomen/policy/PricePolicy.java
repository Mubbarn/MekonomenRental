package mekonomen.policy;

import mekonomen.model.Member;

public interface PricePolicy {
    String getName();
    double calculateTotal(Member member, double basePricePerDay, int days);
}