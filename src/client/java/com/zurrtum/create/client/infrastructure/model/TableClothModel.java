package com.zurrtum.create.client.infrastructure.model;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.model.BakedQuadHelper;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableClothModel extends PositionAwareBakedModel {
    private static final List<WeakReference<TableClothModel>> MODELS = new ArrayList<>(19);
    private static final Direction[] DIRECTIONS = new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
    private static final int SOUTH_WEST = 0b0011;
    private static final int NORTH_WEST = 0b0110;
    private static final int NORTH_EAST = 0b1100;
    private static final int SOUTH_EAST = 0b1001;

    private final BakedCorner[] corner = new BakedCorner[16];
    private List<BakedQuad> south;
    private List<BakedQuad> west;
    private List<BakedQuad> north;
    private List<BakedQuad> east;

    public TableClothModel(BakedModel model) {
        super(model);
        MODELS.add(new WeakReference<>(this));
    }

    public static void reload() {
        MODELS.removeIf(ref -> {
            TableClothModel model = ref.get();
            if (model != null) {
                model.clearCache();
                return false;
            }
            return true;
        });
    }

    public void clearCache() {
        Arrays.fill(corner, null);
        south = west = north = east = null;
    }

    @Override
    protected void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, SimpleQuadBakedModel.Builder builder) {
        copyPlainQuads(model, state, random, builder);
        int index = 0;
        Mutable mutable = new Mutable();
        for (int i = 0; i < 4; i++) {
            Direction direction = DIRECTIONS[i];
            if (Block.shouldDrawSide(state, world, pos, direction, mutable.set(pos, direction))) {
                index |= (1 << i);
            }
        }
        BakedCorner cache = corner[index];
        if (cache == null) {
            Sprite sprite = model.getParticleSprite();
            cache = corner[index] = new BakedCorner(
                (index & SOUTH_WEST) == SOUTH_WEST ? getSouth(sprite) : List.of(),
                (index & NORTH_WEST) == NORTH_WEST ? getWest(sprite) : List.of(),
                (index & NORTH_EAST) == NORTH_EAST ? getNorth(sprite) : List.of(),
                (index & SOUTH_EAST) == SOUTH_EAST ? getEast(sprite) : List.of()
            );
        }
        for (BakedQuad quad : cache.south)
            builder.add(Direction.SOUTH, quad);
        for (BakedQuad quad : cache.west)
            builder.add(Direction.WEST, quad);
        for (BakedQuad quad : cache.north)
            builder.add(Direction.NORTH, quad);
        for (BakedQuad quad : cache.east)
            builder.add(Direction.EAST, quad);
    }

    private List<BakedQuad> getSouth(Sprite sprite) {
        if (south != null) {
            return south;
        }
        return south = replaceQuads(sprite, AllPartialModels.TABLE_CLOTH_SW);
    }

    private List<BakedQuad> getWest(Sprite sprite) {
        if (west != null) {
            return west;
        }
        return west = replaceQuads(sprite, AllPartialModels.TABLE_CLOTH_NW);
    }

    private List<BakedQuad> getNorth(Sprite sprite) {
        if (north != null) {
            return north;
        }
        return north = replaceQuads(sprite, AllPartialModels.TABLE_CLOTH_NE);
    }

    private List<BakedQuad> getEast(Sprite sprite) {
        if (east != null) {
            return east;
        }
        return east = replaceQuads(sprite, AllPartialModels.TABLE_CLOTH_SE);
    }

    private static List<BakedQuad> replaceQuads(Sprite replace, PartialModel model) {
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
        for (BakedQuad quad : model.get().getAllQuads()) {
            builder.add(replaceQuad(replace, quad));
        }
        return builder.build();
    }

    private static BakedQuad replaceQuad(Sprite replace, BakedQuad quad) {
        Sprite original = quad.getSprite();
        if (original == replace) {
            return quad;
        }
        BakedQuad newQuad = BakedQuadHelper.clone(quad);
        int[] vertexData = newQuad.getVertexData();
        for (int vertex = 0; vertex < 4; vertex++) {
            BakedQuadHelper.setU(
                vertexData,
                vertex,
                replace.getFrameU(SpriteShiftEntry.getUnInterpolatedU(original, BakedQuadHelper.getU(vertexData, vertex)))
            );
            BakedQuadHelper.setV(
                vertexData,
                vertex,
                replace.getFrameV(SpriteShiftEntry.getUnInterpolatedV(original, BakedQuadHelper.getV(vertexData, vertex)))
            );
        }
        return newQuad;
    }

    private record BakedCorner(List<BakedQuad> south, List<BakedQuad> west, List<BakedQuad> north, List<BakedQuad> east) {
    }
}
