package com.toni.wings.server.item;

import com.toni.wings.server.apparatus.FlightApparatus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Objects;

public class WingsArmorItem extends ArmorItem implements DyeableLeatherItem {
    private static final String TAG_DISPLAY = "display";
    private static final String TAG_COLOR = "color";
    private static final String TAG_WING_COLORIZER = "WingColorizer";
    private static final String TAG_LEFT_STEM = "leftStem";
    private static final String TAG_RIGHT_STEM = "rightStem";
    private static final String TAG_LEFT_FEATHERS = "leftFeathers";
    private static final String TAG_RIGHT_FEATHERS = "rightFeathers";

    private final FlightApparatus wing;
    private final int defaultColor;

    public WingsArmorItem(ArmorMaterial material, Type type, Properties properties, FlightApparatus wing, int defaultColor) {
        super(material, type, properties);
        this.wing = Objects.requireNonNull(wing);
        this.defaultColor = defaultColor;
    }

    public FlightApparatus getWing() {
        return this.wing;
    }

    @Override
    public int getColor(ItemStack stack) {
        CompoundTag display = stack.getTagElement(TAG_DISPLAY);
        if (display != null && display.contains(TAG_COLOR, Tag.TAG_INT)) {
            return display.getInt(TAG_COLOR);
        }
        return this.defaultColor;
    }

    @Override
    public boolean hasCustomColor(ItemStack stack) {
        CompoundTag display = stack.getTagElement(TAG_DISPLAY);
        return display != null && display.contains(TAG_COLOR, Tag.TAG_INT);
    }

    public PartColors getPartColors(ItemStack stack) {
        CompoundTag wingColorizer = stack.getTagElement(TAG_WING_COLORIZER);
        PartColors stored = null;
        if (wingColorizer != null
            && wingColorizer.contains(TAG_LEFT_STEM, Tag.TAG_INT)
            && wingColorizer.contains(TAG_RIGHT_STEM, Tag.TAG_INT)
            && wingColorizer.contains(TAG_LEFT_FEATHERS, Tag.TAG_INT)
            && wingColorizer.contains(TAG_RIGHT_FEATHERS, Tag.TAG_INT)) {
            stored = new PartColors(
                clampColor(wingColorizer.getInt(TAG_LEFT_STEM)),
                clampColor(wingColorizer.getInt(TAG_RIGHT_STEM)),
                clampColor(wingColorizer.getInt(TAG_LEFT_FEATHERS)),
                clampColor(wingColorizer.getInt(TAG_RIGHT_FEATHERS))
            );
        }

        CompoundTag display = stack.getTagElement(TAG_DISPLAY);
        if (display != null && display.contains(TAG_COLOR, Tag.TAG_INT)) {
            int legacy = clampColor(display.getInt(TAG_COLOR));
            if (stored == null) {
                return PartColors.uniform(legacy);
            }
            // If legacy display color differs, treat it as a global repaint (e.g. crafting dye).
            if (legacy != stored.blendedColor()) {
                return PartColors.uniform(legacy);
            }
            return stored;
        }

        if (stored != null) {
            return stored;
        }
        return PartColors.uniform(clampColor(this.defaultColor));
    }

    public void setPartColors(ItemStack stack, int leftStem, int rightStem, int leftFeathers, int rightFeathers) {
        PartColors colors = new PartColors(
            clampColor(leftStem),
            clampColor(rightStem),
            clampColor(leftFeathers),
            clampColor(rightFeathers)
        );

        CompoundTag wingColorizer = stack.getOrCreateTagElement(TAG_WING_COLORIZER);
        wingColorizer.putInt(TAG_LEFT_STEM, colors.leftStem());
        wingColorizer.putInt(TAG_RIGHT_STEM, colors.rightStem());
        wingColorizer.putInt(TAG_LEFT_FEATHERS, colors.leftFeathers());
        wingColorizer.putInt(TAG_RIGHT_FEATHERS, colors.rightFeathers());

        CompoundTag display = stack.getOrCreateTagElement(TAG_DISPLAY);
        display.putInt(TAG_COLOR, colors.blendedColor());
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "wings:textures/models/armor/wings_hidden_layer_1.png";
    }

    public static int clampColor(int color) {
        return color & 0xFFFFFF;
    }

    public record PartColors(int leftStem, int rightStem, int leftFeathers, int rightFeathers) {
        public PartColors {
            leftStem = clampColor(leftStem);
            rightStem = clampColor(rightStem);
            leftFeathers = clampColor(leftFeathers);
            rightFeathers = clampColor(rightFeathers);
        }

        public static PartColors uniform(int color) {
            int c = clampColor(color);
            return new PartColors(c, c, c, c);
        }

        public int blendedColor() {
            int red = (((leftStem >> 16) & 0xFF) + ((rightStem >> 16) & 0xFF) + ((leftFeathers >> 16) & 0xFF) + ((rightFeathers >> 16) & 0xFF)) / 4;
            int green = (((leftStem >> 8) & 0xFF) + ((rightStem >> 8) & 0xFF) + ((leftFeathers >> 8) & 0xFF) + ((rightFeathers >> 8) & 0xFF)) / 4;
            int blue = ((leftStem & 0xFF) + (rightStem & 0xFF) + (leftFeathers & 0xFF) + (rightFeathers & 0xFF)) / 4;
            return (red << 16) | (green << 8) | blue;
        }
    }
}
