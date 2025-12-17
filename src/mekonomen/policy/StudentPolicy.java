package mekonomen.policy;

import mekonomen.model.Member;

public class StudentPolicy implements PricePolicy {
    @Override public String getName() { return "Student (10% rabatt)"; }

    @Override
    public double calculateTotal(Member member, double basePricePerDay, int days) {
        double total = basePricePerDay * days;
        return total * 0.90;
    }
}