package com.toni.wings.server.item;

import com.toni.wings.server.apparatus.FlightApparatus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class WingsArmorItem extends ArmorItem implements DyeableLeatherItem {
    private static final String TAG_DISPLAY = "display";
    private static final String TAG_COLOR = "color";

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
}
