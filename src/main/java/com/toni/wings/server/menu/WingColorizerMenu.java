package com.toni.wings.server.menu;

import com.toni.wings.server.block.WingsBlocks;
import com.toni.wings.server.block.entity.WingColorizerBlockEntity;
import com.toni.wings.server.item.WingsArmorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

public final class WingColorizerMenu extends AbstractContainerMenu {
    private static final int SLOT_COUNT = 1;
    public static final int WING_SLOT = 0;
    private static final int PLAYER_INVENTORY_START = 1;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerLevelAccess access;

    public WingColorizerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, unpackClientData(playerInventory, data));
    }

    private WingColorizerMenu(int containerId, Inventory playerInventory, ClientData clientData) {
        this(containerId, playerInventory, clientData.container(), clientData.access());
    }

    public WingColorizerMenu(int containerId, Inventory playerInventory, Container container, ContainerLevelAccess access) {
        super(WingsMenus.WING_COLORIZER.get(), containerId);
        checkContainerSize(container, SLOT_COUNT);
        this.container = container;
        this.access = access;
        this.container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, WING_SLOT, 20, 35) {
            @Override
            public boolean mayPlace(@Nonnull ItemStack stack) {
                return stack.getItem() instanceof WingsArmorItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 20 + col * 18, 136 + row * 18));
            }
        }

        for (int hotbar = 0; hotbar < 9; ++hotbar) {
            this.addSlot(new Slot(playerInventory, hotbar, 20 + hotbar * 18, 194));
        }
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        ItemStack empty = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return empty;
        }

        ItemStack inSlot = slot.getItem();
        ItemStack original = inSlot.copy();
        if (index == WING_SLOT) {
            if (!this.moveItemStackTo(inSlot, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (inSlot.getItem() instanceof WingsArmorItem) {
            if (!this.moveItemStackTo(inSlot, WING_SLOT, WING_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_INVENTORY_START && index < PLAYER_INVENTORY_END) {
            if (!this.moveItemStackTo(inSlot, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= HOTBAR_START && index < HOTBAR_END) {
            if (!this.moveItemStackTo(inSlot, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (inSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (inSlot.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, inSlot);
        return original;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return stillValid(this.access, player, WingsBlocks.WING_COLORIZER.get());
    }

    @Override
    public void removed(@Nonnull Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Nonnull
    public ItemStack getWingStack() {
        return this.container.getItem(WING_SLOT);
    }

    public boolean hasEditableWing() {
        ItemStack stack = this.getWingStack();
        return stack.getItem() instanceof WingsArmorItem;
    }

    public boolean applyColors(Player player, int leftStem, int rightStem, int leftFeathers, int rightFeathers) {
        if (!this.stillValid(player)) {
            return false;
        }
        ItemStack stack = this.getWingStack();
        if (!(stack.getItem() instanceof WingsArmorItem wingsItem)) {
            return false;
        }
        wingsItem.setPartColors(
            stack,
            WingsArmorItem.clampColor(leftStem),
            WingsArmorItem.clampColor(rightStem),
            WingsArmorItem.clampColor(leftFeathers),
            WingsArmorItem.clampColor(rightFeathers)
        );
        this.container.setChanged();
        this.broadcastChanges();
        return true;
    }

    private static ClientData unpackClientData(Inventory playerInventory, FriendlyByteBuf data) {
        if (data == null) {
            return new ClientData(new SimpleContainer(SLOT_COUNT), ContainerLevelAccess.NULL);
        }
        BlockPos pos = data.readBlockPos();
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof WingColorizerBlockEntity wingColorizer) {
            return new ClientData(wingColorizer, ContainerLevelAccess.create(playerInventory.player.level(), pos));
        }
        return new ClientData(new SimpleContainer(SLOT_COUNT), ContainerLevelAccess.NULL);
    }

    private record ClientData(Container container, ContainerLevelAccess access) {
    }
}
