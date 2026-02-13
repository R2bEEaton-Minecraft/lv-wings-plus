package com.toni.wings.server.net.serverbound;

import com.toni.wings.server.item.WingsArmorItem;
import com.toni.wings.server.menu.WingColorizerMenu;
import com.toni.wings.server.net.Message;
import com.toni.wings.server.net.ServerMessageContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public final class MessageApplyWingColors implements Message {
    private int containerId;
    private int leftStem;
    private int rightStem;
    private int leftFeathers;
    private int rightFeathers;

    public MessageApplyWingColors() {
        this(0, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF);
    }

    public MessageApplyWingColors(int containerId, int leftStem, int rightStem, int leftFeathers, int rightFeathers) {
        this.containerId = containerId;
        this.leftStem = WingsArmorItem.clampColor(leftStem);
        this.rightStem = WingsArmorItem.clampColor(rightStem);
        this.leftFeathers = WingsArmorItem.clampColor(leftFeathers);
        this.rightFeathers = WingsArmorItem.clampColor(rightFeathers);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.containerId);
        buf.writeVarInt(this.leftStem);
        buf.writeVarInt(this.rightStem);
        buf.writeVarInt(this.leftFeathers);
        buf.writeVarInt(this.rightFeathers);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        this.leftStem = WingsArmorItem.clampColor(buf.readVarInt());
        this.rightStem = WingsArmorItem.clampColor(buf.readVarInt());
        this.leftFeathers = WingsArmorItem.clampColor(buf.readVarInt());
        this.rightFeathers = WingsArmorItem.clampColor(buf.readVarInt());
    }

    public static void handle(MessageApplyWingColors message, ServerMessageContext context) {
        Player player = context.getPlayer();
        if (player.containerMenu instanceof WingColorizerMenu menu && menu.containerId == message.containerId) {
            menu.applyColors(player, message.leftStem, message.rightStem, message.leftFeathers, message.rightFeathers);
        }
    }
}
