/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.AffineTransformation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Backport of a handful of NeoForge-added top-level model JSON keys, parsed manually since 1.21.1
 * has no generic {@code ContextParameterMap} extension point for {@code UnbakedModel}s to hang them off of.
 */
public final class NeoForgeModelProperties {
    private NeoForgeModelProperties() {
    }

    /**
     * Root transform. For block models, this can be specified under the {@code transform} JSON key.
     */
    @Nullable
    public static AffineTransformation deserializeRootTransform(JsonObject jsonObject, JsonDeserializationContext context) {
        if (jsonObject.has("transform")) {
            JsonElement transform = jsonObject.get("transform");
            return context.deserialize(transform, AffineTransformation.class);
        }
        return null;
    }

    /**
     * Render type to use. For block models, this can be specified under the {@code render_type} JSON key.
     */
    @Nullable
    public static RenderLayer deserializeRenderType(JsonObject jsonObject) {
        if (jsonObject.has("render_type")) {
            return NamedBlockRenderLayer.get(JsonHelper.getString(jsonObject, "render_type"));
        }
        return null;
    }

    /**
     * Part visibilities. For models with named parts (i.e. OBJ), this can be specified under the {@code visibility} JSON key
     */
    public static Map<String, Boolean> deserializePartVisibility(JsonObject jsonObject) {
        Map<String, Boolean> partVisibility = new HashMap<>();
        if (jsonObject.has("visibility")) {
            JsonObject visibility = JsonHelper.getObject(jsonObject, "visibility");
            for (Map.Entry<String, JsonElement> part : visibility.entrySet()) {
                partVisibility.put(part.getKey(), part.getValue().getAsBoolean());
            }
        }
        return Map.copyOf(partVisibility);
    }
}
