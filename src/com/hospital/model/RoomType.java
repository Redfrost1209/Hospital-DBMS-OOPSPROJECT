package com.hospital.model;

/**
 * Enum demonstrating Java type-safety, encapsulation of constant attributes,
 * and custom methods inside Enums.
 */
public enum RoomType {
    GENERAL("General Ward", 500.0),
    SEMI_PRIVATE("Semi-Private Room", 1200.0),
    PRIVATE("Private Deluxe Room", 2500.0),
    ICU("Intensive Care Unit (ICU)", 5000.0);

    private final String displayName;
    private final double defaultDailyRate;

    RoomType(String displayName, double defaultDailyRate) {
        this.displayName = displayName;
        this.defaultDailyRate = defaultDailyRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDefaultDailyRate() {
        return defaultDailyRate;
    }

    public static RoomType fromString(String text) {
        for (RoomType rt : RoomType.values()) {
            if (rt.name().equalsIgnoreCase(text) || rt.displayName.equalsIgnoreCase(text)) {
                return rt;
            }
        }
        return GENERAL;
    }
}
