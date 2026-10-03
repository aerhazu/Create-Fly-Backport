package com.zurrtum.create.client.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderLayer.MultiPhaseParameters.class)
public interface RenderLayerMultiPhaseParametersAccessor {
    @Accessor("texture")
    RenderPhase.TextureBase getTexture();
}
