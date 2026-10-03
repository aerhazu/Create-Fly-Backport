package com.zurrtum.create.client.catnip.gui.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;

import static com.zurrtum.create.client.catnip.render.PonderRenderPipelines.TRIANGLE_FAN;

public record ArrowRenderState(Matrix4f pose, float r, float g, float b, float a, float length) {
    public ArrowRenderState(Matrix4f pose, int size, float r, float g, float b, float a, float length) {
        this(pose, r, g, b, a, length);
    }

    public void render(VertexConsumerProvider vertexConsumers) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(TRIANGLE_FAN);
        float depth = 0;
        vertexConsumer.vertex(pose, 0, -(10 + length), depth).color(r, g, b, a);
        vertexConsumer.vertex(pose, -9, -3, depth).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, -6, -6, depth).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, -3, -8, depth).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, 0, -8.5f, depth).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, 3, -8, depth).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, 6, -6, depth).color(r, g, b, 0f);
        vertexConsumer.vertex(pose, 9, -3, depth).color(r, g, b, 0f);
    }
}
