package mekonomen.service;

import mekonomen.model.Item;
import mekonomen.model.Member;
import mekonomen.model.MemberLevel;
import mekonomen.model.Rental;
import mekonomen.policy.MemberPolicy;
import mekonomen.policy.PricePolicy;
import mekonomen.policy.StandardPolicy;
import mekonomen.policy.StudentPolicy;
import mekonomen.repo.Inventory;
import mekonomen.repo.MemberRegistry;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class RentalService {
    private final Inventory inventory;
    private final MemberRegistry memberRegistry;

    private final List<Rental> rentals = new ArrayList<Rental>();

    private final PricePolicy standard = new StandardPolicy();
    private final PricePolicy student = new StudentPolicy();
    private final PricePolicy premium = new MemberPolicy();

    public RentalService(Inventory inventory, MemberRegistry memberRegistry) {
        this.inventory = inventory;
        this.memberRegistry = memberRegistry;
    }

    public List<Rental> getAllRentals() {
        return new ArrayList<Rental>(rentals);
    }

    public void setAllRentals(List<Rental> loaded) {
        rentals.clear();
        if (loaded != null) rentals.addAll(loaded);
    }

    public Rental bookRental(String memberId, String itemId, LocalDate startDate) {
        Member m = memberRegistry.findById(memberId);
        Item it = inventory.findById(itemId);

        if (m == null) throw new IllegalArgumentException("Medlem hittades inte.");
        if (it == null) throw new IllegalArgumentException("Item hittades inte.");
        if (!it.isAvailable()) throw new IllegalArgumentException("Item är inte tillgänglig.");

        Rental r = new Rental(memberId, itemId, startDate);
        rentals.add(r);
        it.setAvailable(false);

        m.addHistory(LocalDate.now() + ": Bokade " + it.getName() + " (" + it.getType() + ")");
        return r;
    }

    public double closeRental(String rentalId, LocalDate endDate) {
        Rental r = findRentalById(rentalId);
        if (r == null) throw new IllegalArgumentException("Uthyrning hittades inte.");
        if (!r.isActive()) throw new IllegalArgumentException("Uthyrningen är redan avslutad.");

        Member m = memberRegistry.findById(r.getMemberId());
        Item it = inventory.findById(r.getItemId());
        if (m == null || it == null) throw new IllegalStateException("Data saknas för uthyrningen.");

        long daysLong = ChronoUnit.DAYS.between(r.getStartDate(), endDate);
        int days = (int) Math.max(1, daysLong);

        PricePolicy policy = policyFor(m.getLevel());
        double total = policy.calculateTotal(m, it.getPricePerDay(), days);

        r.close(endDate, total);
        it.setAvailable(true);

        m.addHistory(LocalDate.now() + ": Avslutade " + it.getName() + " – " + total + " kr (" + policy.getName() + ")");
        return total;
    }

    private PricePolicy policyFor(MemberLevel level) {
        if (level == MemberLevel.STUDENT) return student;
        if (level == MemberLevel.PREMIUM) return premium;
        return standard;
    }

    public Rental findRentalById(String id) {
        for (Rental r : rentals) {
            if (r.getId().equals(id)) return r;
        }
        return null;
    }

    public int countActiveRentals() {
        int c = 0;
        for (Rental r : rentals) if (r.isActive()) c++;
        return c;
    }

    public double totalRevenue() {
        double sum = 0.0;
        for (Rental r : rentals) {
            if (!r.isActive()) sum += r.getTotalPrice();
        }
        return sum;
    }
}