package com.toni.wings.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.toni.wings.WingsMod;
import com.toni.wings.client.audio.WingsSound;
import com.toni.wings.client.flight.FlightView;
import com.toni.wings.client.flight.FlightViews;
import com.toni.wings.server.asm.AnimatePlayerModelEvent;
import com.toni.wings.server.asm.ApplyPlayerRotationsEvent;
import com.toni.wings.server.asm.EmptyOffHandPresentEvent;
import com.toni.wings.server.asm.GetCameraEyeHeightEvent;
import com.toni.wings.server.flight.Flights;
import com.toni.wings.server.flight.FlightPose;
import com.toni.wings.server.config.WingsClientConfig;
import com.toni.wings.util.MathH;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.blaze3d.systems.RenderSystem;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WingsMod.ID)
public final class ClientEventHandler {
    private static ResourceKey<Level> lastPlayerDimension;
    private static final int POSE_PREVIEW_TICKS = 30;
    private static final int POSE_FADE_OUT_TICKS = 30;
    private static int posePreviewTicks;
    private static boolean renderingPosePreview;

    private ClientEventHandler() {
    }

    public static void beginPosePreview() {
        if (!WingsClientConfig.isPosePreviewEnabled() || !isFirstPersonCamera()) {
            posePreviewTicks = 0;
            return;
        }
        posePreviewTicks = POSE_PREVIEW_TICKS;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            lastPlayerDimension = null;
            return;
        }
        if (!WingsClientConfig.isPosePreviewEnabled() || !isFirstPersonCamera()) {
            posePreviewTicks = 0;
        } else if (posePreviewTicks > 0) {
            posePreviewTicks--;
        }
        Flights.get(player).ifPresent(flight -> {
            if (flight.isFloating() && isMovementKeyDown()) {
                flight.setFloating(false);
                WingsMod.instance().network().sendToServer(new com.toni.wings.server.net.serverbound.MessageSetFloating(false));
            }
        });
        ResourceKey<Level> current = player.level().dimension();
        if (current != lastPlayerDimension) {
            lastPlayerDimension = current;
            FlightViews.invalidate(player);
        }
    }

    @SubscribeEvent
    public static void onAnimatePlayerModel(AnimatePlayerModelEvent event) {
        Player player = event.getEntity();
        Flights.get(player).ifPresent(flight -> {
            float delta = event.getTicksExisted() - player.tickCount;
            float amt = flight.getFlyingAmount(delta);
            if (amt == 0.0F) return;
            if (isLocalFirstPerson(player)) return;
            PlayerModel<?> model = event.getModel();
            float pitch = event.getPitch();
            model.head.xRot = MathH.toRadians(MathH.lerp(pitch, pitch / 4.0F - 90.0F, amt));
            applyFlightPose(model, player, flight.getPose(), amt);
            model.leftLeg.xRot = MathH.lerp(model.leftLeg.xRot, 0.0F, amt);
            model.rightLeg.xRot = MathH.lerp(model.rightLeg.xRot, 0.0F, amt);
            model.hat.copyFrom(model.head);
        });
    }

    @SubscribeEvent
    public static void onApplyRotations(ApplyPlayerRotationsEvent event) {
        Flights.ifPlayer(event.getEntity(), (player, flight) -> {
            PoseStack matrixStack = event.getMatrixStack();
            float delta = event.getDelta();
            float amt = flight.getFlyingAmount(delta);
            if (amt > 0.0F) {
                float roll = MathH.lerpDegrees(
                    player.yBodyRotO - player.yRotO,
                    player.yBodyRot - player.getYRot(),
                    delta
                );
                float pitch = -MathH.lerpDegrees(player.xRotO, player.getXRot(), delta) - 90.0F;
                matrixStack.mulPose(Axis.ZP.rotationDegrees(MathH.lerpDegrees(0.0F, roll, amt)));
                matrixStack.mulPose(Axis.XP.rotationDegrees(MathH.lerpDegrees(0.0F, pitch, amt)));
                matrixStack.translate(0.0D, -1.2D * MathH.easeInOut(amt), 0.0D);
            }
        });
    }

    @SubscribeEvent
    public static void onGetCameraEyeHeight(GetCameraEyeHeightEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LocalPlayer) {
            FlightViews.get((LocalPlayer) entity).ifPresent(flight ->
                flight.tickEyeHeight(event.getValue(), event::setValue)
            );
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Flights.ifPlayer(event.getCamera().getEntity(), (player, flight) -> {
            float delta = (float) event.getPartialTick();
            float amt = flight.getFlyingAmount(delta);
            if (amt > 0.0F) {
                float roll = MathH.lerpDegrees(
                    player.yBodyRotO - player.yRotO,
                    player.yBodyRot - player.getYRot(),
                    delta
                );
                float targetRoll = MathH.lerpDegrees(0.0F, -roll * 0.25F, amt);
                if (!Float.isFinite(targetRoll) || Math.abs(targetRoll) > 75.0F) {
                    targetRoll = 0.0F;
                } else {
                    targetRoll = Mth.clamp(targetRoll, -60.0F, 60.0F);
                }
                event.setRoll(targetRoll);
            }
        });
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!WingsClientConfig.isPosePreviewEnabled() || posePreviewTicks <= 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!isFirstPersonCamera()) {
            posePreviewTicks = 0;
            return;
        }
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        GuiGraphics gui = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int scale = 40;
        int x = width - 60;
        int y = height - 30;
        float alpha = 1.0F;
        if (posePreviewTicks <= POSE_FADE_OUT_TICKS) {
            alpha = Mth.clamp(posePreviewTicks / (float) POSE_FADE_OUT_TICKS, 0.0F, 1.0F);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        renderingPosePreview = true;
        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse(gui, x, y, scale, 20.0F, 20.0F, player);
        } finally {
            renderingPosePreview = false;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public static void onEmptyOffHandPresentEvent(EmptyOffHandPresentEvent event) {
        if (isLocalFirstPerson(event.getPlayer())) {
            return;
        }
        Flights.get(event.getPlayer()).ifPresent(flight -> {
            if (flight.isFlying()) {
                event.setResult(Event.Result.ALLOW);
            }
        });
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Flights.ifPlayer(event.getEntity(), Player::isLocalPlayer, (player, flight) ->
            Minecraft.getInstance().getSoundManager().play(new WingsSound(player, flight))
        );
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player entity = event.player;
        if (event.phase == TickEvent.Phase.END && entity instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer) entity;
            FlightViews.get(player).ifPresent(FlightView::tick);
        }
    }

    private static void applyFlightPose(PlayerModel<?> model, Player player, FlightPose pose, float amt) {
        if (pose == null) {
            pose = FlightPose.DEFAULT;
        }
        switch (pose) {
            case MAIN_HAND_FORWARD -> {
                HumanoidArm main = player.getMainArm();
                if (main == HumanoidArm.RIGHT) {
                    model.rightArm.xRot = MathH.lerp(model.rightArm.xRot, -3.2F, amt);
                    model.leftArm.xRot = MathH.lerp(model.leftArm.xRot, -0.6F, amt);
                    model.leftArm.zRot = MathH.lerp(model.leftArm.zRot, 0.15F, amt);
                } else {
                    model.leftArm.xRot = MathH.lerp(model.leftArm.xRot, -3.2F, amt);
                    model.rightArm.xRot = MathH.lerp(model.rightArm.xRot, -0.6F, amt);
                    model.rightArm.zRot = MathH.lerp(model.rightArm.zRot, -0.15F, amt);
                }
            }
            case HANDS_AT_SIDES -> {
                model.leftArm.xRot = MathH.lerp(model.leftArm.xRot, 0.1F, amt);
                model.rightArm.xRot = MathH.lerp(model.rightArm.xRot, 0.1F, amt);
                model.leftArm.zRot = MathH.lerp(model.leftArm.zRot, 0.6F, amt);
                model.rightArm.zRot = MathH.lerp(model.rightArm.zRot, -0.6F, amt);
            }
            case HANDS_AT_SIDES_OUT -> {
                model.leftArm.xRot = MathH.lerp(model.leftArm.xRot, 0.15F, amt);
                model.rightArm.xRot = MathH.lerp(model.rightArm.xRot, 0.15F, amt);
                model.leftArm.yRot = MathH.lerp(model.leftArm.yRot, 0.4F, amt);
                model.rightArm.yRot = MathH.lerp(model.rightArm.yRot, -0.4F, amt);
                model.leftArm.zRot = MathH.lerp(model.leftArm.zRot, 0.2F, amt);
                model.rightArm.zRot = MathH.lerp(model.rightArm.zRot, -0.2F, amt);
            }
            default -> {
                model.leftArm.xRot = MathH.lerp(model.leftArm.xRot, -3.2F, amt);
                model.rightArm.xRot = MathH.lerp(model.rightArm.xRot, -3.2F, amt);
            }
        }
    }

    private static boolean isMovementKeyDown() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options == null) {
            return false;
        }
        return mc.options.keyUp.isDown()
            || mc.options.keyDown.isDown()
            || mc.options.keyLeft.isDown()
            || mc.options.keyRight.isDown()
            || mc.options.keyJump.isDown()
            || mc.options.keyShift.isDown();
    }

    private static boolean isLocalFirstPerson(Player player) {
        if (renderingPosePreview) {
            return false;
        }
        if (!(player instanceof LocalPlayer localPlayer) || !localPlayer.isLocalPlayer()) {
            return false;
        }
        return isFirstPersonCamera();
    }

    private static boolean isFirstPersonCamera() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.options != null && mc.options.getCameraType().isFirstPerson();
    }
}
