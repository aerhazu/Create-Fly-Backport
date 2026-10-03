package com.zurrtum.create.client.catnip.client.render.model;

import net.minecraft.client.render.RenderLayer;

import net.minecraft.client.render.VertexConsumer;

public interface ShadeSeparatedBufferSource {
    VertexConsumer getBuffer(RenderLayer chunkRenderType, boolean shade);
}
