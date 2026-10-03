package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTType;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour.CTContext;
import com.zurrtum.create.client.foundation.model.BakedQuadHelper;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.function.Function;

public class CTModel extends PositionAwareBakedModel {
    private final ConnectedTextureBehaviour behaviour;

    public CTModel(BakedModel model, ConnectedTextureBehaviour behaviour) {
        super(model);
        this.behaviour = behaviour;
    }

    public static Function<BakedModel, BakedModel> of(ConnectedTextureBehaviour behaviour) {
        return model -> new CTModel(model, behaviour);
    }

    @Override
    protected void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, SimpleQuadBakedModel.Builder builder) {
        int[] indices = createCTData(world, pos, state);
        for (BakedQuad quad : model.getQuads(state, null, random)) {
            builder.add(replaceQuad(state, random, indices[quad.getFace().getId()], quad));
        }
        for (Direction direction : Iterate.directions) {
            addQuads(builder, state, direction, random, indices[direction.getId()]);
        }
    }

    protected void addQuads(SimpleQuadBakedModel.Builder builder, BlockState state, Direction direction, Random random, int index) {
        for (BakedQuad quad : model.getQuads(state, direction, random)) {
            builder.add(direction, replaceQuad(state, random, index, quad));
        }
    }

    protected BakedQuad replaceQuad(BlockState state, Random random, int index, BakedQuad quad) {
        if (index == -1) {
            return quad;
        }
        CTSpriteShiftEntry spriteShift = behaviour.getShift(state, random, quad.getFace(), quad.getSprite());
        if (spriteShift == null || quad.getSprite() != spriteShift.getOriginal()) {
            return quad;
        }
        BakedQuad newQuad = BakedQuadHelper.clone(quad);
        int[] vertexData = newQuad.getVertexData();
        for (int vertex = 0; vertex < 4; vertex++) {
            float u = BakedQuadHelper.getU(vertexData, vertex);
            float v = BakedQuadHelper.getV(vertexData, vertex);
            BakedQuadHelper.setU(vertexData, vertex, spriteShift.getTargetU(u, index));
            BakedQuadHelper.setV(vertexData, vertex, spriteShift.getTargetV(v, index));
        }
        return newQuad;
    }

    protected int[] createCTData(BlockRenderView world, BlockPos pos, BlockState state) {
        int[] indices = new int[6];
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        for (Direction face : Iterate.directions) {
            BlockState actualState = world.getBlockState(pos);
            if (!behaviour.buildContextForOccludedDirections() && !Block.shouldDrawSide(
                state,
                world,
                pos,
                face,
                mutablePos.set(pos, face)
            ) && !(actualState.getBlock() instanceof CopycatBlock ufb && !ufb.canFaceBeOccluded(actualState, face))) {
                indices[face.getId()] = -1;
                continue;
            }
            CTType dataType = behaviour.getDataType(world, pos, state, face);
            if (dataType == null) {
                indices[face.getId()] = -1;
                continue;
            }
            CTContext context = behaviour.buildContext(world, pos, state, face, dataType.getContextRequirement());
            indices[face.getId()] = dataType.getTextureIndex(context);
        }
        return indices;
    }
}
