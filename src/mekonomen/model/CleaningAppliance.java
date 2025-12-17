package mekonomen.model;

import java.io.Serializable;

public class CleaningAppliance extends Item implements Serializable {
    private static final long serialVersionUID = 1L;
    private String cleaningType;
    private String notes;

    public CleaningAppliance(String name, double pricePerDay, String cleaningType, String notes) {
        super(name, pricePerDay);
        this.cleaningType = cleaningType;
        this.notes = notes;
    }

    public CleaningAppliance(String id, String name, double pricePerDay, boolean available,
                             String cleaningType, String notes) {
        super(id, name, pricePerDay, available);
        this.cleaningType = cleaningType;
        this.notes = notes;
    }

    public String getCleaningType() { return cleaningType; }
    public String getNotes() { return notes; }

    public void setCleaningType(String cleaningType) { this.cleaningType = cleaningType; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override public String getType() { return "CleaningAppliance"; }

    @Override
    public String getDetails() {
        return "Typ: " + safe(cleaningType) + ", Notering: " + safe(notes);
    }


    public boolean requiresVentilation() {
        return safe(cleaningType).toLowerCase().contains("ozon");
    }

    private String safe(String s) { return s == null ? "" : s; }
}