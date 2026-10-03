package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.LevelUniforms;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class DiffuseLightingMixin {
    @Inject(method = "setShaderLights(Lorg/joml/Vector3f;Lorg/joml/Vector3f;)V", at = @At("TAIL"))
    private static void setShaderLights(Vector3f light0Diffusion, Vector3f light1Diffusion, CallbackInfo ci) {
        LevelUniforms.updateLights(light0Diffusion, light1Diffusion);
    }
}
