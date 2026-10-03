package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.content.kinetics.saw.SawBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class SawRenderer {
    public static void render(DrawContext graphics, float x, float y) {
        MatrixStack matrices = MachinePreviewHelper.begin(graphics, x, y, 33, 33, 25);

        MinecraftClient mc = MinecraftClient.getInstance();
        DiffuseLighting.enableGuiDepthLighting();
        matrices.scale(1, 1, -1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(112.5f));
        matrices.translate(-0.5f, -0.2f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumerProvider.Immediate vertexConsumers = MachinePreviewHelper.buffer();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        Random random = mc.world.random;

        matrices.push();
        blockState = AllBlocks.SHAFT.getDefaultState().with(Properties.AXIS, Axis.X);
        world.blockState(blockState);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(getCurrentAngle()));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);
        matrices.pop();

        blockState = AllBlocks.MECHANICAL_SAW.getDefaultState().with(SawBlock.FACING, Direction.UP);
        world.blockState(blockState);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);

        blockState = Blocks.AIR.getDefaultState();
        world.blockState(blockState);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.getModelRenderer().render(
            world,
            AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE.get(),
            blockState,
            BlockPos.ORIGIN,
            matrices,
            vertexConsumers.getBuffer(RenderLayer.getCutoutMipped()),
            false,
            random,
            42L,
            OverlayTexture.DEFAULT_UV
        );

        MachinePreviewHelper.end(matrices, vertexConsumers);
    }

    public static float getCurrentAngle() {
        return -(AnimationTickHolder.getRenderTime() * 4f) % 360;
    }
}
