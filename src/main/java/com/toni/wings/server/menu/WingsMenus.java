package com.toni.wings.server.menu;

import com.toni.wings.WingsMod;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class WingsMenus {
    private WingsMenus() {
    }

    public static final DeferredRegister<MenuType<?>> REG = DeferredRegister.create(ForgeRegistries.MENU_TYPES, WingsMod.ID);

    public static final RegistryObject<MenuType<WingColorizerMenu>> WING_COLORIZER = REG.register(
        "wing_colorizer",
        () -> IForgeMenuType.create(WingColorizerMenu::new)
    );
}
