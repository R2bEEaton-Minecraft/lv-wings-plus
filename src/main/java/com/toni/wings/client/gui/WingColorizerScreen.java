package com.toni.wings.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.toni.wings.WingsMod;
import com.toni.wings.client.apparatus.WingForm;
import com.toni.wings.client.flight.AnimatorAvian;
import com.toni.wings.client.model.ModelWingsAvian;
import com.toni.wings.server.item.WingsArmorItem;
import com.toni.wings.server.menu.WingColorizerMenu;
import com.toni.wings.server.net.serverbound.MessageApplyWingColors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Locale;

public final class WingColorizerScreen extends AbstractContainerScreen<WingColorizerMenu> {
    private static final ResourceLocation BG_TEXTURE = WingsMod.locate("textures/gui/wing_colorizer.png");

    private static final int PART_BUTTON_Y_TOP = 18;
    private static final int PART_BUTTON_WIDTH = 46;
    private static final int PART_BUTTON_HEIGHT = 16;

    private static final int PREVIEW_X = 8;
    private static final int PREVIEW_Y = 60;
    private static final int PREVIEW_WIDTH = 40;
    private static final int PREVIEW_HEIGHT = 56;

    private static final int SV_X = 52;
    private static final int SV_Y = 46;
    private static final int SV_WIDTH = 96;
    private static final int SV_HEIGHT = 70;

    private static final int HUE_X = 152;
    private static final int HUE_Y = 46;
    private static final int HUE_WIDTH = 10;
    private static final int HUE_HEIGHT = 70;

    private static final int HEX_X = 170;
    private static final int HEX_Y = 47;
    private static final int HEX_WIDTH = 78;
    private static final int HEX_HEIGHT = 20;

    private static final int APPLY_X = 170;
    private static final int APPLY_Y = 72;
    private static final int APPLY_WIDTH = 78;
    private static final int APPLY_HEIGHT = 20;

    private static final int CANCEL_X = 170;
    private static final int CANCEL_Y = 96;
    private static final int CANCEL_WIDTH = 78;
    private static final int CANCEL_HEIGHT = 20;

    private static final int[] DEFAULT_COLORS = new int[]{0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF};

    private final int[] draftColors = new int[4];

    private WingPart selectedPart = WingPart.LEFT_STEM;
    private float hue;
    private float saturation;
    private float value = 1.0F;

    private boolean draggingSv;
    private boolean draggingHue;
    private boolean updatingHex;
    private ItemStack lastSyncedStack = ItemStack.EMPTY;
    private final AnimatorAvian previewAnimator = new AnimatorAvian();

    private EditBox hexField;
    private Button applyButton;
    private Button[] partButtons;

    public WingColorizerScreen(WingColorizerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 222;
        this.inventoryLabelX = 20;
        this.inventoryLabelY = 126;
        System.arraycopy(DEFAULT_COLORS, 0, this.draftColors, 0, DEFAULT_COLORS.length);
        this.syncHsvFromSelectedColor();
    }

    @Override
    protected void init() {
        super.init();
        this.partButtons = new Button[]{
            this.createPartButton(WingPart.LEFT_STEM, 52, PART_BUTTON_Y_TOP),
            this.createPartButton(WingPart.RIGHT_STEM, 102, PART_BUTTON_Y_TOP),
            this.createPartButton(WingPart.LEFT_FEATHERS, 152, PART_BUTTON_Y_TOP),
            this.createPartButton(WingPart.RIGHT_FEATHERS, 202, PART_BUTTON_Y_TOP)
        };
        for (Button button : this.partButtons) {
            this.addRenderableWidget(button);
        }
        this.updatePartButtonState();

        this.hexField = new EditBox(this.font, this.leftPos + HEX_X, this.topPos + HEX_Y, HEX_WIDTH, HEX_HEIGHT, Component.translatable("screen.wings.wing_colorizer.hex"));
        this.hexField.setMaxLength(7);
        this.hexField.setResponder(this::onHexInputChanged);
        this.addRenderableWidget(this.hexField);

        this.applyButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.wings.wing_colorizer.apply"), button -> this.applyColors())
            .bounds(this.leftPos + APPLY_X, this.topPos + APPLY_Y, APPLY_WIDTH, APPLY_HEIGHT)
            .build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.wings.wing_colorizer.cancel"), button -> this.onClose())
            .bounds(this.leftPos + CANCEL_X, this.topPos + CANCEL_Y, CANCEL_WIDTH, CANCEL_HEIGHT)
            .build());

