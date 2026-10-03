package com.zurrtum.create.client.catnip.impl.client.render.model;

import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedBufferSource;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedResultConsumer;
import com.zurrtum.create.client.catnip.impl.client.render.TransformingVertexConsumer;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import com.zurrtum.create.client.infrastructure.model.PositionAwareBakedModel;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

// Modified from https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/forge/src/lib/java/dev/engine_room/flywheel/lib/model/baked/BakedModelBufferer.java
public final class BakedModelBuffererImpl {
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    private BakedModelBuffererImpl() {
    }

    public static void bufferModel(
        BakedModel model,
        BlockPos pos,
        BlockRenderView level,
        BlockState state,
        @Nullable MatrixStack poseStack,
        ShadeSeparatedBufferSource bufferSource
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        Random random = objects.random;
        random.setSeed(state.getRenderingSeed(pos));
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }

        SimpleQuadBakedModel.Builder builder = new SimpleQuadBakedModel.Builder();
        PositionAwareBakedModel.addPartsOf(level, pos, state, model, random, builder);

        BlockModelRenderer blockRenderer = MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer();
        RenderLayer layer = RenderLayers.getBlockLayer(state);
        UniversalMeshEmitter universalEmitter = objects.universalEmitter;
        SimpleQuadBakedModel flatModel = builder.build(model.useAmbientOcclusion(), model.isSideLit(), model.getParticleSprite());
        long seed = state.getRenderingSeed(pos);
        universalEmitter.prepare(bufferSource, layer);
        poseStack.push();
        blockRenderer.render(level, flatModel, state, pos, poseStack, universalEmitter, false, random, seed, OverlayTexture.DEFAULT_UV);
        poseStack.pop();
        universalEmitter.clear();
    }

    public static void bufferModel(
        BakedModel model,
        BlockPos pos,
        BlockRenderView level,
        BlockState state,
        @Nullable MatrixStack poseStack,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        DefaultShadeSeparatedBufferSource bufferSource = objects.defaultBufferSource;
        bufferSource.prepare(resultConsumer);
        bufferModel(model, pos, level, state, poseStack, bufferSource);
        bufferSource.end();
    }

    public static void bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockRenderView level,
        @Nullable MatrixStack poseStack,
        boolean renderFluids,
        ShadeSeparatedBufferSource bufferSource
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        Random random = objects.random;
        UniversalMeshEmitter universalEmitter = objects.universalEmitter;
        TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

        BlockRenderManager renderDispatcher = MinecraftClient.getInstance().getBlockRenderManager();

        BlockModelRenderer blockRenderer = renderDispatcher.getModelRenderer();
        BlockModelRenderer.enableBrightnessCache();

        while (posIterator.hasNext()) {
            BlockPos pos = posIterator.next();
            BlockState state = level.getBlockState(pos);

            if (renderFluids) {
                FluidState fluidState = state.getFluidState();

                if (!fluidState.isEmpty()) {
                    RenderLayer renderType = RenderLayers.getFluidLayer(fluidState);

                    transformingWrapper.prepare(bufferSource.getBuffer(renderType, true), poseStack);

                    poseStack.push();
                    poseStack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));
                    renderDispatcher.renderFluid(pos, level, transformingWrapper, state, fluidState);
                    poseStack.pop();
                }
            }

            if (state.getRenderType() == BlockRenderType.MODEL) {
                long seed = state.getRenderingSeed(pos);
                BakedModel model = renderDispatcher.getModel(state);
                random.setSeed(seed);
                RenderLayer renderType = RenderLayers.getBlockLayer(state);
                universalEmitter.prepare(bufferSource, renderType);
                poseStack.push();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                SimpleQuadBakedModel.Builder builder = new SimpleQuadBakedModel.Builder();
                PositionAwareBakedModel.addPartsOf(level, pos, state, model, random, builder);
                SimpleQuadBakedModel flatModel = builder.build(model.useAmbientOcclusion(), model.isSideLit(), model.getParticleSprite());
                blockRenderer.render(level, flatModel, state, pos, poseStack, universalEmitter, true, random, seed, OverlayTexture.DEFAULT_UV);
                poseStack.pop();
            }
        }

        BlockModelRenderer.disableBrightnessCache();
        transformingWrapper.clear();
        universalEmitter.clear();
    }

    public static void bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockRenderView level,
        @Nullable MatrixStack poseStack,
        boolean renderFluids,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        DefaultShadeSeparatedBufferSource bufferSource = objects.defaultBufferSource;
        bufferSource.prepare(resultConsumer);
        bufferBlocks(posIterator, level, poseStack, renderFluids, bufferSource);
        bufferSource.end();
    }

    private static class ThreadLocalObjects {
        public final MatrixStack identityPoseStack = new MatrixStack();
        public final Random random = Random.createLocal();

        public final DefaultShadeSeparatedBufferSource defaultBufferSource = new DefaultShadeSeparatedBufferSource();
        public final UniversalMeshEmitter universalEmitter = new UniversalMeshEmitter();
        public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
    }
}
