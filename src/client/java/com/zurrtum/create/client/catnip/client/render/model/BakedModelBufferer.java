package com.zurrtum.create.client.catnip.client.render.model;

import com.zurrtum.create.client.catnip.impl.client.render.model.BakedModelBuffererImpl;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class BakedModelBufferer {
    private BakedModelBufferer() {
    }

    public static void bufferModel(
        BakedModel model,
        BlockPos pos,
        BlockRenderView level,
        BlockState state,
        @Nullable MatrixStack poseStack,
        ShadeSeparatedBufferSource bufferSource
    ) {
        BakedModelBuffererImpl.bufferModel(model, pos, level, state, poseStack, bufferSource);
    }

    public static void bufferModel(
        BakedModel model,
        BlockPos pos,
        BlockRenderView level,
        BlockState state,
        @Nullable MatrixStack poseStack,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        BakedModelBuffererImpl.bufferModel(model, pos, level, state, poseStack, resultConsumer);
    }

    public static void bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockRenderView level,
        @Nullable MatrixStack poseStack,
        boolean renderFluids,
        ShadeSeparatedBufferSource bufferSource
    ) {
        BakedModelBuffererImpl.bufferBlocks(posIterator, level, poseStack, renderFluids, bufferSource);
    }

    public static void bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockRenderView level,
        @Nullable MatrixStack poseStack,
        boolean renderFluids,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        BakedModelBuffererImpl.bufferBlocks(posIterator, level, poseStack, renderFluids, resultConsumer);
    }
}
