package com.toni.wings.client.gui;

import com.toni.wings.server.config.WingsClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class WingsClientConfigScreen extends Screen {
    private final Screen parent;
    private boolean posePreviewEnabled;
    private Button posePreviewButton;

    public WingsClientConfigScreen(Screen parent) {
        super(Component.literal("Wings Config"));
        this.parent = parent;
        this.posePreviewEnabled = WingsClientConfig.isPosePreviewEnabled();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

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
        this.onClose();
    }

    private void updatePosePreviewLabel() {
        this.posePreviewButton.setMessage(Component.literal(
            "Pose Preview: " + (this.posePreviewEnabled ? "ON" : "OFF")
        ));
    }
}