        this.syncFromWingStack(true);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.syncFromWingStack(false);
        this.applyButton.active = this.menu.hasEditableWing();
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x0 = this.leftPos;
        int y0 = this.topPos;
        guiGraphics.blit(BG_TEXTURE, x0, y0, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        this.drawWingPreview(guiGraphics, partialTick, x0, y0);

        this.drawSaturationValuePicker(guiGraphics, x0 + SV_X, y0 + SV_Y);
        this.drawHueSlider(guiGraphics, x0 + HUE_X, y0 + HUE_Y);
        this.drawPickerCursors(guiGraphics, x0, y0);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x3F3F3F, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x3F3F3F, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isInside(mouseX, mouseY, this.leftPos + SV_X, this.topPos + SV_Y, SV_WIDTH, SV_HEIGHT)) {
            this.draggingSv = true;
            this.updateSv(mouseX, mouseY);
            return true;
        }
        if (button == 0 && this.isInside(mouseX, mouseY, this.leftPos + HUE_X, this.topPos + HUE_Y, HUE_WIDTH, HUE_HEIGHT)) {
            this.draggingHue = true;
            this.updateHue(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            if (this.draggingSv) {
                this.updateSv(mouseX, mouseY);
                return true;
            }
            if (this.draggingHue) {
                this.updateHue(mouseY);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingSv = false;
        this.draggingHue = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private Button createPartButton(WingPart part, int x, int y) {
        return Button.builder(part.shortLabel(), button -> {
                this.selectedPart = part;
                this.syncHsvFromSelectedColor();
                this.updateHexField();
                this.updatePartButtonState();
            })
            .bounds(this.leftPos + x, this.topPos + y, PART_BUTTON_WIDTH, PART_BUTTON_HEIGHT)
            .build();
    }

    private void updatePartButtonState() {
        if (this.partButtons == null) {
            return;
        }
        for (int i = 0; i < this.partButtons.length; i++) {
            WingPart part = WingPart.values()[i];
            this.partButtons[i].active = part != this.selectedPart;
        }
    }

    private void drawSaturationValuePicker(GuiGraphics guiGraphics, int x, int y) {
        for (int dx = 0; dx < SV_WIDTH; dx++) {
            float sat = dx / (float) (SV_WIDTH - 1);
            int top = 0xFF000000 | hsvToRgb(this.hue, sat, 1.0F);
            int bottom = 0xFF000000;
            guiGraphics.fillGradient(x + dx, y, x + dx + 1, y + SV_HEIGHT, top, bottom);
        }
        drawRectBorder(guiGraphics, x, y, SV_WIDTH, SV_HEIGHT, 0xFFBEBEBE);
    }

    private void drawHueSlider(GuiGraphics guiGraphics, int x, int y) {
        for (int dy = 0; dy < HUE_HEIGHT; dy++) {
            float h = dy / (float) (HUE_HEIGHT - 1);
            int color = 0xFF000000 | hsvToRgb(h, 1.0F, 1.0F);
            guiGraphics.fill(x, y + dy, x + HUE_WIDTH, y + dy + 1, color);
        }
        drawRectBorder(guiGraphics, x, y, HUE_WIDTH, HUE_HEIGHT, 0xFFBEBEBE);
    }

    private void drawPickerCursors(GuiGraphics guiGraphics, int x0, int y0) {
        int svCursorX = x0 + SV_X + Math.round(this.saturation * (SV_WIDTH - 1));
        int svCursorY = y0 + SV_Y + Math.round((1.0F - this.value) * (SV_HEIGHT - 1));
        drawRectBorder(guiGraphics, svCursorX - 2, svCursorY - 2, 5, 5, 0xFFFFFFFF);

        int hueCursorY = y0 + HUE_Y + Math.round(this.hue * (HUE_HEIGHT - 1));
        guiGraphics.fill(x0 + HUE_X - 2, hueCursorY - 1, x0 + HUE_X + HUE_WIDTH + 2, hueCursorY + 1, 0xFFFFFFFF);
    }

    private void drawWingPreview(GuiGraphics guiGraphics, float partialTick, int x0, int y0) {
        int panelX = x0 + PREVIEW_X;
        int panelY = y0 + PREVIEW_Y;
        int cx = panelX + PREVIEW_WIDTH / 2;

        WingForm.get(WingsMod.ANGEL_WINGS).ifPresent(form -> {
            if (!(form.getModel() instanceof ModelWingsAvian model)) {
                return;
            }
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(cx, panelY + PREVIEW_HEIGHT - 10.0F, 250.0F);
            guiGraphics.pose().scale(18.0F, -18.0F, 18.0F);
            guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(180.0F));
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(0.0F));
            guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(180.0F));
            guiGraphics.pose().translate(0.0F, -1.75F, 0.0F);

            MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
            model.renderPartColors(
                this.previewAnimator,
                0.0F,
                guiGraphics.pose(),
                source.getBuffer(form.getRenderType()),
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                this.getDraftPartColors(),
                1.0F
            );
            source.endBatch();
            guiGraphics.pose().popPose();

            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
        });
    }

