package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.compat.sodium.SodiumCompat;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class FanRenderer {
    private static final Random RANDOM = Random.create();

    public static void render(DrawContext graphics, float x, float y, BlockState target) {
        MatrixStack matrices = MachinePreviewHelper.begin(graphics, x, y, 50, 44, 24);

        MinecraftClient mc = MinecraftClient.getInstance();
        DiffuseLighting.enableGuiDepthLighting();
        matrices.scale(1, 1, -1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
        matrices.translate(-0.92f, -0.75f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumerProvider.Immediate vertexConsumers = MachinePreviewHelper.buffer();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        Random random = mc.world.random;

        matrices.push();
        blockState = Blocks.AIR.getDefaultState();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(getCurrentAngle() * 16));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.getModelRenderer().render(
            world,
            AllPartialModels.ENCASED_FAN_INNER.get(),
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

        matrices.push();
        blockState = AllBlocks.ENCASED_FAN.getDefaultState();
        world.blockState(blockState);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);
        matrices.pop();

        matrices.translate(0, 0, 2);
        blockState = target;
        FluidState fluidState = blockState.getFluidState();
        if (!fluidState.isEmpty()) {
            Fluid fluid = fluidState.getFluid();
            SodiumCompat.markFluidSpriteActive(fluid);
            FluidRenderHelper.renderFluidBox(
                fluid,
                ComponentChanges.EMPTY,
                0,
                0,
                0,
                1,
                1,
                1,
                vertexConsumers,
                matrices,
                LightmapTextureManager.MAX_LIGHT_COORDINATE,
                false,
                true
            );
        } else {
            world.blockState(blockState);
            RANDOM.setSeed(blockState.getRenderingSeed(BlockPos.ORIGIN));
            if (blockState.getBlock() instanceof AbstractFireBlock) {
                buffer = vertexConsumers.getBuffer(RenderLayer.getCutout());
            }
            blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, RANDOM);
        }

        MachinePreviewHelper.end(matrices, vertexConsumers);
    }

    public static float getCurrentAngle() {
        return (AnimationTickHolder.getRenderTime() * 4f) % 360;
    }
}
