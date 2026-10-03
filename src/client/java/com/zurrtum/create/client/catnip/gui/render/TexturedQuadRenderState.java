package com.zurrtum.create.client.catnip.gui.render;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.render.PonderRenderPipelines;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public record TexturedQuadRenderState(
    Matrix4f pose, float left, float right, float top, float bot, int red, int green, int blue, int alpha, float u1, float u2, float v1, float v2,
    Identifier texture
) {
    public TexturedQuadRenderState(
        Matrix4f pose,
        Identifier texture,
        int left,
        int right,
        int top,
        int bot,
        Color color,
        float u1,
        float u2,
        float v1,
        float v2
    ) {
        this(pose, left, right, top, bot, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), u1, u2, v1, v2, texture);
    }

    public void render(VertexConsumerProvider vertexConsumers) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(PonderRenderPipelines.guiTextured(texture));
        float depth = 0;
        vertexConsumer.vertex(pose, left, bot, depth).color(red, green, blue, alpha).texture(u1, v2);
        vertexConsumer.vertex(pose, right, bot, depth).color(red, green, blue, alpha).texture(u2, v2);
        vertexConsumer.vertex(pose, right, top, depth).color(red, green, blue, alpha).texture(u2, v1);
        vertexConsumer.vertex(pose, left, top, depth).color(red, green, blue, alpha).texture(u1, v1);
    }
}
