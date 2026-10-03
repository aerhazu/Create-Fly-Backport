package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.AllExtensions;
import com.zurrtum.create.client.content.equipment.armor.ArmTextureOverride;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "getArmPose(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;", at = @At(value = "HEAD"), cancellable = true)
    private static void getArmPose(AbstractClientPlayerEntity player, Hand hand, CallbackInfoReturnable<ArmPose> cir) {
        Item item = player.getStackInHand(hand).getItem();
        ArmPose pose = AllExtensions.ARM_POSE.get(item);
        if (pose != null) {
            cir.setReturnValue(pose);
        }
    }

    // 1.21.1's private renderArm resolves the skin texture internally via player.getSkinTextures().texture()
    // instead of taking an Identifier parameter, so HeldItemRendererMixin swaps ArmTextureOverride.current
    // instead of passing an alternate texture directly, to render a second arm pass with an overlay texture.
    @WrapOperation(method = "renderArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SkinTextures;texture()Lnet/minecraft/util/Identifier;"))
    private static Identifier create$overrideArmTexture(SkinTextures instance, Operation<Identifier> original) {
        return ArmTextureOverride.current != null ? ArmTextureOverride.current : original.call(instance);
    }
}
