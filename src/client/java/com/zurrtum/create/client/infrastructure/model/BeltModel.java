package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.foundation.model.BakedQuadHelper;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

public class BeltModel extends PositionAwareBakedModel {
    public BeltModel(BakedModel model) {
        super(model);
    }

    private static final SpriteShiftEntry SPRITE_SHIFT = AllSpriteShifts.ANDESIDE_BELT_CASING;

    @Override
    public Sprite particleSpriteWithInfo(BlockRenderView world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof BeltBlockEntity blockEntity && blockEntity.casing == CasingType.ANDESITE) {
            return AllSpriteShifts.ANDESITE_CASING.getOriginal();
        } else {
            return model.getParticleSprite();
        }
    }

    @Override
    protected void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, SimpleQuadBakedModel.Builder builder) {
        BeltBlockEntity blockentity = (BeltBlockEntity) world.getBlockEntity(pos);
        if (blockentity == null || blockentity.casing == CasingType.NONE) {
            copyPlainQuads(model, state, random, builder);
            return;
        }
        if (blockentity.casing == CasingType.BRASS) {
            copyPlainQuads(model, state, random, builder);
            if (blockentity.covered) {
                boolean alongX = state.get(BeltBlock.HORIZONTAL_FACING).getAxis() == Axis.X;
                copyPlainQuads(alongX ? AllPartialModels.BRASS_BELT_COVER_X.get() : AllPartialModels.BRASS_BELT_COVER_Z.get(), state, random, builder);
            }
            return;
        }
        Sprite original = SPRITE_SHIFT.getOriginal();
        if (blockentity.covered) {
            boolean alongX = state.get(BeltBlock.HORIZONTAL_FACING).getAxis() == Axis.X;
            replaceQuads(
                original,
                alongX ? AllPartialModels.ANDESITE_BELT_COVER_X.get() : AllPartialModels.ANDESITE_BELT_COVER_Z.get(),
                state,
                random,
                builder
            );
        }
        replaceQuads(original, model, state, random, builder);
    }

    private void replaceQuads(Sprite replace, BakedModel part, BlockState state, Random random, SimpleQuadBakedModel.Builder builder) {
        for (BakedQuad quad : part.getQuads(state, null, random)) {
            builder.add(replaceQuad(replace, quad));
        }
        for (Direction direction : Iterate.directions) {
            for (BakedQuad quad : part.getQuads(state, direction, random)) {
                builder.add(direction, replaceQuad(replace, quad));
            }
        }
    }

    private BakedQuad replaceQuad(Sprite replace, BakedQuad quad) {
        Sprite original = quad.getSprite();
        if (original != replace) {
            return quad;
        }
        BakedQuad newQuad = BakedQuadHelper.clone(quad);
        int[] vertexData = newQuad.getVertexData();
        for (int vertex = 0; vertex < 4; vertex++) {
            float u = BakedQuadHelper.getU(vertexData, vertex);
            float v = BakedQuadHelper.getV(vertexData, vertex);
            BakedQuadHelper.setU(vertexData, vertex, SPRITE_SHIFT.getTargetU(u));
            BakedQuadHelper.setV(vertexData, vertex, SPRITE_SHIFT.getTargetV(v));
        }
        return newQuad;
    }
}
