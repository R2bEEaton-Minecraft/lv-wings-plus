package com.toni.wings.server.net.serverbound;

import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.flight.FlightPose;
import com.toni.wings.server.flight.Flights;
import com.toni.wings.server.net.Message;
import com.toni.wings.server.net.ServerMessageContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public final class MessageSetFlightPose implements Message {
    private FlightPose pose;

    public MessageSetFlightPose() {
        this(FlightPose.DEFAULT);
    }

    public MessageSetFlightPose(FlightPose pose) {
        this.pose = pose == null ? FlightPose.DEFAULT : pose;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.pose.ordinal());
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.pose = FlightPose.byOrdinal(buf.readVarInt());
    }

    public static void handle(MessageSetFlightPose message, ServerMessageContext context) {
        Player player = context.getPlayer();
        Flights.get(player)
            .filter(flight -> flight.hasEffect(player) && (flight.isFlying() || flight.isFloating()))
            .ifPresent(flight -> flight.setPose(message.pose, Flight.PlayerSet.ofAll()));
    }
}
