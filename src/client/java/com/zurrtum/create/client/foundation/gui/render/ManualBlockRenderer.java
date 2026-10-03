package com.zurrtum.create.client.foundation.gui.render;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class ManualBlockRenderer {
    private static final Random random = Random.create();

    public static void render(DrawContext graphics, float x, float y, BlockState block) {
        MatrixStack matrices = MachinePreviewHelper.begin(graphics, x, y, 27, 27, 20);

        MinecraftClient mc = MinecraftClient.getInstance();
        DiffuseLighting.enableGuiDepthLighting();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -0.2f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumerProvider.Immediate vertexConsumers = MachinePreviewHelper.buffer();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        world.blockState(block);
        random.setSeed(42L);
        blockRenderManager.renderBlock(block, BlockPos.ORIGIN, world, matrices, buffer, false, random);

        MachinePreviewHelper.end(matrices, vertexConsumers);
    }
}
