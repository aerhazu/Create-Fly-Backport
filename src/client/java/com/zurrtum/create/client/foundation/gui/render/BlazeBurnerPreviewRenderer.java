package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class BlazeBurnerPreviewRenderer {
    public static void render(
        DrawContext graphics,
        float x,
        float y,
        World world,
        BlockState block,
        HeatLevel heatLevel,
        float animation,
        boolean drawGoggles,
        int hash
    ) {
        MatrixStack matrices = MachinePreviewHelper.begin(graphics, x, y, 68, 68 / 1.6f, 48);

        DiffuseLighting.enableGuiDepthLighting();
        matrices.scale(1, 1, -1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-22.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45));
        matrices.scale(1, -1, 1);
        float horizontalAngle = AngleHelper.rad(270);
        boolean canDrawFlame = heatLevel.isAtLeast(HeatLevel.FADING);
        PartialModel drawHat = AllPartialModels.LOGISTICS_HAT;

        VertexConsumerProvider.Immediate vertexConsumers = MachinePreviewHelper.buffer();
        VertexConsumer cutout = vertexConsumers.getBuffer(RenderLayer.getCutoutMipped());
        CachedBuffers.partial(AllPartialModels.BLAZE_CAGE, block).rotateCentered(horizontalAngle + MathHelper.PI, Direction.UP)
            .light(LightmapTextureManager.MAX_LIGHT_COORDINATE).renderInto(matrices, cutout);

        BlazeBurnerRenderer.renderShared(
            matrices,
            null,
            vertexConsumers,
            world,
            block,
            heatLevel,
            animation,
            horizontalAngle,
            canDrawFlame,
            drawGoggles,
            drawHat,
            hash
        );

        MachinePreviewHelper.end(matrices, vertexConsumers);
    }
}
