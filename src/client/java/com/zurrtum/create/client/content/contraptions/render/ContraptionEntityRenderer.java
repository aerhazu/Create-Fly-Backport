package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.ShadedBlockSbbBuilder;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption.RenderedBlocks;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.client.infrastructure.model.PositionAwareBakedModel;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.atomic.AtomicReference;

public class ContraptionEntityRenderer<C extends AbstractContraptionEntity> extends EntityRenderer<C> {
    public static final SuperByteBufferCache.Compartment<Pair<Contraption, RenderLayer>> CONTRAPTION = new SuperByteBufferCache.Compartment<>();
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    public ContraptionEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    public static SuperByteBuffer getBuffer(
        Contraption contraption,
        ClientContraption clientContraption,
        VirtualRenderWorld renderWorld,
        RenderLayer renderType
    ) {
        return SuperByteBufferCache.getInstance()
            .get(CONTRAPTION, Pair.of(contraption, renderType), () -> buildStructureBuffer(clientContraption, renderWorld, renderType));
    }

    @SuppressWarnings("unchecked")
    public ClientContraption getOrCreateClientContraptionLazy(Contraption contraption) {
        AtomicReference<ClientContraption> clientContraption = (AtomicReference<ClientContraption>) contraption.clientContraption;
        var out = clientContraption.getAcquire();
        if (out == null) {
            // Another thread may hit this block in the same moment.
            // One thread will win and the ContraptionRenderInfo that
            // it generated will become canonical. It's important that
            // we only maintain one RenderInfo instance, specifically
            // for the VirtualRenderWorld inside.
            clientContraption.compareAndExchangeRelease(null, createClientContraption(contraption));

            // Must get again to ensure we have the canonical instance.
            out = clientContraption.getAcquire();
        }
        return out;
    }

    protected ClientContraption createClientContraption(Contraption contraption) {
        return new ClientContraption(contraption);
    }

    @SuppressWarnings("removal")
    private static SuperByteBuffer buildStructureBuffer(ClientContraption clientContraption, VirtualRenderWorld renderWorld, RenderLayer layer) {
        BlockRenderManager dispatcher = MinecraftClient.getInstance().getBlockRenderManager();
        BlockModelRenderer renderer = dispatcher.getModelRenderer();
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

        MatrixStack poseStack = objects.poseStack;
        Random random = objects.random;
        RenderedBlocks blocks = clientContraption.getRenderedBlocks();

        ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
        sbbBuilder.begin();

        BlockModelRenderer.enableBrightnessCache();
        for (BlockPos pos : blocks.positions()) {
            BlockState state = blocks.lookup().apply(pos);
            if (state.getRenderType() == BlockRenderType.MODEL) {
                BakedModel model = dispatcher.getModel(state);
                long randomSeed = state.getRenderingSeed(pos);
                random.setSeed(randomSeed);
                if (RenderLayers.getBlockLayer(state) == layer) {
                    poseStack.push();
                    poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    SimpleQuadBakedModel.Builder builder = new SimpleQuadBakedModel.Builder();
                    PositionAwareBakedModel.addPartsOf(renderWorld, pos, state, model, random, builder);
                    SimpleQuadBakedModel flatModel = builder.build(model.useAmbientOcclusion(), model.isSideLit(), model.getParticleSprite());
                    renderer.render(renderWorld, flatModel, state, pos, poseStack, sbbBuilder, true, random, randomSeed, OverlayTexture.DEFAULT_UV);
                    poseStack.pop();
                }
            }
        }
        BlockModelRenderer.disableBrightnessCache();

        return sbbBuilder.end();
    }

    @Override
    public net.minecraft.util.Identifier getTexture(C entity) {
        return null;
    }

    @Override
    public boolean shouldRender(C entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        if (entity.getContraption() == null)
            return false;
        if (!entity.isAliveOrStale())
            return false;
        if (!entity.isReadyForRender())
            return false;

        return super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ);
    }

    @Override
    public void render(C entity, float entityYaw, float partialTicks, MatrixStack poseStack, VertexConsumerProvider buffers, int overlay) {
        Contraption contraption = entity.getContraption();
        if (contraption == null) {
            return;
        }

        ClientContraption clientContraption = getOrCreateClientContraptionLazy(contraption);
        World world = entity.getWorld();
        VirtualRenderWorld renderWorld = clientContraption.getRenderLevel();
        ContraptionMatrices matrices = clientContraption.getMatrices();
        matrices.setup((matrixStack, pt) -> transform(entity, matrixStack, pt), poseStack, entity, partialTicks);

        if (!VisualizationManager.supportsVisualization(world)) {
            for (RenderLayer renderType : RenderLayer.getBlockLayers()) {
                SuperByteBuffer sbb = getBuffer(contraption, clientContraption, renderWorld, renderType);
                if (!sbb.isEmpty()) {
                    VertexConsumer vc = buffers.getBuffer(getRenderLayer(renderType));
                    sbb.transform(matrices.getModel()).useLevelLight(world, matrices.getWorld()).renderInto(poseStack, vc);
                }
            }
        }

        var adjustRenderedBlockEntities = clientContraption.getAndAdjustShouldRenderBlockEntities();
        clientContraption.scratchErroredBlockEntities.clear();
        BlockEntityRenderHelper.renderBlockEntities(
            clientContraption.renderedBlockEntityView,
            adjustRenderedBlockEntities,
            clientContraption.scratchErroredBlockEntities,
            renderWorld,
            world,
            matrices.getModelViewProjection(),
            matrices.getLight(),
            buffers,
            partialTicks
        );
        clientContraption.shouldRenderBlockEntities.andNot(clientContraption.scratchErroredBlockEntities);
        renderActors(world, renderWorld, contraption, matrices, buffers);

        matrices.clear();
    }

    private RenderLayer getRenderLayer(RenderLayer layer) {
        return layer == RenderLayer.getTranslucent() ? PonderRenderTypes.translucent() : layer;
    }

    public void transform(C entity, MatrixStack matrixStack, float partialTicks) {
    }

    private static void renderActors(
        World level,
        VirtualRenderWorld renderWorld,
        Contraption c,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffer
    ) {
        MatrixStack m = matrices.getModel();

        for (Pair<StructureTemplate.StructureBlockInfo, MovementContext> actor : c.getActors()) {
            MovementContext context = actor.getRight();
            if (context == null)
                continue;
            if (context.world == null)
                context.world = level;
            StructureTemplate.StructureBlockInfo blockInfo = actor.getLeft();

            MovementBehaviour movementBehaviour = MovementBehaviour.REGISTRY.get(blockInfo.state());
            if (movementBehaviour != null) {
                MovementRenderBehaviour render = movementBehaviour.getAttachRender();
                if (render == null || c.isHiddenInPortal(blockInfo.pos()))
                    continue;
                m.push();
                TransformStack.of(m).translate(blockInfo.pos());
                render.renderInContraption(context, renderWorld, matrices, buffer);
                m.pop();
            }
        }
    }

    @SuppressWarnings("removal")
    private static class ThreadLocalObjects {
        public final MatrixStack poseStack = new MatrixStack();
        public final Random random = Random.createLocal();
        public final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
    }
}
