package com.toni.wings.server.net.serverbound;

import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.flight.Flights;
import com.toni.wings.server.net.Message;
import com.toni.wings.server.net.ServerMessageContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public final class MessageSetCreativeHoverFlapRate implements Message {
    private double flapRate;

    public MessageSetCreativeHoverFlapRate() {
        this(Flight.CREATIVE_HOVER_FLAP_RATE_DEFAULT);
    }

    public MessageSetCreativeHoverFlapRate(double flapRate) {
        this.flapRate = Flight.clampCreativeHoverFlapRate(flapRate);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(this.flapRate);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.flapRate = Flight.clampCreativeHoverFlapRate(buf.readDouble());
    }

    public static void handle(MessageSetCreativeHoverFlapRate message, ServerMessageContext context) {
        Player player = context.getPlayer();
        Flights.get(player).ifPresent(flight ->
            flight.setCreativeHoverFlapRate(message.flapRate, Flight.PlayerSet.ofAll())
        );
    }
}
