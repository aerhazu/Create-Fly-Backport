package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllCTBehaviours;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import com.zurrtum.create.content.decoration.girder.GirderBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

public class ConnectedGirderModel extends CTModel {
    public ConnectedGirderModel(BakedModel model) {
        super(model, AllCTBehaviours.METAL_GIRDER);
    }

    @Override
    protected void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, SimpleQuadBakedModel.Builder builder) {
        super.addPartsWithInfo(world, pos, state, random, builder);
        for (Direction direction : Iterate.horizontalDirections) {
            if (GirderBlock.isConnected(world, pos, state, direction)) {
                copyPlainQuads(AllPartialModels.METAL_GIRDER_BRACKETS.get(direction).get(), state, random, builder);
            }
        }
    }
}
