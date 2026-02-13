package com.toni.wings.server.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class WingsClientConfig {
    public static final double CREATIVE_FLAPPING_SPEED_MIN = 0.25D;
    public static final double CREATIVE_FLAPPING_SPEED_MAX = 2.0D;
    private static final double CREATIVE_FLAPPING_SPEED_DEFAULT = 1.0D;

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue ENABLE_POSE_PREVIEW;
    private static final ForgeConfigSpec.DoubleValue CREATIVE_FLAPPING_SPEED_MULTIPLIER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Client configuration for lv wings").push("client");

        ENABLE_POSE_PREVIEW = builder
            .comment("Show the player pose preview overlay when cycling flight poses.")
            .define("enablePosePreview", false);

        CREATIVE_FLAPPING_SPEED_MULTIPLIER = builder
            .comment("Creative mode wing flapping speed multiplier.")
            .defineInRange("creativeFlappingSpeedMultiplier",
                CREATIVE_FLAPPING_SPEED_DEFAULT,
                CREATIVE_FLAPPING_SPEED_MIN,
                CREATIVE_FLAPPING_SPEED_MAX
            );

        builder.pop();
        SPEC = builder.build();
    }

    private WingsClientConfig() {
    }

    public static boolean isPosePreviewEnabled() {
        return ENABLE_POSE_PREVIEW.get();
    }

    public static void setPosePreviewEnabled(boolean enabled) {
        ENABLE_POSE_PREVIEW.set(enabled);
        ENABLE_POSE_PREVIEW.save();
    }

    public static double getCreativeFlappingSpeedMultiplier() {
        return CREATIVE_FLAPPING_SPEED_MULTIPLIER.get();
    }

    public static void setCreativeFlappingSpeedMultiplier(double multiplier) {
        CREATIVE_FLAPPING_SPEED_MULTIPLIER.set(clampCreativeFlappingSpeed(multiplier));
        CREATIVE_FLAPPING_SPEED_MULTIPLIER.save();
    }

    public static double clampCreativeFlappingSpeed(double multiplier) {
        return Math.max(CREATIVE_FLAPPING_SPEED_MIN, Math.min(CREATIVE_FLAPPING_SPEED_MAX, multiplier));
    }

    public static double normalizeCreativeFlappingSpeed(double multiplier) {
        double clamped = clampCreativeFlappingSpeed(multiplier);
        return (clamped - CREATIVE_FLAPPING_SPEED_MIN) / (CREATIVE_FLAPPING_SPEED_MAX - CREATIVE_FLAPPING_SPEED_MIN);
    }

    public static double denormalizeCreativeFlappingSpeed(double sliderValue) {
        double clampedSlider = Math.max(0.0D, Math.min(1.0D, sliderValue));
        return CREATIVE_FLAPPING_SPEED_MIN + clampedSlider * (CREATIVE_FLAPPING_SPEED_MAX - CREATIVE_FLAPPING_SPEED_MIN);
    }

    public static void validate() {
        isPosePreviewEnabled();
        getCreativeFlappingSpeedMultiplier();
    }
}
