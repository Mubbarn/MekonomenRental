package mekonomen.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public abstract class Item implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;

    private String name;
    private double pricePerDay;
    private boolean available = true;

    protected Item(String name, double pricePerDay) {
        this(UUID.randomUUID().toString(), name, pricePerDay, true);
    }

    protected Item(String id, String name, double pricePerDay, boolean available) {
        this.id = Objects.requireNonNull(id);
        this.name = name;
        this.pricePerDay = pricePerDay;
        this.available = available;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPricePerDay() { return pricePerDay; }
    public boolean isAvailable() { return available; }

    public void setName(String name) { this.name = name; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }
    public void setAvailable(boolean available) { this.available = available; }

    public abstract String getType();
    public abstract String getDetails();
}