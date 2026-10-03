package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class BasinBlazeBurnerRenderer {
    public static void render(DrawContext graphics, float x, float y, HeatLevel heat) {
        MatrixStack matrices = MachinePreviewHelper.begin(graphics, x, y, 30, 30, 23);

        MinecraftClient mc = MinecraftClient.getInstance();
        DiffuseLighting.enableGuiDepthLighting();
        matrices.scale(1, 1, -1);
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
        float offset = -(MathHelper.sin(AnimationTickHolder.getRenderTime() / 16f) + 0.5f) / 16f;

        blockState = AllBlocks.BLAZE_BURNER.getDefaultState();
        world.blockState(blockState);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, random);

        matrices.push();
        blockState = Blocks.AIR.getDefaultState();
        world.blockState(blockState);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        boolean seething = heat == HeatLevel.SEETHING;
        blockRenderManager.getModelRenderer().render(
            world,
            (seething ? AllPartialModels.BLAZE_SUPER : AllPartialModels.BLAZE_ACTIVE).get(),
            blockState,
            BlockPos.ORIGIN,
            matrices,
            buffer,
            false,
            random,
            42L,
            OverlayTexture.DEFAULT_UV
        );
        matrices.translate(0, offset, 0);
        blockRenderManager.getModelRenderer().render(
            world,
            (seething ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2 : AllPartialModels.BLAZE_BURNER_RODS_2).get(),
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

        SpriteShiftEntry spriteShift = seething ? AllSpriteShifts.SUPER_BURNER_FLAME : AllSpriteShifts.BURNER_FLAME;

        float spriteWidth = spriteShift.getTarget().getMaxU() - spriteShift.getTarget().getMinU();

        float spriteHeight = spriteShift.getTarget().getMaxV() - spriteShift.getTarget().getMinV();

        float time = AnimationTickHolder.getRenderTime(mc.world);
        float speed = 1 / 32f + 1 / 64f * heat.ordinal();

        double vScroll = speed * time;
        vScroll = vScroll - Math.floor(vScroll);
        vScroll = vScroll * spriteHeight / 2;

        double uScroll = speed * time / 2;
        uScroll = uScroll - Math.floor(uScroll);
        uScroll = uScroll * spriteWidth / 2;

        CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_FLAME, Blocks.AIR.getDefaultState())
            .shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll).light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
            .renderInto(matrices, vertexConsumers.getBuffer(RenderLayer.getCutoutMipped()));

        MachinePreviewHelper.end(matrices, vertexConsumers);
    }
}
