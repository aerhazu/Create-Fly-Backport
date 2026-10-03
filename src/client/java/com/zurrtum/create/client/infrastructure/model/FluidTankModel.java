package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllCTBehaviours;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

public class FluidTankModel extends CTModel {
    public FluidTankModel(BakedModel model, ConnectedTextureBehaviour behaviour) {
        super(model, behaviour);
    }

    public static FluidTankModel standard(BakedModel model) {
        return new FluidTankModel(model, AllCTBehaviours.FLUID_TANK);
    }

    public static FluidTankModel creative(BakedModel model) {
        return new FluidTankModel(model, AllCTBehaviours.CREATIVE_FLUID_TANK);
    }

    @Override
    protected void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, SimpleQuadBakedModel.Builder builder) {
        int[] indices = createCTData(world, pos, state);
        boolean[] culls = createCullData(world, pos);
        for (BakedQuad quad : model.getQuads(state, null, random)) {
            builder.add(replaceQuad(state, random, indices[quad.getFace().getId()], quad));
        }
        for (Direction direction : Iterate.directions) {
            int i = direction.getHorizontal();
            if (i != -1 && culls[i]) {
                continue;
            }
            addQuads(builder, state, direction, random, indices[direction.getId()]);
        }
    }

    protected boolean[] createCullData(BlockRenderView world, BlockPos pos) {
        boolean[] culledFaces = new boolean[4];
        for (Direction face : Iterate.horizontalDirections) {
            culledFaces[face.getHorizontal()] = ConnectivityHandler.isConnected(world, pos, pos.offset(face));
        }
        return culledFaces;
    }
}
