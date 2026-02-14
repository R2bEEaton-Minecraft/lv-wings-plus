package com.toni.wings.server.item;

import com.toni.wings.server.apparatus.FlightApparatus;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;

public final class DyeableWingsArmorItem extends WingsArmorItem implements DyeableLeatherItem {
    public DyeableWingsArmorItem(ArmorMaterial material, Type type, Properties properties, FlightApparatus wing, int defaultColor) {
        super(material, type, properties, wing, defaultColor);
    }

    public DyeableWingsArmorItem(ArmorMaterial material, Type type, Properties properties, FlightApparatus wing, PartColors defaultPartColors) {
        super(material, type, properties, wing, defaultPartColors);
    }
}
