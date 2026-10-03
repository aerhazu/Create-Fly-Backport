package com.zurrtum.create.client.catnip.gui.render;

import com.zurrtum.create.client.catnip.render.PonderRenderPipelines;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public record TextureArrowRenderState(Matrix4f pose, float alpha, float tx, float ty, float tw, float th, Identifier texture) {
    public TextureArrowRenderState(Matrix4f pose, int size, float alpha, Identifier texture, float tx, float ty, float tw, float th) {
        this(pose, alpha, tx, ty, tw, th, texture);
    }

    public void render(VertexConsumerProvider vertexConsumers) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(PonderRenderPipelines.guiTextured(texture));
        float depth = 0;
        vertexConsumer.vertex(pose, -1, -1, depth).color(1f, 1f, 1f, alpha).texture(tx, ty);
        vertexConsumer.vertex(pose, -1, 1, depth).color(1f, 1f, 1f, alpha).texture(tx, ty + th);
        vertexConsumer.vertex(pose, 1, 1, depth).color(1f, 1f, 1f, alpha).texture(tx + tw, ty + th);
        vertexConsumer.vertex(pose, 1, -1, depth).color(1f, 1f, 1f, alpha).texture(tx + tw, ty);
    }
}
