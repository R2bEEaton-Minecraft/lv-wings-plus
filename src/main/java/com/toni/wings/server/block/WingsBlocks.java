package com.toni.wings.server.block;

import com.toni.wings.WingsMod;
import com.toni.wings.server.item.WingsItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class WingsBlocks {
    private WingsBlocks() {
    }

    public static final DeferredRegister<Block> REG = DeferredRegister.create(ForgeRegistries.BLOCKS, WingsMod.ID);

    public static final RegistryObject<Block> WING_COLORIZER = REG.register(
        "wing_colorizer",
        () -> new WingColorizerBlock(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE).strength(2.5F))
    );

    public static final RegistryObject<Item> WING_COLORIZER_ITEM = WingsItems.REG.register(
        "wing_colorizer",
        () -> new BlockItem(WING_COLORIZER.get(), new Item.Properties())
    );

    public static void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(WING_COLORIZER_ITEM.get());
        }
    }
}
