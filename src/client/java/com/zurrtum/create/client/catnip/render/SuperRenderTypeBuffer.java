package com.zurrtum.create.client.catnip.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

public interface SuperRenderTypeBuffer extends VertexConsumerProvider {
    VertexConsumer getEarlyBuffer(RenderLayer type);

    VertexConsumer getBuffer(RenderLayer type);

    VertexConsumer getLateBuffer(RenderLayer type);

    default RenderLayer getRenderLayer(RenderLayer type) {
        return type == RenderLayer.getTranslucent() ? PonderRenderTypes.translucent() : type;
    }

    void draw();

    void draw(RenderLayer type);
}
