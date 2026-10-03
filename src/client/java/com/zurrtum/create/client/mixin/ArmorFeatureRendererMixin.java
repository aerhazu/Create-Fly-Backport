package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.item.LayeredArmorItem;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    private ArmorFeatureRendererMixin(FeatureRendererContext<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V"))
    private void renderArmorPiece(
        ArmorFeatureRenderer<T, M, A> instance,
        MatrixStack poseStack,
        VertexConsumerProvider vertexConsumers,
        T entity,
        EquipmentSlot slot,
        int light,
        A armorModel,
        Operation<Void> original,
        @Local(ordinal = 0, argsOnly = true) float limbAngle,
        @Local(ordinal = 1, argsOnly = true) float limbDistance,
        @Local(ordinal = 3, argsOnly = true) float animationProgress,
        @Local(ordinal = 4, argsOnly = true) float headYaw,
        @Local(ordinal = 5, argsOnly = true) float headPitch
    ) {
        original.call(instance, poseStack, vertexConsumers, entity, slot, light, armorModel);
        ItemStack stack = entity.getEquippedStack(slot);
        if (stack.getItem() instanceof LayeredArmorItem item) {
            VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(
                vertexConsumers,
                RenderLayer.getArmorCutoutNoCull(item.getLayerTexture()),
                stack.hasGlint()
            );
            M model = getContextModel();
            model.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
            model.render(poseStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV, -1);
        }
    }
}
