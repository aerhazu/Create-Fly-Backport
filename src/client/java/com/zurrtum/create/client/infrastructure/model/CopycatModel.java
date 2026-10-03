package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.GrassColors;

public abstract class CopycatModel extends PositionAwareBakedModel {
    public CopycatModel(BakedModel model) {
        super(model);
    }

    public static RenderLayer getLayer(BlockRenderView world, BlockPos pos) {
        return RenderLayers.getBlockLayer(CopycatBlock.getMaterial(world, pos));
    }

    public static int getColor(BlockState state, BlockRenderView world, BlockPos pos, int i) {
        if (world == null || pos == null) {
            return GrassColors.getDefaultColor();
        }
        return MinecraftClient.getInstance().getBlockColors().getColor(CopycatBlock.getMaterial(world, pos), world, pos, i);
    }

    @Override
    protected void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, SimpleQuadBakedModel.Builder builder) {
        if (!(state.getBlock() instanceof CopycatBlock block)) {
            return;
        }
        CopycatBlockEntity copycat = (CopycatBlockEntity) world.getBlockEntity(pos);
        BlockState material = copycat == null ? AllBlocks.COPYCAT_BASE.getDefaultState() : copycat.getMaterial();
        addPartsWithInfo(world, pos, state, block, material, random, builder);
    }

    protected abstract void addPartsWithInfo(
        BlockRenderView world,
        BlockPos pos,
        BlockState state,
        CopycatBlock block,
        BlockState material,
        Random random,
        SimpleQuadBakedModel.Builder builder
    );

    protected static BakedModel getModelOf(BlockState material) {
        return MinecraftClient.getInstance().getBlockRenderManager().getModel(material);
    }

    @Override
    public Sprite particleSpriteWithInfo(BlockRenderView world, BlockPos pos, BlockState state) {
        CopycatBlockEntity copycat = (CopycatBlockEntity) world.getBlockEntity(pos);
        if (copycat == null) {
            return model.getParticleSprite();
        }
        return getModelOf(copycat.getMaterial()).getParticleSprite();
    }

    protected void addModelParts(
        BlockRenderView world,
        BlockPos pos,
        BlockState material,
        Random random,
        BakedModel model,
        SimpleQuadBakedModel.Builder builder
    ) {
        addPartsOf(world, pos, material, model, random, builder);
    }

    protected SimpleQuadBakedModel.Builder getMaterialParts(BlockRenderView world, BlockPos pos, BlockState material, Random random, BakedModel model) {
        SimpleQuadBakedModel.Builder builder = new SimpleQuadBakedModel.Builder();
        addModelParts(world, pos, material, random, model, builder);
        return builder;
    }

    protected OcclusionData gatherOcclusionData(
        BlockRenderView world,
        BlockPos pos,
        BlockState state,
        BlockState material,
        CopycatBlock copycatBlock
    ) {
        OcclusionData occlusionData = new OcclusionData();
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        for (Direction face : Iterate.directions) {
            if (!copycatBlock.canFaceBeOccluded(state, face))
                continue;
            if (!Block.shouldDrawSide(material, world, pos, face, mutablePos.set(pos, face)))
                occlusionData.occlude(face);
        }
        return occlusionData;
    }

    protected static class OcclusionData {
        private final boolean[] occluded;

        public OcclusionData() {
            occluded = new boolean[6];
        }

        public void occlude(Direction face) {
            occluded[face.getId()] = true;
        }

        public boolean isOccluded(Direction face) {
            return face != null && occluded[face.getId()];
        }
    }
}