    private void updateSv(double mouseX, double mouseY) {
        this.saturation = Mth.clamp((float) ((mouseX - (this.leftPos + SV_X)) / (double) (SV_WIDTH - 1)), 0.0F, 1.0F);
        this.value = 1.0F - Mth.clamp((float) ((mouseY - (this.topPos + SV_Y)) / (double) (SV_HEIGHT - 1)), 0.0F, 1.0F);
        this.setSelectedPartColor(hsvToRgb(this.hue, this.saturation, this.value), true);
    }

    private void updateHue(double mouseY) {
        this.hue = Mth.clamp((float) ((mouseY - (this.topPos + HUE_Y)) / (double) (HUE_HEIGHT - 1)), 0.0F, 1.0F);
        this.setSelectedPartColor(hsvToRgb(this.hue, this.saturation, this.value), true);
    }

    private void setSelectedPartColor(int color, boolean updateHex) {
        this.draftColors[this.selectedPart.index()] = WingsArmorItem.clampColor(color);
        if (updateHex) {
            this.updateHexField();
        }
    }

    private void syncHsvFromSelectedColor() {
        float[] hsv = rgbToHsv(this.draftColors[this.selectedPart.index()]);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
    }

    private void syncFromWingStack(boolean force) {
        ItemStack stack = this.menu.getWingStack();
        if (stack.getItem() instanceof WingsArmorItem wingsItem) {
            if (force || !ItemStack.isSameItemSameTags(stack, this.lastSyncedStack)) {
                WingsArmorItem.PartColors colors = wingsItem.getPartColors(stack);
                this.draftColors[WingPart.LEFT_STEM.index()] = colors.leftStem();
                this.draftColors[WingPart.RIGHT_STEM.index()] = colors.rightStem();
                this.draftColors[WingPart.LEFT_FEATHERS.index()] = colors.leftFeathers();
                this.draftColors[WingPart.RIGHT_FEATHERS.index()] = colors.rightFeathers();
                this.lastSyncedStack = stack.copy();
                this.syncHsvFromSelectedColor();
                this.updateHexField();
            }
            return;
        }

        if (force || !this.lastSyncedStack.isEmpty()) {
            System.arraycopy(DEFAULT_COLORS, 0, this.draftColors, 0, DEFAULT_COLORS.length);
            this.lastSyncedStack = ItemStack.EMPTY;
            this.syncHsvFromSelectedColor();
            this.updateHexField();
        }
    }

    private void onHexInputChanged(String value) {
        if (this.updatingHex) {
            return;
        }
        String normalized = value.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (normalized.length() != 6) {
            return;
        }
        try {
            int parsed = Integer.parseInt(normalized, 16);
            this.setSelectedPartColor(parsed, false);
            this.syncHsvFromSelectedColor();
        } catch (NumberFormatException ignored) {
        }
    }

    private void updateHexField() {
        if (this.hexField == null) {
            return;
        }
        this.updatingHex = true;
        this.hexField.setValue(formatHex(this.draftColors[this.selectedPart.index()]));
        this.updatingHex = false;
    }

