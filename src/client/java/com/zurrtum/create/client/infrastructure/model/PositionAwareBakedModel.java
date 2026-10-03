package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.List;
import java.util.function.Supplier;

/**
 * Backport replacement for 1.21.8's {@code WrapperBlockStateModel}. Wraps the plain vanilla-baked
 * {@link BakedModel} for a block ({@link #model}) and additionally implements {@link FabricBakedModel} so
 * that, when the Fabric Rendering API is active (Indigo/Sodium+Indium), block quad emission gets access to
 * world/position context via {@link #emitBlockQuads} - needed for connected textures, casing overlays, etc.
 * Contexts that only see the plain {@link BakedModel} interface (e.g. item rendering) fall back to the
 * position-unaware {@link #getQuads} implementation.
 */
public abstract class PositionAwareBakedModel implements BakedModel, FabricBakedModel {
    protected final BakedModel model;

    public PositionAwareBakedModel(BakedModel model) {
        this.model = model;
    }

    public BakedModel getWrapped() {
        return model;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView world, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        Random random = randomSupplier.get();
        SimpleQuadBakedModel.Builder builder = new SimpleQuadBakedModel.Builder();
        addPartsWithInfo(world, pos, state, random, builder);
        emit(context, builder);
    }

    /**
     * Resolved lazily: the renderer is not registered yet when this class is loaded. The benign race
     * between chunk-builder threads only ever recomputes the same immutable value.
     */
    private static volatile RenderMaterial standardMaterial;

    private static RenderMaterial standardMaterial() {
        RenderMaterial material = standardMaterial;
        if (material == null) {
            material = RendererAccess.INSTANCE.getRenderer().materialById(RenderMaterial.MATERIAL_STANDARD);
            standardMaterial = material;
        }
        return material;
    }

    public static void emit(RenderContext context, SimpleQuadBakedModel.Builder builder) {
        QuadEmitter emitter = context.getEmitter();
        // Must be an actual material, not null. Indigo's MutableQuadViewImpl#material substitutes
        // MATERIAL_STANDARD for null, but Sodium's own FRAPI implementation dereferences it directly and
        // throws while building chunk meshes. Passing the standard material explicitly is identical to
        // what Indigo was already doing.
        RenderMaterial material = standardMaterial();
        for (BakedQuad quad : builder.getUnculledQuads()) {
            emitter.fromVanilla(quad, material, null);
            emitter.emit();
        }
        for (Direction direction : Direction.values()) {
            for (BakedQuad quad : builder.getFacedQuads(direction)) {
                emitter.fromVanilla(quad, material, direction);
                emitter.emit();
            }
        }
    }

    protected void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, SimpleQuadBakedModel.Builder builder) {
        copyPlainQuads(model, state, random, builder);
    }

    public static void copyPlainQuads(BakedModel model, BlockState state, Random random, SimpleQuadBakedModel.Builder builder) {
        for (BakedQuad quad : model.getQuads(state, null, random))
            builder.add(quad);
        for (Direction direction : Direction.values())
            for (BakedQuad quad : model.getQuads(state, direction, random))
                builder.add(direction, quad);
    }

    /**
     * Recursively unwraps {@code otherModel} if it is itself a {@link PositionAwareBakedModel} (or a
     * Fabric-compat wrapper around one), delegating to its position-aware quad collection; otherwise falls
     * back to plain quad copying. Used by models that need to render another block's model in place (e.g.
     * copycat blocks mimicking a material, or brackets attached to a shaft).
     */
    public static void addPartsOf(
        BlockRenderView world,
        BlockPos pos,
        BlockState state,
        BakedModel otherModel,
        Random random,
        SimpleQuadBakedModel.Builder builder
    ) {
        if (otherModel instanceof PositionAwareBakedModel wrapper) {
            wrapper.addPartsWithInfo(world, pos, state, random, builder);
        } else {
            copyPlainQuads(otherModel, state, random, builder);
        }
    }

    public Sprite particleSpriteWithInfo(BlockRenderView world, BlockPos pos, BlockState state) {
        return getParticleSprite();
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
