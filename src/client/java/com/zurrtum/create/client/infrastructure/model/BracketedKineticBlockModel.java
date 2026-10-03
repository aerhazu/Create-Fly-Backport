package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

public class BracketedKineticBlockModel extends PositionAwareBakedModel {
    public BracketedKineticBlockModel(BakedModel model) {
        super(model);
    }

    /**
     * Only the bracket is baked into the chunk mesh. The shaft/cogwheel geometry itself is the rotating
     * part and is drawn by the block entity (via {@code SingleAxisRotatingVisual} or, without Flywheel,
     * {@code BracketedKineticBlockEntityRenderer}) from the very same model. Emitting the base model here
     * as well would leave a second, stationary copy behind whenever the block spins.
     */
    @Override
    protected void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, SimpleQuadBakedModel.Builder builder) {
        BracketedBlockEntityBehaviour attachmentBehaviour = BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);
        if (attachmentBehaviour == null) {
            return;
        }
        BlockState bracket = attachmentBehaviour.getBracket();
        if (bracket == null) {
            return;
        }
        BakedModel bracketModel = MinecraftClient.getInstance().getBlockRenderManager().getModel(bracket);
        addPartsOf(world, pos, state, bracketModel, random, builder);
    }
}
