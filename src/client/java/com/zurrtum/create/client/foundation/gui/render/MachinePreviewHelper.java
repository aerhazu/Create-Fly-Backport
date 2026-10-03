package com.zurrtum.create.client.foundation.gui.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Backport replacement for 1.21.8's deferred {@code SpecialGuiElementRenderer} machinery: these small
 * 3D machine previews (used in JEI/REI recipe viewers and other UIs) are rendered directly into the
 * current frame instead of being baked into a cached offscreen texture. All previews share the same
 * bottom-center anchored box convention that the original renderers already assumed.
 */
public class MachinePreviewHelper {
    public static MatrixStack begin(DrawContext graphics, float x, float y, float width, float yAnchor, float scale) {
        MatrixStack matrices = graphics.getMatrices();
        matrices.push();
        matrices.translate(x + width / 2f, y + yAnchor, 100);
        matrices.scale(scale, scale, scale);
        return matrices;
    }

    public static VertexConsumerProvider.Immediate buffer() {
        return MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
    }

    public static void end(MatrixStack matrices, VertexConsumerProvider.Immediate buffer) {
        buffer.draw();
        matrices.pop();
    }
}
