package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.foundation.model.BakedQuadHelper;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatStepBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.List;
import java.util.function.Consumer;

public class CopycatStepModel extends CopycatModel {
    protected static final Vec3d VEC_Y_3 = new Vec3d(0, .75, 0);
    protected static final Vec3d VEC_Y_2 = new Vec3d(0, .5, 0);
    protected static final Vec3d VEC_Y_N2 = new Vec3d(0, -.5, 0);
    protected static final Box CUBE_AABB = new Box(BlockPos.ORIGIN);

    public CopycatStepModel(BakedModel model) {
        super(model);
    }

    @Override
    protected void addPartsWithInfo(
        BlockRenderView world,
        BlockPos pos,
        BlockState state,
        CopycatBlock block,
        BlockState material,
        Random random,
        SimpleQuadBakedModel.Builder builder
    ) {
        Direction facing = state.get(CopycatStepBlock.FACING, Direction.SOUTH);
        boolean upperHalf = state.get(CopycatStepBlock.HALF, BlockHalf.BOTTOM) == BlockHalf.TOP;
        Vec3d normal = Vec3d.of(facing.getVector());
        Vec3d normalScaled2 = normal.multiply(.5);
        Vec3d normalScaledN3 = normal.multiply(-.75);
        Box bb = CUBE_AABB.shrink(-normal.x * .75, .75, -normal.z * .75);

        OcclusionData occlusionData = gatherOcclusionData(world, pos, state, material, block);
        BakedModel model = getModelOf(material);
        SimpleQuadBakedModel.Builder original = getMaterialParts(world, pos, material, random, model);
        addCroppedQuads(facing, upperHalf, normalScaled2, normalScaledN3, bb, original.getUnculledQuads(), builder::add);
        for (Direction direction : Iterate.directions) {
            if (occlusionData.isOccluded(direction))
                continue;
            addCroppedQuads(
                facing,
                upperHalf,
                normalScaled2,
                normalScaledN3,
                bb,
                original.getFacedQuads(direction),
                block.shouldFaceAlwaysRender(state, direction) ? builder::add : (BakedQuad quad) -> builder.add(direction, quad)
            );
        }
    }

    protected void addCroppedQuads(
        Direction facing,
        boolean upperHalf,
        Vec3d normalScaled2,
        Vec3d normalScaledN3,
        Box bb,
        List<BakedQuad> quads,
        Consumer<BakedQuad> consumer
    ) {
        int size = quads.size();
        if (size == 0) {
            return;
        }
        for (boolean top : Iterate.trueAndFalse) {
            for (boolean front : Iterate.trueAndFalse) {
                Box bb1 = bb;
                if (front)
                    bb1 = bb1.offset(normalScaledN3);
                if (top)
                    bb1 = bb1.offset(VEC_Y_3);

                Vec3d offset = Vec3d.ZERO;
                if (front)
                    offset = offset.add(normalScaled2);
                if (top != upperHalf)
                    offset = offset.add(upperHalf ? VEC_Y_2 : VEC_Y_N2);

                for (int i = 0; i < size; i++) {
                    BakedQuad quad = quads.get(i);
                    Direction direction = quad.getFace();

                    if (front && direction == facing)
                        continue;
                    if (!front && direction == facing.getOpposite())
                        continue;
                    if (!top && direction == Direction.UP)
                        continue;
                    if (top && direction == Direction.DOWN)
                        continue;

                    consumer.accept(BakedQuadHelper.cloneWithCustomGeometry(
                        quad,
                        BakedModelHelper.cropAndMove(quad.getVertexData(), quad.getSprite(), bb1, offset)
                    ));
                }
            }
        }
    }
}
