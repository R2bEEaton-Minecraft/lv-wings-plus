package com.toni.wings.server.flight;

import java.util.Locale;

public enum FlightPose {
    DEFAULT("default"),
    MAIN_HAND_FORWARD("main_hand_forward"),
    HANDS_AT_SIDES("hands_at_sides"),
    HANDS_AT_SIDES_OUT("hands_at_sides_out");

    private final String id;

    FlightPose(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public FlightPose next() {
        FlightPose[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public static FlightPose byId(String id) {
        if (id == null || id.isEmpty()) {
            return DEFAULT;
        }
        String key = id.toLowerCase(Locale.ROOT);
        for (FlightPose pose : values()) {
            if (pose.id.equals(key)) {
                return pose;
            }
        }
        return DEFAULT;
    }

    public static FlightPose byOrdinal(int ordinal) {
        FlightPose[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return DEFAULT;
        }
        return values[ordinal];
    }
}
