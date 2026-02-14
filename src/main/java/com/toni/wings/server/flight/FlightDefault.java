package com.toni.wings.server.flight;

import com.google.common.collect.Lists;
import com.toni.wings.WingsMod;
import com.toni.wings.server.apparatus.FlightApparatus;
import com.toni.wings.server.effect.WingsEffects;
import com.toni.wings.server.item.WingsArmorItem;
import com.toni.wings.util.CubicBezier;
import com.toni.wings.util.MathH;
import com.toni.wings.util.NBTSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class FlightDefault implements Flight {
    private static final CubicBezier FLY_AMOUNT_CURVE = new CubicBezier(0.37F, 0.13F, 0.3F, 1.12F);

    private static final int INITIAL_TIME_FLYING = 0;

    private static final int MAX_TIME_FLYING = 20;

    private static final float MIN_SPEED = 0.03F;

    private static final float MAX_SPEED = 0.0715F;

    private static final float Y_BOOST = 0.05F;

    private static final float FALL_REDUCTION = 0.9F;

    private static final float PITCH_OFFSET = 30.0F;

    private final List<FlyingListener> flyingListeners = Lists.newArrayList();

    private final List<SyncListener> syncListeners = Lists.newArrayList();

    private final WingState voidState = new WingState(FlightApparatus.NONE, FlightApparatus.FlightState.NONE);

    private int prevTimeFlying = INITIAL_TIME_FLYING;

    private int timeFlying = INITIAL_TIME_FLYING;

    private boolean isFlying;

    private boolean isFloating;

    private boolean creativeHovering;

    private FlightPose pose = FlightPose.DEFAULT;

    private FlightApparatus flightApparatus = FlightApparatus.NONE;

    private double creativeHoverFlapRate = CREATIVE_HOVER_FLAP_RATE_DEFAULT;

    private WingState state = this.voidState;

    @Override
    public void setIsFlying(boolean isFlying, PlayerSet players) {
        if (this.isFlying != isFlying) {
            this.isFlying = isFlying;
            if (!isFlying && this.isFloating) {
                this.isFloating = false;
            }
            this.flyingListeners.forEach(FlyingListener.onChangeUsing(isFlying));
            this.sync(players);
        }
    }

    @Override
    public boolean isFlying() {
        return this.isFlying;
    }

    @Override
    public void setTimeFlying(int timeFlying) {
        this.timeFlying = timeFlying;
    }

    @Override
    public int getTimeFlying() {
        return this.timeFlying;
    }

    @Override
    public void setPose(FlightPose pose, PlayerSet players) {
        if (pose == null) {
            pose = FlightPose.DEFAULT;
        }
        if (this.pose != pose) {
            this.pose = pose;
            this.sync(players);
        }
    }

    @Override
    public FlightPose getPose() {
        return this.pose;
    }

    @Override
    public void setFloating(boolean floating, PlayerSet players) {
        if (this.isFloating != floating) {
            this.isFloating = floating;
            if (floating && !this.isFlying) {
                this.setIsFlying(true, players);
                return;
            }
            this.sync(players);
        }
    }

    @Override
    public boolean isFloating() {
        return this.isFloating;
    }

    @Override
    public void setCreativeHovering(boolean creativeHovering, PlayerSet players) {
        if (this.creativeHovering != creativeHovering) {
            this.creativeHovering = creativeHovering;
            this.sync(players);
        }
    }

    @Override
    public boolean isCreativeHovering() {
        return this.creativeHovering;
    }

    @Override
    public void setWing(FlightApparatus wing, PlayerSet players) {
        Objects.requireNonNull(wing);
        if (this.flightApparatus != wing) {
            this.flightApparatus = wing;
            this.sync(players);
        }
    }

    @Override
    public FlightApparatus getWing() {
        return this.flightApparatus;
    }

    @Override
    public void setCreativeHoverFlapRate(double flapRate, PlayerSet players) {
        double clamped = Flight.clampCreativeHoverFlapRate(flapRate);
        if (Double.compare(this.creativeHoverFlapRate, clamped) != 0) {
            this.creativeHoverFlapRate = clamped;
            this.sync(players);
        }
    }

    @Override
    public double getCreativeHoverFlapRate() {
        return this.creativeHoverFlapRate;
    }

    @Override
    public float getFlyingAmount(float delta) {
        return FLY_AMOUNT_CURVE
                .eval(MathH.lerp(this.getPrevTimeFlying(), this.getTimeFlying(), delta) / MAX_TIME_FLYING);
    }

    private void setPrevTimeFlying(int prevTimeFlying) {
        this.prevTimeFlying = prevTimeFlying;
    }

    private int getPrevTimeFlying() {
        return this.prevTimeFlying;
    }

    @Override
    public void registerFlyingListener(FlyingListener listener) {
        this.flyingListeners.add(listener);
    }

    @Override
    public void registerSyncListener(SyncListener listener) {
        this.syncListeners.add(listener);
    }

    @Override
    public boolean canFly(Player player) {
        return (this.hasEffect(player) && this.flightApparatus.isUsable(player));
    }

    @Override
    public boolean hasEffect(Player player) {
        return hasWingsItem(player) || WingsEffects.WINGS.filter(effect -> player.getEffect(effect) != null).isPresent();
    }

    @Override
    public boolean canLand(Player player) {
        return this.flightApparatus.isLandable(player);
    }

    private void onWornUpdate(Player player) {
        if (player.isEffectiveAi()) {
            if (this.isFlying()) {
                if (this.isFloating() && player.zza == 0.0F && player.xxa == 0.0F && !player.onGround()) {
                    Vec3 motion = player.getDeltaMovement();
                    Vec3 dampened = motion.multiply(0.6D, 0.3D, 0.6D);
                    if (Math.abs(dampened.y()) < 0.05D) {
                        dampened = new Vec3(dampened.x(), 0.0D, dampened.z());
                    }
                    player.setDeltaMovement(dampened);
                    player.fallDistance = 0.0F;
                } else {
                    float speed = (float) Mth.clampedLerp(MIN_SPEED, MAX_SPEED, player.zza);
                    float elevationBoost = MathH.transform(
                            Math.abs(player.getXRot()),
                            45.0F, 90.0F,
                            1.0F, 0.0F);
                    float pitch = -MathH.toRadians(player.getXRot() - PITCH_OFFSET * elevationBoost);
                    float yaw = -MathH.toRadians(player.getYRot()) - MathH.PI;
                    float vxz = -Mth.cos(pitch);
                    float vy = Mth.sin(pitch);
                    float vz = Mth.cos(yaw);
                    float vx = Mth.sin(yaw);
                    player.setDeltaMovement(player.getDeltaMovement().add(
                            vx * vxz * speed,
                            vy * speed + Y_BOOST * (player.getXRot() > 0.0F ? elevationBoost : 1.0D),
                            vz * vxz * speed));
                }
            }
            if (this.canLand(player)) {
                Vec3 mot = player.getDeltaMovement();
                if (mot.y() < 0.0D) {
                    player.setDeltaMovement(mot.multiply(1.0D, FALL_REDUCTION, 1.0D));
                }
                player.fallDistance = 0.0F;
            }
        }
        if (!player.level().isClientSide) {
            if (this.flightApparatus.isUsable(player)) {
                (this.state = this.state.next(this.flightApparatus)).onUpdate(player);
            } else if (this.isFlying()) {
                this.setIsFlying(false, PlayerSet.ofAll());
                this.state = this.state.notFlying();
            }
        }
    }

    @Override
    public void tick(Player player) {
        boolean hasEffect = this.hasEffect(player);
        if (!player.level().isClientSide) {
            this.setCreativeHovering(hasEffect && isCreativeHovering(player), PlayerSet.ofAll());
        }
        FlightApparatus equipped = getEquippedWing(player);
        if (hasEffect || !player.isEffectiveAi()) {
            if (!hasEffect && !player.level().isClientSide) {
                this.setWing(FlightApparatus.NONE, PlayerSet.ofAll());
                if (this.isFloating()) {
                    this.setFloating(false, PlayerSet.ofAll());
                }
            } else if (equipped != FlightApparatus.NONE && !player.level().isClientSide) {
                this.setWing(equipped, PlayerSet.ofAll());
            }
            if (!player.level().isClientSide && this.isFloating() && player.onGround()) {
                this.setFloating(false, PlayerSet.ofAll());
            }
            this.onWornUpdate(player);
        } else if (!player.level().isClientSide) {
            this.setWing(FlightApparatus.NONE, PlayerSet.ofAll());
            if (this.isFlying()) {
                this.setIsFlying(false, PlayerSet.ofAll());
            }
            if (this.isFloating()) {
                this.setFloating(false, PlayerSet.ofAll());
            }
        }
        this.setPrevTimeFlying(this.getTimeFlying());
        if (this.isFlying() || this.isFloating()) {
            if (this.getTimeFlying() < MAX_TIME_FLYING) {
                this.setTimeFlying(this.getTimeFlying() + 1);
            } else if (player.isLocalPlayer() && player.onGround()) {
                this.setIsFlying(false, PlayerSet.ofOthers());
            }
        } else {
            if (this.getTimeFlying() > INITIAL_TIME_FLYING) {
                this.setTimeFlying(this.getTimeFlying() - 1);
            }
        }
    }

    @Override
    public void onFlown(Player player, Vec3 direction) {
        if (this.isFlying()) {
            this.flightApparatus.onFlight(player, direction);
        } else if (player.getDeltaMovement().y() < -0.5D) {
            this.flightApparatus.onLanding(player, direction);
        }
    }

    @Override
    public void clone(Flight other) {
        this.setIsFlying(other.isFlying());
        this.setTimeFlying(other.getTimeFlying());
        this.setWing(other.getWing());
        this.setPose(other.getPose());
        this.setFloating(other.isFloating());
        this.setCreativeHovering(other.isCreativeHovering());
        this.setCreativeHoverFlapRate(other.getCreativeHoverFlapRate());
    }

    @Override
    public void sync(PlayerSet players) {
        this.syncListeners.forEach(SyncListener.onSyncUsing(players));
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isFlying());
        buf.writeVarInt(this.getTimeFlying());
        buf.writeUtf(Objects.requireNonNull(WingsMod.WINGS.getKey(this.getWing())).toString());
        buf.writeBoolean(this.isFloating());
        buf.writeBoolean(this.isCreativeHovering());
        buf.writeVarInt(this.getPose().ordinal());
        buf.writeDouble(this.getCreativeHoverFlapRate());
    }

    @Override
    public void deserialize(FriendlyByteBuf buf) {
        this.setIsFlying(buf.readBoolean());
        this.setTimeFlying(buf.readVarInt());
        ResourceLocation wingId = ResourceLocation.tryParse(buf.readUtf(64));
        FlightApparatus wing = wingId != null
                ? WingsMod.WINGS.getOptional(wingId).orElse(FlightApparatus.NONE)
                : FlightApparatus.NONE;
        this.setWing(wing);
        this.setFloating(buf.readBoolean());
        this.setCreativeHovering(buf.readBoolean());
        this.setPose(FlightPose.byOrdinal(buf.readVarInt()));
        this.setCreativeHoverFlapRate(buf.readDouble());
    }

    public static final class Serializer implements NBTSerializer<FlightDefault, CompoundTag> {
        private static final String IS_FLYING = "isFlying";

        private static final String TIME_FLYING = "timeFlying";

        private static final String WING = "wing";

        private static final String POSE = "pose";

        private static final String FLOATING = "floating";

        private static final String CREATIVE_HOVERING = "creativeHovering";

        private static final String CREATIVE_HOVER_FLAP_RATE = "creativeHoverFlapRate";

        private final Supplier<FlightDefault> factory;

        public Serializer(Supplier<FlightDefault> factory) {
            this.factory = factory;
        }

        @Override
        public CompoundTag serialize(FlightDefault instance) {
            CompoundTag compound = new CompoundTag();
            compound.putBoolean(IS_FLYING, instance.isFlying());
            compound.putInt(TIME_FLYING, instance.getTimeFlying());
            compound.putString(WING, Objects.requireNonNull(WingsMod.WINGS.getKey(instance.getWing())).toString());
            compound.putString(POSE, instance.getPose().getId());
            compound.putBoolean(FLOATING, instance.isFloating());
            compound.putBoolean(CREATIVE_HOVERING, instance.isCreativeHovering());
            compound.putDouble(CREATIVE_HOVER_FLAP_RATE, instance.getCreativeHoverFlapRate());
            return compound;
        }

        @Override
        public FlightDefault deserialize(CompoundTag compound) {
            FlightDefault f = this.factory.get();
            f.setIsFlying(compound.getBoolean(IS_FLYING));
            f.setTimeFlying(compound.getInt(TIME_FLYING));
            ResourceLocation wingId = ResourceLocation.tryParse(compound.getString(WING));
            FlightApparatus wing = wingId != null
                    ? WingsMod.WINGS.getOptional(wingId).orElse(FlightApparatus.NONE)
                    : FlightApparatus.NONE;
            f.setWing(wing);
            if (compound.contains(POSE, Tag.TAG_STRING)) {
                f.setPose(FlightPose.byId(compound.getString(POSE)));
            }
            if (compound.contains(FLOATING, Tag.TAG_BYTE)) {
                f.setFloating(compound.getBoolean(FLOATING));
            }
            if (compound.contains(CREATIVE_HOVERING, Tag.TAG_BYTE)) {
                f.setCreativeHovering(compound.getBoolean(CREATIVE_HOVERING));
            }
            if (compound.contains(CREATIVE_HOVER_FLAP_RATE, Tag.TAG_DOUBLE)) {
                f.setCreativeHoverFlapRate(compound.getDouble(CREATIVE_HOVER_FLAP_RATE));
            }
            return f;
        }
    }

    private final class WingState {
        private final FlightApparatus apparatus;

        private final FlightApparatus.FlightState activity;

        private WingState(FlightApparatus apparatus, FlightApparatus.FlightState activity) {
            this.apparatus = apparatus;
            this.activity = activity;
        }

        private WingState notFlying() {
            return FlightDefault.this.voidState;
        }

        private WingState next(FlightApparatus wf) {
            if (this.apparatus.equals(wf)) {
                return this;
            }
            return new WingState(wf, wf.createState(FlightDefault.this));
        }

        private void onUpdate(Player player) {
            this.activity.onUpdate(player);
        }
    }

    private static FlightApparatus getEquippedWing(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof WingsArmorItem wingsItem) {
            return wingsItem.getWing();
        }
        return FlightApparatus.NONE;
    }

    private static boolean hasWingsItem(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return chest.getItem() instanceof WingsArmorItem;
    }

    private static boolean isCreativeHovering(Player player) {
        return player.getAbilities().instabuild && player.getAbilities().flying && !player.onGround();
    }
}
