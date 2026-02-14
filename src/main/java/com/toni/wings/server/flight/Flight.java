package com.toni.wings.server.flight;

import com.toni.wings.server.apparatus.FlightApparatus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public interface Flight {
    double CREATIVE_HOVER_FLAP_RATE_MIN = 0.0D;

    double CREATIVE_HOVER_FLAP_RATE_MAX = 2.0D;

    double CREATIVE_HOVER_FLAP_RATE_DEFAULT = 0.67D;

    default void setIsFlying(boolean isFlying) {
        this.setIsFlying(isFlying, PlayerSet.empty());
    }
    void setIsFlying(boolean isFlying, PlayerSet players);

    boolean isFlying();
    default void toggleIsFlying(PlayerSet players) {
        this.setIsFlying(!this.isFlying(), players);
        //System.out.println("Flight set to " + this.isFlying());
    }

    void setTimeFlying(int timeFlying);

    int getTimeFlying();

    default void setPose(FlightPose pose) {
        this.setPose(pose, PlayerSet.empty());
    }

    void setPose(FlightPose pose, PlayerSet players);

    FlightPose getPose();

    default void setFloating(boolean floating) {
        this.setFloating(floating, PlayerSet.empty());
    }

    void setFloating(boolean floating, PlayerSet players);

    boolean isFloating();

    default void setCreativeHovering(boolean creativeHovering) {
        this.setCreativeHovering(creativeHovering, PlayerSet.empty());
    }

    void setCreativeHovering(boolean creativeHovering, PlayerSet players);

    boolean isCreativeHovering();

    default void setWing(FlightApparatus wing) {
        this.setWing(wing, PlayerSet.empty());
    }

    void setWing(FlightApparatus wing, PlayerSet players);

    FlightApparatus getWing();

    default void setCreativeHoverFlapRate(double flapRate) {
        this.setCreativeHoverFlapRate(flapRate, PlayerSet.empty());
    }

    void setCreativeHoverFlapRate(double flapRate, PlayerSet players);

    double getCreativeHoverFlapRate();

    float getFlyingAmount(float delta);

    void registerFlyingListener(FlyingListener listener);

    void registerSyncListener(SyncListener listener);

    boolean canFly(Player player);

    boolean hasEffect(Player player);
    boolean canLand(Player player);

    void tick(Player player);

    void onFlown(Player player, Vec3 direction);

    void clone(Flight other);

    void sync(PlayerSet players);

    void serialize(FriendlyByteBuf buf);

    void deserialize(FriendlyByteBuf buf);

    static double clampCreativeHoverFlapRate(double flapRate) {
        return Math.max(CREATIVE_HOVER_FLAP_RATE_MIN, Math.min(CREATIVE_HOVER_FLAP_RATE_MAX, flapRate));
    }

    interface FlyingListener {
        void onChange(boolean isFlying);

        static Consumer<FlyingListener> onChangeUsing(boolean isFlying) {
            return l -> l.onChange(isFlying);
        }
    }

    interface SyncListener {
        void onSync(PlayerSet players);

        static Consumer<SyncListener> onSyncUsing(PlayerSet players) {
            return l -> l.onSync(players);
        }
    }

    interface PlayerSet {
        void notify(Notifier notifier);

        static PlayerSet empty() {
            return n -> {
            };
        }

        static PlayerSet ofSelf() {
            return Notifier::notifySelf;
        }

        static PlayerSet ofPlayer(ServerPlayer player) {
            return n -> n.notifyPlayer(player);
        }

        static PlayerSet ofOthers() {
            return Notifier::notifyOthers;
        }

        static PlayerSet ofAll() {
            return n -> {
                n.notifySelf();
                n.notifyOthers();
            };
        }
    }

    interface Notifier {
        void notifySelf();

        void notifyPlayer(ServerPlayer player);

        void notifyOthers();

        static Notifier of(Runnable notifySelf, Consumer<ServerPlayer> notifyPlayer, Runnable notifyOthers) {
            return new Notifier() {
                @Override
                public void notifySelf() {
                    notifySelf.run();
                }

                @Override
                public void notifyPlayer(ServerPlayer player) {
                    notifyPlayer.accept(player);
                }

                @Override
                public void notifyOthers() {
                    notifyOthers.run();
                }
            };
        }
    }
}
