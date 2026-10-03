package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import com.zurrtum.create.client.infrastructure.model.PositionAwareBakedModel;
import com.zurrtum.create.client.model.LayerBakedModel;
import com.zurrtum.create.foundation.block.LightControlBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
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

final class BakedModelBufferer {
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    private BakedModelBufferer() {
    }

    private static boolean isDark(BlockRenderView level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof LightControlBlock block) {
            return block.getLuminance(level, pos) == 0;
        }
        return state.getLuminance() == 0;
    }

    public static SimpleModel bufferModel(
        BakedModel model,
        BlockPos pos,
        BlockRenderView level,
        BlockState state,
        @Nullable MatrixStack poseStack,
        BlockMaterialFunction blockMaterialFunction
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        Random random = objects.random;
        random.setSeed(state.getRenderingSeed(pos));

        SimpleQuadBakedModel.Builder builder = new SimpleQuadBakedModel.Builder();
        PositionAwareBakedModel.addPartsOf(level, pos, state, model, random, builder);

        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        MeshEmitterManager<VanillinMeshEmitter> emitters = objects.emitters;
        emitters.prepare(blockMaterialFunction);
        BlockModelRenderer blockRenderer = MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer();
        RenderLayer renderType = LayerBakedModel.getBlockRenderLayer(model, () -> RenderLayers.getBlockLayer(state));
        VanillinMeshEmitter emitter = emitters.getEmitter(renderType);
        boolean ambientOcclusion = model.useAmbientOcclusion();
        emitter.prepareForModelLayer(MinecraftClient.isAmbientOcclusionEnabled() && ambientOcclusion && isDark(level, pos, state));
        SimpleQuadBakedModel flatModel = builder.build(ambientOcclusion, model.isSideLit(), model.getParticleSprite());
        long seed = state.getRenderingSeed(pos);
        poseStack.push();
        blockRenderer.render(level, flatModel, state, pos, poseStack, emitter, false, random, seed, OverlayTexture.DEFAULT_UV);
        poseStack.pop();
        return emitters.end();
    }

    public static SimpleModel bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockRenderView level,
        @Nullable MatrixStack poseStack,
        boolean renderFluids,
        BlockMaterialFunction blockMaterialFunction
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        Random random = objects.random;
        MeshEmitterManager<VanillinMeshEmitter> emitters = objects.emitters;
        TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

        emitters.prepare(blockMaterialFunction);

        BlockRenderManager renderDispatcher = MinecraftClient.getInstance().getBlockRenderManager();

        BlockModelRenderer blockRenderer = renderDispatcher.getModelRenderer();
        BlockModelRenderer.enableBrightnessCache();

        boolean aoEnabled = MinecraftClient.isAmbientOcclusionEnabled();

        while (posIterator.hasNext()) {
            BlockPos pos = posIterator.next();
            BlockState state = level.getBlockState(pos);

            if (renderFluids) {
                FluidState fluidState = state.getFluidState();
                if (!fluidState.isEmpty()) {
                    RenderLayer renderType = RenderLayers.getFluidLayer(fluidState);

                    BufferBuilder bufferBuilder = emitters.getBuffer(renderType, true, false);

                    if (bufferBuilder != null) {
                        transformingWrapper.prepare(bufferBuilder, poseStack);

                        poseStack.push();
                        poseStack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));
                        renderDispatcher.renderFluid(pos, level, transformingWrapper, state, fluidState);
                        poseStack.pop();
                    }
                }
            }

            if (state.getRenderType() == BlockRenderType.MODEL) {
                BakedModel model = renderDispatcher.getModel(state);
                long seed = state.getRenderingSeed(pos);
                random.setSeed(seed);
                SimpleQuadBakedModel.Builder builder = new SimpleQuadBakedModel.Builder();
                PositionAwareBakedModel.addPartsOf(level, pos, state, model, random, builder);
                RenderLayer renderType = RenderLayers.getBlockLayer(state);
                VanillinMeshEmitter emitter = emitters.getEmitter(renderType);
                boolean ambientOcclusion = model.useAmbientOcclusion();
                emitter.prepareForModelLayer(aoEnabled && ambientOcclusion);
                SimpleQuadBakedModel flatModel = builder.build(ambientOcclusion, model.isSideLit(), model.getParticleSprite());
                poseStack.push();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                blockRenderer.render(level, flatModel, state, pos, poseStack, emitter, true, random, seed, OverlayTexture.DEFAULT_UV);
                poseStack.pop();
            }
        }

        BlockModelRenderer.disableBrightnessCache();
        transformingWrapper.clear();
        return emitters.end();
    }

    private static class ThreadLocalObjects {
        public final MatrixStack identityPoseStack = new MatrixStack();
        public final Random random = Random.createLocal();

        public final MeshEmitterManager<VanillinMeshEmitter> emitters = new MeshEmitterManager<>(VanillinMeshEmitter::new);
        public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
    }
}
