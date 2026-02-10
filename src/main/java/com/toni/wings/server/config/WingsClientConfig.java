package com.toni.wings.server.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class WingsClientConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue ENABLE_POSE_PREVIEW;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Client configuration for lv wings").push("client");

        ENABLE_POSE_PREVIEW = builder
            .comment("Show the player pose preview overlay when cycling flight poses.")
            .define("enablePosePreview", false);

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

    public static void validate() {
        isPosePreviewEnabled();
    }
}
