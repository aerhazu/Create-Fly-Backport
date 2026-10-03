package com.zurrtum.create.client.catnip.gui.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;

import static com.zurrtum.create.client.catnip.render.PonderRenderPipelines.TRIANGLE_FAN;

public record DirectionIndicatorRenderState(Matrix4f pose, float r, float g, float b) {
    public void render(VertexConsumerProvider vertexConsumers) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(TRIANGLE_FAN);
        float depth = 0;
        vertexConsumer.vertex(pose, 0, 0, depth).color(r, g, b, 1);
        vertexConsumer.vertex(pose, 5, -5, depth).color(r, g, b, 0.6f);
        vertexConsumer.vertex(pose, 3, -4.5f, depth).color(r, g, b, 0.7f);
        vertexConsumer.vertex(pose, 0, -4.2f, depth).color(r, g, b, 0.7f);
        vertexConsumer.vertex(pose, -3, -4.5f, depth).color(r, g, b, 0.7f);
        vertexConsumer.vertex(pose, -5, -5, depth).color(r, g, b, 0.6f);
    }
}
