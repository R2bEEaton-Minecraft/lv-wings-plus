package com.toni.wings.server.net.serverbound;

import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.flight.Flights;
import com.toni.wings.server.net.Message;
import com.toni.wings.server.net.ServerMessageContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public final class MessageSetFloating implements Message {
    private boolean floating;

    public MessageSetFloating() {
    }

    public MessageSetFloating(boolean floating) {
        this.floating = floating;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.floating);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.floating = buf.readBoolean();
    }

    public static void handle(MessageSetFloating message, ServerMessageContext context) {
        Player player = context.getPlayer();
        Flights.get(player).ifPresent(flight -> {
            if (!message.floating || flight.canFly(player)) {
                flight.setFloating(message.floating, Flight.PlayerSet.ofAll());
            }
        });
    }
}
