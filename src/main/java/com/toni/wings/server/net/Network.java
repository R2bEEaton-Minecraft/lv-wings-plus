package com.toni.wings.server.net;

import com.toni.wings.WingsMod;
import com.toni.wings.server.net.clientbound.MessageSyncFlight;
import com.toni.wings.server.net.serverbound.MessageApplyWingColors;
import com.toni.wings.server.net.serverbound.MessageControlFlying;
import com.toni.wings.server.net.serverbound.MessageSetCreativeHoverFlapRate;
import com.toni.wings.server.net.serverbound.MessageSetFloating;
import com.toni.wings.server.net.serverbound.MessageSetFlightPose;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Network {
    private final SimpleChannel network = new NetBuilder(WingsMod.locate("net"))
        .version(5).optionalServer().requiredClient()
        .serverbound(MessageControlFlying::new).consumer(() -> MessageControlFlying::handle)
        .serverbound(MessageSetFlightPose::new).consumer(() -> MessageSetFlightPose::handle)
        .serverbound(MessageSetFloating::new).consumer(() -> MessageSetFloating::handle)
        .serverbound(MessageSetCreativeHoverFlapRate::new).consumer(() -> MessageSetCreativeHoverFlapRate::handle)
        .serverbound(MessageApplyWingColors::new).consumer(() -> MessageApplyWingColors::handle)
        .clientbound(MessageSyncFlight::new).consumer(() -> MessageSyncFlight::handle)
        .build();

    public void sendToServer(Message message) {
        network.sendToServer(message);
    }

    public void sendToPlayer(Message message, ServerPlayer player) {
        network.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public void sendToAllTracking(Message message, Entity entity) {
        network.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }

    public boolean isRemotePresent(Connection connection) {
        return this.network.isRemotePresent(connection);
    }
}