    private void applyColors() {
        if (!this.menu.hasEditableWing()) {
            return;
        }
        WingsMod.instance().network().sendToServer(new MessageApplyWingColors(
            this.menu.containerId,
            this.draftColors[WingPart.LEFT_STEM.index()],
            this.draftColors[WingPart.RIGHT_STEM.index()],
            this.draftColors[WingPart.LEFT_FEATHERS.index()],
            this.draftColors[WingPart.RIGHT_FEATHERS.index()]
        ));
    }

    private WingsArmorItem.PartColors getDraftPartColors() {
        return new WingsArmorItem.PartColors(
            this.draftColors[WingPart.LEFT_STEM.index()],
            this.draftColors[WingPart.RIGHT_STEM.index()],
            this.draftColors[WingPart.LEFT_FEATHERS.index()],
            this.draftColors[WingPart.RIGHT_FEATHERS.index()]
        );
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static void drawRectBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + 1, color);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color);
        guiGraphics.fill(x, y, x + 1, y + height, color);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static int hsvToRgb(float hue, float saturation, float value) {
        hue = hue - (float) Math.floor(hue);
        saturation = Mth.clamp(saturation, 0.0F, 1.0F);
        value = Mth.clamp(value, 0.0F, 1.0F);
        if (saturation <= 0.0F) {
            int grey = Math.round(value * 255.0F);
            return (grey << 16) | (grey << 8) | grey;
        }

        float scaled = hue * 6.0F;
        int sector = (int) Math.floor(scaled);
        float fraction = scaled - sector;
        float p = value * (1.0F - saturation);
        float q = value * (1.0F - saturation * fraction);
        float t = value * (1.0F - saturation * (1.0F - fraction));

        float red;
        float green;
        float blue;
        switch (sector % 6) {
            case 0 -> {
                red = value;
                green = t;
                blue = p;
            }
            case 1 -> {
                red = q;
                green = value;
                blue = p;
            }
            case 2 -> {
                red = p;
                green = value;
                blue = t;
            }
            case 3 -> {
                red = p;
                green = q;
                blue = value;
            }
            case 4 -> {
                red = t;
                green = p;
                blue = value;
            }
            default -> {
                red = value;
                green = p;
                blue = q;
            }
        }

        int r = Math.round(red * 255.0F);
        int g = Math.round(green * 255.0F);
        int b = Math.round(blue * 255.0F);
        return (r << 16) | (g << 8) | b;
    }

    private static float[] rgbToHsv(int rgb) {
        float red = ((rgb >> 16) & 0xFF) / 255.0F;
        float green = ((rgb >> 8) & 0xFF) / 255.0F;
        float blue = (rgb & 0xFF) / 255.0F;

        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float delta = max - min;

        float hue;
        if (delta == 0.0F) {
            hue = 0.0F;
        } else if (max == red) {
            hue = ((green - blue) / delta) % 6.0F;
        } else if (max == green) {
            hue = ((blue - red) / delta) + 2.0F;
        } else {
            hue = ((red - green) / delta) + 4.0F;
        }
        hue /= 6.0F;
        if (hue < 0.0F) {
            hue += 1.0F;
        }

        float saturation = max == 0.0F ? 0.0F : (delta / max);
        return new float[]{hue, saturation, max};
    }

    private static String formatHex(int color) {
        return "#" + String.format(Locale.ROOT, "%06X", WingsArmorItem.clampColor(color));
    }

    private enum WingPart {
        LEFT_STEM(0, "screen.wings.wing_colorizer.left_stem_short", "screen.wings.wing_colorizer.left_stem"),
        RIGHT_STEM(1, "screen.wings.wing_colorizer.right_stem_short", "screen.wings.wing_colorizer.right_stem"),
        LEFT_FEATHERS(2, "screen.wings.wing_colorizer.left_feathers_short", "screen.wings.wing_colorizer.left_feathers"),
        RIGHT_FEATHERS(3, "screen.wings.wing_colorizer.right_feathers_short", "screen.wings.wing_colorizer.right_feathers");

        private final int index;
        private final String shortKey;
        private final String labelKey;

        WingPart(int index, String shortKey, String labelKey) {
            this.index = index;
            this.shortKey = shortKey;
            this.labelKey = labelKey;
        }

        int index() {
            return this.index;
        }

        Component shortLabel() {
            return Component.translatable(this.shortKey).withStyle(ChatFormatting.WHITE);
        }

        Component label() {
            return Component.translatable(this.labelKey);
        }
    }
}
