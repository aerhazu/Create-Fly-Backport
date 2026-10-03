package com.zurrtum.create.client.ponder.foundation.render;

import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.lang.ClientFontHelper;
import com.zurrtum.create.client.foundation.gui.render.MachinePreviewHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class TitleTextRenderer {
    public static void render(DrawContext graphics, float x, float y, float diff, String title, String otherTitle) {
        MatrixStack matrices = MachinePreviewHelper.begin(graphics, x, y, 180, 20, 1);
        matrices.scale(1, 1, -1);
        matrices.translate(-90, -20, 0);

        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        float absoluteIndexDiff = Math.abs(diff);
        float angle = diff * -90;
        matrices.translate(0, 6, 0);
        VertexConsumerProvider.Immediate vertexConsumers = MachinePreviewHelper.buffer();

        matrices.push();
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(angle + Math.signum(diff) * 90));
        matrices.translate(0, -6, 5);
        ClientFontHelper.drawSplitString(
            vertexConsumers,
            matrices,
            font,
            otherTitle,
            0,
            0,
            180,
            UIRenderHelper.COLOR_TEXT.getFirst().scaleAlphaForText(absoluteIndexDiff).getRGB()
        );
        matrices.pop();

        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(angle));
        matrices.translate(0, -6, 5);
        ClientFontHelper.drawSplitString(
            vertexConsumers,
            matrices,
            font,
            title,
            0,
            0,
            180,
            UIRenderHelper.COLOR_TEXT.getFirst().scaleAlphaForText(1 - absoluteIndexDiff).getRGB()
        );

        MachinePreviewHelper.end(matrices, vertexConsumers);
    }
}
