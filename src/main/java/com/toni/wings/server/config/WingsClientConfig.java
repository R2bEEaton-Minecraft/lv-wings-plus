package com.toni.wings.server.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class WingsClientConfig {
    public static final double CREATIVE_HOVER_FLAP_RATE_MIN = 0.0D;
    public static final double CREATIVE_HOVER_FLAP_RATE_MAX = 2.0D;
    private static final double CREATIVE_HOVER_FLAP_RATE_DEFAULT = 0.67D;

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue ENABLE_POSE_PREVIEW;
    private static final ForgeConfigSpec.DoubleValue CREATIVE_HOVER_FLAP_RATE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Client configuration for lv wings").push("client");

        ENABLE_POSE_PREVIEW = builder
            .comment("Show the player pose preview overlay when cycling flight poses.")
            .define("enablePosePreview", false);

        CREATIVE_HOVER_FLAP_RATE = builder
            .comment("Creative mode wing flapping speed (0.00 to 2.00).")
            .defineInRange("creativeHoverFlapRate",
                CREATIVE_HOVER_FLAP_RATE_DEFAULT,
                CREATIVE_HOVER_FLAP_RATE_MIN,
                CREATIVE_HOVER_FLAP_RATE_MAX
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

    public static double getCreativeHoverFlapRate() {
        return CREATIVE_HOVER_FLAP_RATE.get();
    }

    public static void setCreativeHoverFlapRate(double flapRate) {
        CREATIVE_HOVER_FLAP_RATE.set(clampCreativeHoverFlapRate(flapRate));
        CREATIVE_HOVER_FLAP_RATE.save();
    }

    public static double clampCreativeHoverFlapRate(double flapRate) {
        return Math.max(CREATIVE_HOVER_FLAP_RATE_MIN, Math.min(CREATIVE_HOVER_FLAP_RATE_MAX, flapRate));
    }

    public static double normalizeCreativeHoverFlapRate(double flapRate) {
        double clamped = clampCreativeHoverFlapRate(flapRate);
        return (clamped - CREATIVE_HOVER_FLAP_RATE_MIN) /
            (CREATIVE_HOVER_FLAP_RATE_MAX - CREATIVE_HOVER_FLAP_RATE_MIN);
    }

    public static double denormalizeCreativeHoverFlapRate(double sliderValue) {
        double clampedSlider = Math.max(0.0D, Math.min(1.0D, sliderValue));
        return CREATIVE_HOVER_FLAP_RATE_MIN + clampedSlider *
            (CREATIVE_HOVER_FLAP_RATE_MAX - CREATIVE_HOVER_FLAP_RATE_MIN);
    }

    public static void validate() {
        isPosePreviewEnabled();
        getCreativeHoverFlapRate();
    }
}
