package com.zurrtum.create.client.model;

import com.google.common.base.Supplier;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import org.jetbrains.annotations.Nullable;

public interface LayerBakedModel {
    static RenderLayer getBlockRenderLayer(BakedModel model, Supplier<RenderLayer> defaultLayer) {
        if (model instanceof LayerBakedModel layered) {
            RenderLayer layer = layered.create$getBlockRenderLayer();
            if (layer != null) {
                return layer;
            }
        }
        return defaultLayer.get();
    }

    @Nullable
    default RenderLayer create$getBlockRenderLayer() {
        return null;
    }
}
