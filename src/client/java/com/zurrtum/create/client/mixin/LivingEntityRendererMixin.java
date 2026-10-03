package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.content.equipment.armor.BacktankFeatureRenderer;
import com.zurrtum.create.client.content.equipment.armor.CardboardArmorHandlerClient;
import com.zurrtum.create.client.content.equipment.hats.HatFeatureRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow
    @Final
    protected List<FeatureRenderer<T, M>> features;

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("TAIL"))
    private void addFeature(EntityRendererFactory.Context ctx, M model, float shadowRadius, CallbackInfo ci) {
        if (model instanceof BipedEntityModel) {
            BacktankFeatureRenderer<T, BipedEntityModel<T>> renderer = new BacktankFeatureRenderer<>((FeatureRendererContext<T, BipedEntityModel<T>>) (FeatureRendererContext<?, ?>) this);
            features.add((FeatureRenderer<T, M>) renderer);
        }
        LivingEntityRenderer<T, M> renderer = (LivingEntityRenderer<T, M>) (Object) this;
        if (!(renderer instanceof ShulkerEntityRenderer || renderer instanceof ArmorStandEntityRenderer)) {
            features.add(new HatFeatureRenderer<>(renderer));
        }
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void render(T entity, float yaw, float partialTicks, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo ci) {
        if ((LivingEntityRenderer<T, M>) (Object) this instanceof PlayerEntityRenderer renderer && entity instanceof AbstractClientPlayerEntity player && CardboardArmorHandlerClient.playerRendersAsBoxWhenSneaking(
            renderer,
            player,
            partialTicks,
            matrixStack,
            vertexConsumerProvider,
            light
        )) {
            ci.cancel();
        }
    }
}
