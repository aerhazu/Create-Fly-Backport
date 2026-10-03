package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
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

public class PressBasinRenderer {
    public static void render(DrawContext graphics, float x, float y) {
        MatrixStack matrices = MachinePreviewHelper.begin(graphics, x, y, 30, 80, 23);

        MinecraftClient mc = MinecraftClient.getInstance();
        DiffuseLighting.enableGuiDepthLighting();
        matrices.scale(1, 1, -1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -1.8f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumerProvider.Immediate vertexConsumers = MachinePreviewHelper.buffer();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        Random random = mc.world.random;
        float time = AnimationTickHolder.getRenderTime();

        blockState = AllBlocks.MECHANICAL_PRESS.getDefaultState();
        world.blockState(blockState);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);

        matrices.push();
        blockState = AllBlocks.SHAFT.getDefaultState().with(Properties.AXIS, Axis.Z);
        world.blockState(blockState);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(getShaftAngle(time)));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);
        matrices.pop();

        matrices.push();
        blockState = Blocks.AIR.getDefaultState();
        world.blockState(blockState);
        matrices.translate(0, getAnimatedHeadOffset(time), 0);
        blockRenderManager.getModelRenderer().render(
            world,
            AllPartialModels.MECHANICAL_PRESS_HEAD.get(),
            blockState,
            BlockPos.ORIGIN,
            matrices,
            buffer,
            false,
            random,
            42L,
            OverlayTexture.DEFAULT_UV
        );
        matrices.pop();

        matrices.translate(0, -1.65f, 0);
        blockState = AllBlocks.BASIN.getDefaultState();
        world.blockState(blockState);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);

        MachinePreviewHelper.end(matrices, vertexConsumers);
    }

    private static float getShaftAngle(float time) {
        return (time * 4f) % 360;
    }

    private static float getAnimatedHeadOffset(float time) {
        float cycle = time % 30;
        if (cycle < 10) {
            float progress = cycle / 10;
            return -(progress * progress * progress);
        }
        if (cycle < 15)
            return -1;
        if (cycle < 20)
            return -1 + (1 - ((20 - cycle) / 5));
        return 0;
    }
}
