package com.toni.wings.server.block.entity;

import com.toni.wings.WingsMod;
import com.toni.wings.server.block.WingsBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class WingsBlockEntities {
    private WingsBlockEntities() {
    }

    public static final DeferredRegister<BlockEntityType<?>> REG = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WingsMod.ID);

    public static final RegistryObject<BlockEntityType<WingColorizerBlockEntity>> WING_COLORIZER = REG.register(
        "wing_colorizer",
        () -> BlockEntityType.Builder.of(WingColorizerBlockEntity::new, WingsBlocks.WING_COLORIZER.get()).build(null)
    );
}
