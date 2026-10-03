package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.foundation.render.PlayerSkyhookRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin {
    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void afterSetupAnim(
        LivingEntity entity,
        float limbAngle,
        float limbDistance,
        float animationProgress,
        float headYaw,
        float headPitch,
        CallbackInfo ci
    ) {
        if (!(entity instanceof AbstractClientPlayerEntity player)) {
            return;
        }
        PlayerSkyhookRenderer.afterSetupAnim(player.getUuid(), player.getMainArm(), player.getMainHandStack(), (PlayerEntityModel) (Object) this);
    }
}
