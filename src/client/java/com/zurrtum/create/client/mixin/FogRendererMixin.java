package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.FogUniforms;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class FogRendererMixin {
    @Inject(
        method = "applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZF)V",
        at = @At("TAIL")
    )
    private static void applyFog(
        Camera camera,
        BackgroundRenderer.FogType fogType,
        float viewDistance,
        boolean thickFog,
        float tickDelta,
        CallbackInfo ci
    ) {
        float[] color = RenderSystem.getShaderFogColor();
        float start = RenderSystem.getShaderFogStart();
        float end = RenderSystem.getShaderFogEnd();
        FogUniforms.update(new Vector4f(color[0], color[1], color[2], color[3]), start, end, start, end);
    }
}
