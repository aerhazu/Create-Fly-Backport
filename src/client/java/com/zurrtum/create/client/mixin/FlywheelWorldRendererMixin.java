package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.impl.FlwImplXplat;
import com.zurrtum.create.client.flywheel.impl.event.RenderContextImpl;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.BlockBreakingInfo;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

@Mixin(value = WorldRenderer.class, priority = 1001) // Higher priority to go after Sodium
public class FlywheelWorldRendererMixin {
    @Shadow
    @Nullable
    private ClientWorld world;

    @Shadow
    @Final
    public BufferBuilderStorage bufferBuilders;

    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions;

    @Unique
    @Nullable
    private RenderContextImpl flywheel$renderContext;

    @Inject(
        method = "render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
        at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/chunk/light/LightingProvider;doLightUpdates()I")
    )
    private void flywheel$beginRender(
        RenderTickCounter tickCounter,
        boolean renderBlockOutline,
        Camera camera,
        GameRenderer gameRenderer,
        LightmapTextureManager lightmapTextureManager,
        Matrix4f positionMatrix,
        Matrix4f projectionMatrix,
        CallbackInfo ci
    ) {
        flywheel$renderContext = RenderContextImpl.create(
            (WorldRenderer) (Object) this,
            world,
            bufferBuilders,
            positionMatrix,
            projectionMatrix,
            camera,
            tickCounter.getTickDelta(false)
        );

        VisualizationManager manager = VisualizationManager.get(world);
        if (manager != null) {
            manager.renderDispatcher().onStartLevelRender(flywheel$renderContext);
        }
    }

    @Inject(
        method = "render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
        at = @At("RETURN")
    )
    private void flywheel$endRender(CallbackInfo ci) {
        flywheel$renderContext = null;
    }

    @Inject(method = "reload()V", at = @At("RETURN"))
    private void flywheel$reload(CallbackInfo ci) {
        if (world != null) {
            FlwImplXplat.INSTANCE.dispatchReloadLevelRendererEvent(world);
        }
    }

    /**
     * Anchored to the {@code noCullingBlockEntities} read that follows the visible-chunk block entity
     * loops, rather than to the first {@code BlockEntityRenderDispatcher#render} call.
     * <p>
     * That call site sits inside a doubly-nested loop, so it fired once per visible block entity —
     * and Sodium's {@code LevelRendererMixin} replaces that iteration entirely, so under Sodium it
     * never fired at all and nothing Flywheel owns was ever drawn. This field read executes exactly
     * once per frame in both cases; Sodium anchors its own block entity hook to it for the same reason.
     * {@code Shift.BEFORE} puts us ahead of the {@code aload_0} so the stack is clean.
     */
    @Inject(
        method = "render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/render/WorldRenderer;noCullingBlockEntities:Ljava/util/Set;",
            opcode = Opcodes.GETFIELD,
            ordinal = 0,
            shift = At.Shift.BEFORE
        )
    )
    private void flywheel$beforeBlockEntities(CallbackInfo ci) {
        if (flywheel$renderContext != null) {
            VisualizationManager manager = VisualizationManager.get(world);
            if (manager != null) {
                manager.renderDispatcher().afterEntities(flywheel$renderContext);
            }
        }
    }

    @Inject(
        method = "render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/block/BlockRenderManager;renderDamage(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V",
            ordinal = 0
        )
    )
    private void flywheel$beforeRenderCrumbling(CallbackInfo ci) {
        if (flywheel$renderContext != null) {
            VisualizationManager manager = VisualizationManager.get(world);
            if (manager != null) {
                manager.renderDispatcher().beforeCrumbling(flywheel$renderContext, blockBreakingProgressions);
            }
        }
    }

    @WrapOperation(
        method = "render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"
        )
    )
    private void flywheel$decideNotToRenderBlockEntity(
        BlockEntityRenderDispatcher instance,
        BlockEntity blockEntity,
        float tickProgress,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        Operation<Void> original
    ) {
        if (VisualizationManager.supportsVisualization(blockEntity.getWorld()) && VisualizationHelper.skipVanillaRender(blockEntity)) {
            return;
        }
        original.call(instance, blockEntity, tickProgress, matrices, vertexConsumers);
    }

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    private void flywheel$decideNotToRenderEntity(
        Entity entity,
        double cameraX,
        double cameraY,
        double cameraZ,
        float tickProgress,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        CallbackInfo ci
    ) {
        if (VisualizationManager.supportsVisualization(entity.getWorld()) && VisualizationHelper.skipVanillaRender(entity)) {
            ci.cancel();
        }
    }
}
