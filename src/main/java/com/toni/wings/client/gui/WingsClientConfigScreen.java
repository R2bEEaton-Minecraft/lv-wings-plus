package com.toni.wings.client.gui;

import com.toni.wings.server.config.WingsClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class WingsClientConfigScreen extends Screen {
    private static final double CREATIVE_HOVER_FLAP_RATE_STEP = 0.01D;

    private final Screen parent;
    private boolean posePreviewEnabled;
    private double creativeHoverFlapRate;
    private Button posePreviewButton;

    public WingsClientConfigScreen(Screen parent) {
        super(Component.literal("Wings Config"));
        this.parent = parent;
        this.posePreviewEnabled = WingsClientConfig.isPosePreviewEnabled();
        this.creativeHoverFlapRate = WingsClientConfig.getCreativeHoverFlapRate();
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
            this.creativeHoverFlapRate
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
        WingsClientConfig.setCreativeHoverFlapRate(this.creativeHoverFlapRate);
        this.onClose();
    }

    private void updatePosePreviewLabel() {
        this.posePreviewButton.setMessage(Component.literal(
            "Pose Preview: " + (this.posePreviewEnabled ? "ON" : "OFF")
        ));
    }

    private static String formatFlapRate(double value) {
        return String.format(Locale.US, "%.2f/s", value);
    }

    private static double snapCreativeHoverFlapRate(double value) {
        double min = WingsClientConfig.CREATIVE_HOVER_FLAP_RATE_MIN;
        double steps = Math.round((value - min) / CREATIVE_HOVER_FLAP_RATE_STEP);
        double snapped = min + steps * CREATIVE_HOVER_FLAP_RATE_STEP;
        return WingsClientConfig.clampCreativeHoverFlapRate(snapped);
    }

    private final class CreativeFlappingSpeedSlider extends AbstractSliderButton {
        private CreativeFlappingSpeedSlider(int x, int y, int width, int height, double initialValue) {
            super(
                x,
                y,
                width,
                height,
                Component.empty(),
                WingsClientConfig.normalizeCreativeHoverFlapRate(initialValue)
            );
            this.applyValue();
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(
                "Creative Flapping Speed: " +
                    WingsClientConfigScreen.formatFlapRate(WingsClientConfigScreen.this.creativeHoverFlapRate)
            ));
        }

        @Override
        protected void applyValue() {
            double value = WingsClientConfig.denormalizeCreativeHoverFlapRate(this.value);
            WingsClientConfigScreen.this.creativeHoverFlapRate =
                WingsClientConfigScreen.snapCreativeHoverFlapRate(value);
            this.value = WingsClientConfig.normalizeCreativeHoverFlapRate(
                WingsClientConfigScreen.this.creativeHoverFlapRate
            );
        }
    }
}
