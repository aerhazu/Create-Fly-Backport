package com.zurrtum.create.client.model;

import com.google.gson.JsonObject;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public interface LayerUnbakedModel {
    static JsonUnbakedModel setBlockRenderLayer(JsonUnbakedModel model, JsonObject jsonObject) {
        if (jsonObject.has("render_type")) {
            RenderLayer layer = NamedBlockRenderLayer.get(JsonHelper.getString(jsonObject, "render_type"));
            if (layer != null) {
                ((LayerUnbakedModel) (Object) model).create$setBlockRenderLayer(layer);
            }
        }
        return model;
    }

    @Nullable
    default RenderLayer create$getBlockRenderLayer() {
        return null;
    }

    default void create$setBlockRenderLayer(RenderLayer layer) {
    }
}
