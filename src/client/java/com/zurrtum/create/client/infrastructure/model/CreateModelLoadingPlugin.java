package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllModels;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModelEventHandler;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import com.zurrtum.create.client.model.LayerBakedModel;
import com.zurrtum.create.client.model.LayerUnbakedModel;
import com.zurrtum.create.client.model.RenderLayerBakedModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.random.Random;

import java.util.Map;
import java.util.function.Function;

/**
 * Backport replacement for 1.21.8's async {@code ModelBaker} mixin + {@code BlockStatesLoader} block-model
 * substitution. Uses {@link ModelLoadingPlugin.Context#modifyModelAfterBake()} to both populate
 * {@link PartialModel}s (models not tied to any block/item, loaded via {@link ModelLoadingPlugin.Context#addModels})
 * and to wrap the plain baked model of any block registered in {@link AllModels#ALL} with its
 * {@link PositionAwareBakedModel}.
 */
public class CreateModelLoadingPlugin implements ModelLoadingPlugin {
    public static void register() {
        ModelLoadingPlugin.register(new CreateModelLoadingPlugin());
    }

    @Override
    public void onInitializeModelLoader(Context context) {
        Map<Identifier, PartialModel> partials = PartialModelEventHandler.getRegisterAdditional();
        context.addModels(partials.keySet());

        context.modifyModelAfterBake().register((model, ctx) -> {
            PartialModel partial = partials.get(ctx.resourceId());
            if (partial != null) {
                // A model that doubles as a blockstate model (create:block/shaft, block/cogwheel, ...) is baked
                // once per variant, all reporting the same resourceId and a null topLevelId, each carrying that
                // variant's rotation. Taking whichever finished last handed the visuals a pre-rotated mesh,
                // which RotatingInstance#rotateToFace then rotated a second time. Only the unrotated bake may
                // define the PartialModel; the null check is a fallback for partials that have no unrotated
                // variant at all, so they still get geometry rather than nothing.
                var settings = ctx.settings();
                boolean unrotated = settings == null || AffineTransformation.identity().equals(settings.getRotation());
                if (!unrotated && partial.get() != null) {
                    return model;
                }

                // Plain JSON models (the vast majority of PartialModels) bake into vanilla's ordinary
                // BakedModel, not SimpleQuadBakedModel, so their quads need to be copied over manually.
                SimpleQuadBakedModel simple = model instanceof SimpleQuadBakedModel s ? s : create$toSimpleQuadBakedModel(model);

                // Carry the model's declared render_type across. The RenderLayerBakedModel wrapping below
                // only runs for blocks and items, so without this a cutout PartialModel (encased fan
                // propeller, ...) reaches BakedModelBufferer with no layer and gets buffered as solid,
                // painting its transparent texels black instead of discarding them.
                RenderLayer declared = LayerBakedModel.getBlockRenderLayer(model, () -> null);
                if (declared == null && ctx.sourceModel() instanceof LayerUnbakedModel layered) {
                    declared = layered.create$getBlockRenderLayer();
                }
                if (declared != null) {
                    simple.create$setBlockRenderLayer(declared);
                }

                PartialModelEventHandler.onBakingCompleted(partial, simple);
                return model;
            }

            BakedModel result = model;
            if (!(result instanceof LayerBakedModel) && ctx.sourceModel() instanceof LayerUnbakedModel layered) {
                RenderLayer layer = layered.create$getBlockRenderLayer();
                if (layer != null) {
                    result = new RenderLayerBakedModel(result, layer);
                }
            }

            ModelIdentifier topLevelId = ctx.topLevelId();
            if (topLevelId == null) {
                return result;
            }

            if (ModelIdentifier.INVENTORY_VARIANT.equals(topLevelId.variant())) {
                if (Registries.ITEM.getId(AllItems.LINKED_CONTROLLER).equals(topLevelId.id())) {
                    return new LinkedControllerModel(result);
                }
                return result;
            }

            Block block = Registries.BLOCK.get(topLevelId.id());
            Function<BakedModel, BakedModel> factory = AllModels.ALL.get(block);
            if (factory == null) {
                return result;
            }
            return factory.apply(result);
        });
    }

    private static SimpleQuadBakedModel create$toSimpleQuadBakedModel(BakedModel model) {
        SimpleQuadBakedModel.Builder builder = new SimpleQuadBakedModel.Builder();
        PositionAwareBakedModel.copyPlainQuads(model, null, Random.create(), builder);
        return new SimpleQuadBakedModel(builder, model.useAmbientOcclusion(), model.isSideLit(), model.getParticleSprite(), model.getTransformation());
    }
}
