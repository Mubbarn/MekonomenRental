package mekonomen.model;

import java.io.Serializable;

public class CarAppliance extends Item implements Serializable {
    private static final long serialVersionUID = 1L;
    private String applianceType;
    private String compatibility;

    public CarAppliance(String name, double pricePerDay, String applianceType, String compatibility) {
        super(name, pricePerDay);
        this.applianceType = applianceType;
        this.compatibility = compatibility;
    }

    public CarAppliance(String id, String name, double pricePerDay, boolean available,
                        String applianceType, String compatibility) {
        super(id, name, pricePerDay, available);
        this.applianceType = applianceType;
        this.compatibility = compatibility;
    }

    public String getApplianceType() { return applianceType; }
    public String getCompatibility() { return compatibility; }

    public void setApplianceType(String applianceType) { this.applianceType = applianceType; }
    public void setCompatibility(String compatibility) { this.compatibility = compatibility; }

    @Override public String getType() { return "CarAppliance"; }

    @Override
    public String getDetails() {
        return "Typ: " + safe(applianceType) + ", Passar: " + safe(compatibility);
    }


    public boolean isChildSeat() {
        return safe(applianceType).toLowerCase().contains("barn");
    }

    private String safe(String s) { return s == null ? "" : s; }
}
