package com.zurrtum.create.client.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.List;

/**
 * Delegates every {@link BakedModel} method to the wrapped model, but additionally carries a custom
 * render layer (from a JSON model's {@code render_type} key), which vanilla's baked model types have
 * no field for.
 */
public class RenderLayerBakedModel implements BakedModel, LayerBakedModel {
    private final BakedModel model;
    private final RenderLayer layer;

    public RenderLayerBakedModel(BakedModel model, RenderLayer layer) {
        this.model = model;
        this.layer = layer;
    }

    @Override
    public RenderLayer create$getBlockRenderLayer() {
        return layer;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, Random random) {
        return model.getQuads(state, face, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return model.useAmbientOcclusion();
    }

    @Override
    public boolean hasDepth() {
        return model.hasDepth();
    }

    @Override
    public boolean isSideLit() {
        return model.isSideLit();
    }

    @Override
    public boolean isBuiltin() {
        return model.isBuiltin();
    }

    @Override
    public Sprite getParticleSprite() {
        return model.getParticleSprite();
    }

    @Override
    public ModelTransformation getTransformation() {
        return model.getTransformation();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return model.getOverrides();
    }
}
