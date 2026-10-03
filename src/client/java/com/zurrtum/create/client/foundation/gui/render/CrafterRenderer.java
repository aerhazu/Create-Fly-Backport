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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class CrafterRenderer {
    public static void render(DrawContext graphics, float x, float y) {
        MatrixStack matrices = MachinePreviewHelper.begin(graphics, x, y, 29, 29, 22);

        MinecraftClient mc = MinecraftClient.getInstance();
        DiffuseLighting.enableGuiDepthLighting();
        matrices.scale(1, 1, -1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-22.5f));
        matrices.translate(-0.5f, -0.16f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumerProvider.Immediate vertexConsumers = MachinePreviewHelper.buffer();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        Random random = mc.world.random;

        blockState = Blocks.AIR.getDefaultState();
        world.blockState(blockState);
        matrices.push();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(getCurrentAngle()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.getModelRenderer().render(
            world,
            AllPartialModels.SHAFTLESS_COGWHEEL.get(),
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

        blockState = AllBlocks.MECHANICAL_CRAFTER.getDefaultState();
        world.blockState(blockState);
        matrices.push();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);
        matrices.pop();

        MachinePreviewHelper.end(matrices, vertexConsumers);
    }

    public static float getCurrentAngle() {
        return (AnimationTickHolder.getRenderTime() * 4f) % 360;
    }
}
