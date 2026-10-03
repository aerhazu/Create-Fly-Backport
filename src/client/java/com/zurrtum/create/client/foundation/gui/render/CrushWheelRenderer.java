package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class CrushWheelRenderer {
    private static final BlockState blockState = AllBlocks.CRUSHING_WHEEL.getDefaultState().with(Properties.AXIS, Axis.X);

    public static void render(DrawContext graphics, float x, float y) {
        MatrixStack matrices = MachinePreviewHelper.begin(graphics, x, y, 92, 48, 22);

        MinecraftClient mc = MinecraftClient.getInstance();
        DiffuseLighting.enableGuiDepthLighting();
        matrices.scale(1, 1, -1);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-22.5f));
        matrices.translate(-1.5f, -0.6f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumerProvider.Immediate vertexConsumers = MachinePreviewHelper.buffer();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        Random random = mc.world.random;
        world.blockState(blockState);

        float angle = getCurrentAngle();
        matrices.push();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-angle));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);
        matrices.pop();

        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.translate(2, 0, 0);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);

        MachinePreviewHelper.end(matrices, vertexConsumers);
    }

    public static float getCurrentAngle() {
        return (AnimationTickHolder.getRenderTime() * 4f) % 360;
    }
}
