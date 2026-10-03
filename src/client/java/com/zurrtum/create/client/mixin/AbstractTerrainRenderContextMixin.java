package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.foundation.block.SelfEmissiveLightingBlock;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractBlockRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("UnstableApiUsage")
@Mixin(AbstractBlockRenderContext.class)
public class AbstractTerrainRenderContextMixin {
    @Shadow(remap = false)
    @Final
    protected BlockRenderInfo blockInfo;

    @WrapOperation(method = "shadeQuad(Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;ZZZ)V", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/client/indigo/renderer/render/AbstractBlockRenderContext;flatBrightness(Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)I"))
    private int light(
        AbstractBlockRenderContext instance,
        MutableQuadViewImpl quad,
        BlockState blockState,
        BlockPos pos,
        Operation<Integer> original
    ) {
        if (blockState.getBlock() instanceof SelfEmissiveLightingBlock) {
            pos = blockInfo.blockPos;
        }
        return original.call(instance, quad, blockState, pos);
    }
}
