package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;

public class DrainRenderer {
    public static void render(DrawContext graphics, float x, float y, Fluid fluid, ComponentChanges components) {
        MatrixStack matrices = MachinePreviewHelper.begin(graphics, x, y, 26, 23, 20);

        MinecraftClient mc = MinecraftClient.getInstance();
        DiffuseLighting.enableGuiDepthLighting();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
        matrices.scale(1, -1, 1);
        matrices.translate(-0.5f, 0.2f, -0.5f);

        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumerProvider.Immediate vertexConsumers = MachinePreviewHelper.buffer();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());

        BlockState blockState = AllBlocks.ITEM_DRAIN.getDefaultState();
        world.blockState(blockState);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, mc.world.random);

        float from = 2 / 16f;
        float to = 1f - from;
        FluidRenderHelper.renderFluidBox(
            fluid,
            components,
            from,
            from,
            from,
            to,
            3 / 4f,
            to,
            vertexConsumers,
            matrices,
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
            false,
            true
        );

        MachinePreviewHelper.end(matrices, vertexConsumers);
    }
}
