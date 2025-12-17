package mekonomen.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Rental implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String memberId;
    private final String itemId;
    private final LocalDate startDate;
    private LocalDate endDate;
    private double totalPrice;

    public Rental(String memberId, String itemId, LocalDate startDate) {
        this(UUID.randomUUID().toString(), memberId, itemId, startDate, null, 0.0);
    }

    public Rental(String id, String memberId, String itemId, LocalDate startDate, LocalDate endDate, double totalPrice) {
        this.id = Objects.requireNonNull(id);
        this.memberId = Objects.requireNonNull(memberId);
        this.itemId = Objects.requireNonNull(itemId);
        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = endDate;
        this.totalPrice = totalPrice;
    }

    public String getId() { return id; }
    public String getMemberId() { return memberId; }
    public String getItemId() { return itemId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isActive() { return endDate == null; }
    public double getTotalPrice() { return totalPrice; }

    public void close(LocalDate endDate, double totalPrice) {
        this.endDate = Objects.requireNonNull(endDate);
        this.totalPrice = totalPrice;
    }
}