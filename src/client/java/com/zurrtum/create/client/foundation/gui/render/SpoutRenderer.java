package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class SpoutRenderer {
    public static void render(DrawContext graphics, float x, float y, Fluid fluid, ComponentChanges components) {
        render(graphics, x, y, fluid, components, 0);
    }

    public static void render(DrawContext graphics, float x, float y, Fluid fluid, ComponentChanges components, int offset) {
        MatrixStack matrices = MachinePreviewHelper.begin(graphics, x, y, 26, 65, 20);

        MinecraftClient mc = MinecraftClient.getInstance();
        DiffuseLighting.enableGuiDepthLighting();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumerProvider.Immediate vertexConsumers = MachinePreviewHelper.buffer();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        Random random = mc.world.random;
        float time = AnimationTickHolder.getRenderTime();

        blockState = AllBlocks.SPOUT.getDefaultState();
        world.blockState(blockState);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);

        float cycle = (time - offset * 8) % 30;
        float squeeze = cycle < 20 ? -MathHelper.sin((float) (cycle / 20f * Math.PI)) : 0;
        float move = -3 * squeeze / 32f;

        blockState = Blocks.AIR.getDefaultState();
        world.blockState(blockState);
        blockRenderManager.getModelRenderer()
            .render(world, AllPartialModels.SPOUT_TOP.get(), blockState, BlockPos.ORIGIN, matrices, buffer, false, random, 42L, OverlayTexture.DEFAULT_UV);
        matrices.push();
        matrices.translate(0, move, 0);
        blockRenderManager.getModelRenderer()
            .render(world, AllPartialModels.SPOUT_MIDDLE.get(), blockState, BlockPos.ORIGIN, matrices, buffer, false, random, 42L, OverlayTexture.DEFAULT_UV);
        matrices.translate(0, move, 0);
        blockRenderManager.getModelRenderer()
            .render(world, AllPartialModels.SPOUT_BOTTOM.get(), blockState, BlockPos.ORIGIN, matrices, buffer, false, random, 42L, OverlayTexture.DEFAULT_UV);
        matrices.pop();

        matrices.push();
        blockState = AllBlocks.DEPOT.getDefaultState();
        world.blockState(blockState);
        matrices.translate(0.07f, -2, -0.14f);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);
        matrices.pop();

        if (fluid != Fluids.EMPTY) {
            float scale = 20;
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
            float fluidScale = 16;
            matrices.scale(fluidScale, -fluidScale, fluidScale);
            matrices.translate(0, -1.4f, 0);
            float from = 3f / 16f;
            float to = 17f / 16f;
            FluidRenderHelper.renderFluidBox(
                fluid,
                components,
                from,
                from,
                from,
                to,
                to,
                to,
                vertexConsumers,
                matrices,
                LightmapTextureManager.MAX_LIGHT_COORDINATE,
                false,
                true
            );
            matrices.pop();

            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
            matrices.translate(scale / 2f, scale * 1.5f, scale / 2f);
            matrices.scale(fluidScale, -fluidScale, fluidScale);
            matrices.translate(-0.5f, -1f, -0.5f);
            float fluidWidth = 1 / 128f * -squeeze * 16;
            from = -fluidWidth / 2 + 0.5f;
            to = fluidWidth / 2 + 0.5f;
            FluidRenderHelper.renderFluidBox(
                fluid,
                components,
                from,
                0,
                from,
                to,
                2,
                to,
                vertexConsumers,
                matrices,
                LightmapTextureManager.MAX_LIGHT_COORDINATE,
                false,
                true
            );
            matrices.pop();
        }

        MachinePreviewHelper.end(matrices, vertexConsumers);
    }
}
