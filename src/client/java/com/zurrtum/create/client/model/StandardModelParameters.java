/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.AffineTransformation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper around all standard top-level model parameters added by vanilla and NeoForge except elements.
 * <p>
 * For use in custom model loaders which want to respect these properties but create the quads from
 * something other than the vanilla elements spec.
 */
public record StandardModelParameters(
    @Nullable Identifier parent, Map<String, String> textures, @Nullable ModelTransformation itemTransforms, @Nullable Boolean ambientOcclusion,
    @Nullable JsonUnbakedModel.GuiLight guiLight, @Nullable AffineTransformation rootTransform, @Nullable RenderLayer layer,
    Map<String, Boolean> partVisibility
) {
    public static StandardModelParameters parse(JsonObject jsonObject, JsonDeserializationContext context) {
        String parentName = JsonHelper.getString(jsonObject, "parent", "");
        Identifier parent = parentName.isEmpty() ? null : Identifier.of(parentName);

        Map<String, String> textures = new HashMap<>();
        if (jsonObject.has("textures")) {
            JsonObject jsonobject = JsonHelper.getObject(jsonObject, "textures");
            for (Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
                textures.put(entry.getKey(), entry.getValue().getAsString());
            }
        }

        ModelTransformation itemTransforms = null;
        if (jsonObject.has("display")) {
            JsonObject jsonobject1 = JsonHelper.getObject(jsonObject, "display");
            itemTransforms = context.deserialize(jsonobject1, ModelTransformation.class);
        }

        Boolean ambientOcclusion = null;
        if (jsonObject.has("ambientocclusion")) {
            ambientOcclusion = JsonHelper.getBoolean(jsonObject, "ambientocclusion");
        }

        JsonUnbakedModel.GuiLight guiLight = null;
        if (jsonObject.has("gui_light")) {
            guiLight = JsonUnbakedModel.GuiLight.byName(JsonHelper.getString(jsonObject, "gui_light"));
        }

        AffineTransformation rootTransform = NeoForgeModelProperties.deserializeRootTransform(jsonObject, context);
        RenderLayer layer = NeoForgeModelProperties.deserializeRenderType(jsonObject);
        Map<String, Boolean> partVisibility = NeoForgeModelProperties.deserializePartVisibility(jsonObject);

        return new StandardModelParameters(
            parent,
            Map.copyOf(textures),
            itemTransforms,
            ambientOcclusion,
            guiLight,
            rootTransform,
            layer,
            partVisibility
        );
    }
}
