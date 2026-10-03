package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ModelBuilderImpl {
    private ModelBuilderImpl() {
    }

    public static SimpleModel buildBakedModelBuilder(BakedModelBuilder builder) {
        BlockState blockState = builder.level.getBlockState(builder.pos);
        return BakedModelBufferer.bufferModel(builder.model, builder.pos, builder.level, blockState, builder.poseStack, builder.materialFunc);
    }

    public static SimpleModel buildBlockModelBuilder(BlockModelBuilder builder) {
        return BakedModelBufferer.bufferBlocks(
            builder.positions.iterator(),
            builder.level,
            builder.poseStack,
            builder.renderFluids,
            builder.materialFunc
        );
    }
}
