/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model;

import com.google.gson.*;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.zurrtum.create.client.model.obj.ObjGeometryHolder;
import com.zurrtum.create.client.model.obj.ObjLoader;
import com.zurrtum.create.client.model.obj.ObjModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.Transformation;

import java.lang.reflect.Type;

public class UnbakedModelParser {
    public static Gson wrap(Gson gson) {
        return new GsonBuilder().registerTypeAdapterFactory(new Deserializer(gson))
            .registerTypeAdapter(Transformation.class, new TransformationHelper.Deserializer()).create();
    }

    /**
     * Always parses a real {@link JsonUnbakedModel} via the delegate (vanilla) Gson, so that models using
     * {@code "loader": "neoforge:obj"} remain usable everywhere vanilla expects a concrete {@code JsonUnbakedModel}
     * (most importantly, as the resolved target of another model's {@code "parent"} reference, which is how nearly
     * every item/block icon in this mod reaches its OBJ geometry). The parsed OBJ geometry, if any, is attached to
     * that same instance via {@link ObjGeometryHolder} rather than represented by a separate class.
     */
    public static class Deserializer implements JsonDeserializer<JsonUnbakedModel>, TypeAdapterFactory {
        private final Gson gson;
        private TypeAdapter<?> cached;

        public Deserializer(Gson gson) {
            this.gson = gson;
        }

        @Override
        public JsonUnbakedModel deserialize(
            JsonElement jsonElement,
            Type type,
            JsonDeserializationContext jsonDeserializationContext
        ) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonUnbakedModel model = gson.fromJson(new JsonTreeReader(jsonObject), JsonUnbakedModel.class);
            JsonElement element = jsonObject.get("loader");
            if (element != null && element.isJsonPrimitive() && element.getAsString().equals("neoforge:obj")) {
                ObjModel objModel = ObjLoader.INSTANCE.read(jsonObject, jsonDeserializationContext);
                ((ObjGeometryHolder) model).create$setObjGeometry(objModel.getGeometry());
            }
            return model;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(Gson proxy, TypeToken<T> type) {
            Class<?> raw = type.getRawType();
            if (raw == UnbakedModel.class || raw == JsonUnbakedModel.class) {
                if (cached != null) {
                    return (TypeAdapter<T>) cached;
                }
                TreeTypeAdapter<T> adapter = new TreeTypeAdapter<>(null, (JsonDeserializer<T>) this, proxy, type, this);
                cached = adapter;
                return adapter;
            }
            return gson.getAdapter(type);
        }
    }
}
