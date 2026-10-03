package com.zurrtum.create.client.catnip.gui.render;

import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;

public record GradientRectRenderState(
    Matrix4f pose, float left, float top, float right, float bottom, int startRed, int startGreen, int startBlue, int startAlpha, int endRed,
    int endGreen, int endBlue, int endAlpha
) {
    public GradientRectRenderState(Matrix4f pose, float left, float top, float right, float bottom, Color startColor, Color endColor) {
        this(
            pose,
            left,
            top,
            right,
            bottom,
            startColor.getRed(),
            startColor.getGreen(),
            startColor.getBlue(),
            startColor.getAlpha(),
            endColor.getRed(),
            endColor.getGreen(),
            endColor.getBlue(),
            endColor.getAlpha()
        );
    }

    public void render(VertexConsumerProvider vertexConsumers) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getDebugQuads());
        float depth = 0;
        vertexConsumer.vertex(pose, right, top, depth).color(startRed, startGreen, startBlue, startAlpha);
        vertexConsumer.vertex(pose, left, top, depth).color(startRed, startGreen, startBlue, startAlpha);
        vertexConsumer.vertex(pose, left, bottom, depth).color(endRed, endGreen, endBlue, endAlpha);
        vertexConsumer.vertex(pose, right, bottom, depth).color(endRed, endGreen, endBlue, endAlpha);
    }
}
