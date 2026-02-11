package com.toni.wings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.toni.wings.WingsMod;
import com.toni.wings.client.flight.FlightViews;
import com.toni.wings.client.model.ModelWingsAvian;
import com.toni.wings.client.model.ModelWingsInsectoid;
import com.toni.wings.server.item.WingsArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import javax.annotation.Nonnull;

public final class LayerWings extends RenderLayer<LivingEntity, HumanoidModel<LivingEntity>> {
    private final TransformFunction transform;

    public static final ModelLayerLocation INSECTOID_WINGS = layer("insectoid_wings");
    public static final ModelLayerLocation AVIAN_WINGS = layer("avian_wings");

    public static void init(IEventBus modBus)
    {
        modBus.addListener(LayerWings::initLayers);
    }

    public LayerWings(LivingEntityRenderer<LivingEntity, HumanoidModel<LivingEntity>> renderer, TransformFunction transform) {
        super(renderer);
        this.transform = transform;
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int packedLight, @Nonnull LivingEntity player, float limbSwing, float limbSwingAmount, float delta, float age, float headYaw, float headPitch) {
        if (!player.isInvisible()) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.getItem() instanceof WingsArmorItem wingsItem && !wingsItem.shouldRenderWingModel(chest)) {
                return;
            }
            WingsArmorItem.PartColors partColors = chest.getItem() instanceof WingsArmorItem wingsItem
                ? wingsItem.getPartColors(chest)
                : WingsArmorItem.PartColors.uniform(0xFFFFFF);
            FlightViews.get(player).ifPresent(flight -> {
                flight.ifFormPresent(form -> {
                    VertexConsumer builder = SodiumBypassVertexConsumer.wrap(buffer.getBuffer(form.getRenderType()));
                    matrixStack.pushPose();
                    this.transform.apply(player, matrixStack);
                    if (!form.renderPartColors(matrixStack, builder, packedLight, OverlayTexture.NO_OVERLAY, partColors, 1.0F, delta)) {
                        float[] color = getColorFloats(partColors.blendedColor());
                        form.render(matrixStack, builder, packedLight, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], 1.0F, delta);
                    }
                    matrixStack.popPose();
                });
            });
        }
    }

    public static void initLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(INSECTOID_WINGS, ModelWingsInsectoid::createBodyLayer);
        event.registerLayerDefinition(AVIAN_WINGS, ModelWingsAvian::createBodyLayer);
    }

    private static ModelLayerLocation layer(String name)
    {
        return layer(name, "main");
    }

    private static ModelLayerLocation layer(String name, String layer)
    {
        return new ModelLayerLocation(WingsMod.locate(name), layer);
    }

    @FunctionalInterface
    public interface TransformFunction {
        void apply(LivingEntity player, PoseStack stack);
    }

    private static float[] getColorFloats(int color) {
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        return new float[]{red, green, blue};
    }
}
