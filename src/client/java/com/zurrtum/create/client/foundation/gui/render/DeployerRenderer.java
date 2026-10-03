package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlock;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class DeployerRenderer {
    public static void render(DrawContext graphics, float x, float y) {
        render(graphics, x, y, 0);
    }

    public static void render(DrawContext graphics, float x, float y, int offsetIndex) {
        MatrixStack matrices = MachinePreviewHelper.begin(graphics, x, y, 26, 75, 20);

        MinecraftClient mc = MinecraftClient.getInstance();
        DiffuseLighting.enableGuiDepthLighting();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -2.24f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumerProvider.Immediate vertexConsumers = MachinePreviewHelper.buffer();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        Random random = mc.world.random;
        float time = AnimationTickHolder.getRenderTime();
        float cycle = (time - offsetIndex * 8) % 30;
        float offset = cycle < 10 ? cycle / 10f : cycle < 20 ? (20 - cycle) / 10f : 0;

        matrices.push();
        blockState = AllBlocks.SHAFT.getDefaultState().with(Properties.AXIS, Direction.Axis.Z);
        world.blockState(blockState);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(getCurrentAngle(time)));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);
        matrices.pop();

        blockState = AllBlocks.DEPLOYER.getDefaultState().with(DeployerBlock.FACING, Direction.DOWN)
            .with(DeployerBlock.AXIS_ALONG_FIRST_COORDINATE, false);
        world.blockState(blockState);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);

        matrices.push();
        blockState = Blocks.AIR.getDefaultState();
        world.blockState(blockState);
        matrices.translate(0, -offset, 0);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.getModelRenderer()
            .render(world, AllPartialModels.DEPLOYER_POLE.get(), blockState, BlockPos.ORIGIN, matrices, buffer, false, random, 42L, OverlayTexture.DEFAULT_UV);
        blockRenderManager.getModelRenderer().render(
            world,
            AllPartialModels.DEPLOYER_HAND_HOLDING.get(),
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

        matrices.translate(0, -2.06f, 0);
        blockState = AllBlocks.DEPOT.getDefaultState();
        world.blockState(blockState);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);

        MachinePreviewHelper.end(matrices, vertexConsumers);
    }

    public static float getCurrentAngle(float time) {
        return (time * 4f) % 360;
    }
}
