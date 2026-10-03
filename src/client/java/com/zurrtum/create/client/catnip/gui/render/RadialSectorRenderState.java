package com.zurrtum.create.client.catnip.gui.render;

import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Vec2f;
import org.joml.Matrix4f;

import java.util.List;

import static com.zurrtum.create.client.catnip.render.PonderRenderPipelines.POSITION_COLOR_STRIP;

public record RadialSectorRenderState(Matrix4f pose, List<Vec2f> innerPoints, List<Vec2f> outerPoints, int outerColor, int innerColor) {
    public RadialSectorRenderState(
        Matrix4f pose,
        double minX,
        double maxX,
        double minY,
        double maxY,
        List<Vec2f> innerPoints,
        List<Vec2f> outerPoints,
        Color innerColor,
        Color outerColor
    ) {
        this(pose, innerPoints, outerPoints, outerColor.getRGB(), innerColor.getRGB());
    }

    public void render(VertexConsumerProvider vertexConsumers) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(POSITION_COLOR_STRIP);
        float depth = 0;
        for (int i = 0; i < innerPoints.size(); i++) {
            Vec2f point = outerPoints.get(i);
            vertexConsumer.vertex(pose, point.x, point.y, depth).color(outerColor);

            point = innerPoints.get(i);
            vertexConsumer.vertex(pose, point.x, point.y, depth).color(innerColor);
        }
    }
}
