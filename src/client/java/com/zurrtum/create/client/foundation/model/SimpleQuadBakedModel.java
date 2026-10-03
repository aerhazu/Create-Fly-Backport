package com.zurrtum.create.client.foundation.model;

import com.zurrtum.create.client.model.LayerBakedModel;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Backport replacement for 1.21.8's {@code GeometryBakedModel}/{@code BakedGeometry}: a flat, precomputed
 * set of quads (unculled + one list per cull face) wrapped as a {@link BakedModel}.
 */
public class SimpleQuadBakedModel implements BakedModel, LayerBakedModel {
    private final List<BakedQuad> unculledQuads;
    private final Map<Direction, List<BakedQuad>> facedQuads;
    private final boolean ambientOcclusion;
    private final boolean sideLit;
    private final Sprite particleSprite;
    private final ModelTransformation transformation;
    /**
     * A JSON model's {@code render_type}, which vanilla's baked models have nowhere to store. Flattening
     * a model into quads would otherwise lose it, and BakedModelBufferer would fall back to the
     * block layer of whatever (usually empty) state it is buffering against - solid, which shades a
     * cutout model's transparent texels black instead of discarding them.
     */
    @Nullable
    private RenderLayer create$blockRenderLayer;

    @Override
    @Nullable
    public RenderLayer create$getBlockRenderLayer() {
        return create$blockRenderLayer;
    }

    public void create$setBlockRenderLayer(@Nullable RenderLayer layer) {
        this.create$blockRenderLayer = layer;
    }

    public SimpleQuadBakedModel(
        List<BakedQuad> unculledQuads,
        Map<Direction, List<BakedQuad>> facedQuads,
        boolean ambientOcclusion,
        boolean sideLit,
        Sprite particleSprite,
        ModelTransformation transformation
    ) {
        this.unculledQuads = unculledQuads;
        this.facedQuads = facedQuads;
        this.ambientOcclusion = ambientOcclusion;
        this.sideLit = sideLit;
        this.particleSprite = particleSprite;
        this.transformation = transformation;
    }

    public SimpleQuadBakedModel(Builder builder, boolean ambientOcclusion, Sprite particleSprite) {
        this(builder, ambientOcclusion, true, particleSprite);
    }

    public SimpleQuadBakedModel(Builder builder, boolean ambientOcclusion, boolean sideLit, Sprite particleSprite) {
        this(builder, ambientOcclusion, sideLit, particleSprite, ModelTransformation.NONE);
    }

    public SimpleQuadBakedModel(
        Builder builder,
        boolean ambientOcclusion,
        boolean sideLit,
        Sprite particleSprite,
        ModelTransformation transformation
    ) {
        this(builder.unculledQuads, builder.facedQuads, ambientOcclusion, sideLit, particleSprite, transformation);
    }

    @Override
    public List<BakedQuad> getQuads(net.minecraft.block.BlockState state, Direction face, Random random) {
        if (face == null)
            return unculledQuads;
        return facedQuads.getOrDefault(face, Collections.emptyList());
    }

    @Override
    public boolean useAmbientOcclusion() {
        return ambientOcclusion;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return sideLit;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return particleSprite;
    }

    @Override
    public ModelTransformation getTransformation() {
        return transformation;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    public List<BakedQuad> getUnculledQuads() {
        return unculledQuads;
    }

    public Map<Direction, List<BakedQuad>> getFacedQuads() {
        return facedQuads;
    }

    public List<BakedQuad> getAllQuads() {
        List<BakedQuad> all = new ArrayList<>(unculledQuads);
        for (List<BakedQuad> quads : facedQuads.values())
            all.addAll(quads);
        return all;
    }

    public static class Builder {
        private final List<BakedQuad> unculledQuads = new ArrayList<>();
        private final Map<Direction, List<BakedQuad>> facedQuads = new EnumMap<>(Direction.class);

        public Builder() {
            for (Direction direction : Direction.values())
                facedQuads.put(direction, new ArrayList<>());
        }

        public Builder add(BakedQuad quad) {
            unculledQuads.add(quad);
            return this;
        }

        public Builder add(Direction direction, BakedQuad quad) {
            facedQuads.get(direction).add(quad);
            return this;
        }

        public List<BakedQuad> getUnculledQuads() {
            return unculledQuads;
        }

        public List<BakedQuad> getFacedQuads(Direction direction) {
            return facedQuads.get(direction);
        }

        public SimpleQuadBakedModel build(boolean ambientOcclusion, Sprite particleSprite) {
            return new SimpleQuadBakedModel(this, ambientOcclusion, particleSprite);
        }

        public SimpleQuadBakedModel build(boolean ambientOcclusion, boolean sideLit, Sprite particleSprite) {
            return new SimpleQuadBakedModel(this, ambientOcclusion, sideLit, particleSprite);
        }

        public SimpleQuadBakedModel build(boolean ambientOcclusion, boolean sideLit, Sprite particleSprite, ModelTransformation transformation) {
            return new SimpleQuadBakedModel(this, ambientOcclusion, sideLit, particleSprite, transformation);
        }
    }
}
