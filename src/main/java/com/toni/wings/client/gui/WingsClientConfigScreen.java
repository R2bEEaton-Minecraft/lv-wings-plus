package com.toni.wings.client.gui;

import com.toni.wings.server.config.WingsClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class WingsClientConfigScreen extends Screen {
    private static final double CREATIVE_FLAPPING_SPEED_STEP = 0.05D;

    private final Screen parent;
    private boolean posePreviewEnabled;
    private double creativeFlappingSpeedMultiplier;
    private Button posePreviewButton;

    public WingsClientConfigScreen(Screen parent) {
        super(Component.literal("Wings Config"));
        this.parent = parent;
        this.posePreviewEnabled = WingsClientConfig.isPosePreviewEnabled();
        this.creativeFlappingSpeedMultiplier = WingsClientConfig.getCreativeFlappingSpeedMultiplier();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(new CreativeFlappingSpeedSlider(
            centerX - 100,
            centerY - 34,
            200,
            20,
            this.creativeFlappingSpeedMultiplier
        ));

        this.posePreviewButton = this.addRenderableWidget(Button.builder(Component.empty(), button -> {
                this.posePreviewEnabled = !this.posePreviewEnabled;
                this.updatePosePreviewLabel();
            })
            .bounds(centerX - 100, centerY - 10, 200, 20)
            .build());
        this.updatePosePreviewLabel();

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.saveAndClose())
            .bounds(centerX - 100, centerY + 24, 98, 20)
            .build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> this.onClose())
            .bounds(centerX + 2, centerY + 24, 98, 20)
            .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 24, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private void saveAndClose() {
        WingsClientConfig.setPosePreviewEnabled(this.posePreviewEnabled);
        WingsClientConfig.setCreativeFlappingSpeedMultiplier(this.creativeFlappingSpeedMultiplier);
        this.onClose();
    }

    private void updatePosePreviewLabel() {
        this.posePreviewButton.setMessage(Component.literal(
            "Pose Preview: " + (this.posePreviewEnabled ? "ON" : "OFF")
        ));
    }

    private static String formatPercent(double value) {
        return (int) Math.round(value * 100.0D) + "%";
    }

    private static double snapCreativeFlappingSpeed(double value) {
        double min = WingsClientConfig.CREATIVE_FLAPPING_SPEED_MIN;
        double steps = Math.round((value - min) / CREATIVE_FLAPPING_SPEED_STEP);
        double snapped = min + steps * CREATIVE_FLAPPING_SPEED_STEP;
        return WingsClientConfig.clampCreativeFlappingSpeed(snapped);
    }

    private final class CreativeFlappingSpeedSlider extends AbstractSliderButton {
        private CreativeFlappingSpeedSlider(int x, int y, int width, int height, double initialValue) {
            super(
                x,
                y,
                width,
                height,
                Component.empty(),
                WingsClientConfig.normalizeCreativeFlappingSpeed(initialValue)
            );
            this.applyValue();
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(
                "Creative Flapping Speed: " +
                    WingsClientConfigScreen.formatPercent(WingsClientConfigScreen.this.creativeFlappingSpeedMultiplier)
            ));
        }

        @Override
        protected void applyValue() {
            double value = WingsClientConfig.denormalizeCreativeFlappingSpeed(this.value);
            WingsClientConfigScreen.this.creativeFlappingSpeedMultiplier = WingsClientConfigScreen.snapCreativeFlappingSpeed(value);
            this.value = WingsClientConfig.normalizeCreativeFlappingSpeed(
                WingsClientConfigScreen.this.creativeFlappingSpeedMultiplier
            );
        }
    }
}
