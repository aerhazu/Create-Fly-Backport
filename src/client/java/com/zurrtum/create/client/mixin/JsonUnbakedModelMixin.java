package com.zurrtum.create.client.mixin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import com.zurrtum.create.client.model.LayerUnbakedModel;
import com.zurrtum.create.client.model.UnbakedModelParser;
import com.zurrtum.create.client.model.obj.ObjGeometry;
import com.zurrtum.create.client.model.obj.ObjGeometryHolder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(JsonUnbakedModel.class)
public abstract class JsonUnbakedModelMixin implements LayerUnbakedModel, ObjGeometryHolder {
    @Unique
    private RenderLayer blockRenderLayer;

    @Unique
    private ObjGeometry create$objGeometry;

    @Shadow
    public abstract SpriteIdentifier resolveSprite(String reference);

    @Shadow
    public abstract boolean useAmbientOcclusion();

    @Shadow
    public abstract JsonUnbakedModel.GuiLight getGuiLight();

    @Shadow
    public abstract ModelTransformation getTransformations();

    @Shadow
    protected JsonUnbakedModel parent;

    @Override
    @Nullable
    public RenderLayer create$getBlockRenderLayer() {
        return blockRenderLayer;
    }

    @Override
    public void create$setBlockRenderLayer(RenderLayer blockRenderLayer) {
        this.blockRenderLayer = blockRenderLayer;
    }

    @Override
    @Nullable
    public ObjGeometry create$getObjGeometry() {
        return create$objGeometry;
    }

    @Override
    public void create$setObjGeometry(ObjGeometry geometry) {
        this.create$objGeometry = geometry;
    }

    /**
     * Walks up the {@code "parent"} chain to find attached OBJ geometry. Needed because most item/block icons in
     * this mod reference their OBJ-loader model indirectly, through a plain wrapper model that only has a
     * {@code "parent"} pointing at the actual {@code "loader": "neoforge:obj"} file - mirroring how vanilla's own
     * {@code getElements()} inherits its element list from the nearest ancestor that defines one.
     */
    @Unique
    private ObjGeometry create$resolveObjGeometry() {
        if (create$objGeometry != null) {
            return create$objGeometry;
        }
        return parent == null ? null : ((JsonUnbakedModelMixin) (Object) parent).create$resolveObjGeometry();
    }

    @Inject(method = "bake(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Z)Lnet/minecraft/client/render/model/BakedModel;", at = @At("HEAD"), cancellable = true)
    private void create$bakeObjGeometry(
        Baker baker,
        JsonUnbakedModel model,
        Function<SpriteIdentifier, Sprite> textureGetter,
        ModelBakeSettings settings,
        boolean bakeOverrides,
        CallbackInfoReturnable<BakedModel> cir
    ) {
        ObjGeometry geometry = create$resolveObjGeometry();
        if (geometry == null) {
            return;
        }
        SimpleQuadBakedModel.Builder builder = new SimpleQuadBakedModel.Builder();
        geometry.bake(builder, this::resolveSprite, textureGetter, settings);
        Sprite particle = textureGetter.apply(resolveSprite("particle"));
        JsonUnbakedModel.GuiLight guiLight = getGuiLight();
        boolean sideLit = guiLight == null || guiLight.isSide();
        cir.setReturnValue(new SimpleQuadBakedModel(builder, useAmbientOcclusion(), sideLit, particle, getTransformations()));
    }

    @WrapOperation(method = "<clinit>()V", at = @At(value = "INVOKE", target = "Lcom/google/gson/GsonBuilder;create()Lcom/google/gson/Gson;"), remap = false)
    private static Gson wrap(GsonBuilder instance, Operation<Gson> original) {
        return UnbakedModelParser.wrap(original.call(instance));
    }
}
