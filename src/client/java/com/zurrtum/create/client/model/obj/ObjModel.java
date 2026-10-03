/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model.obj;

import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import com.zurrtum.create.client.model.LayerUnbakedModel;
import com.zurrtum.create.client.model.StandardModelParameters;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * A model loaded from an OBJ file.
 * <p>
 * Supports positions, texture coordinates, normals and colors. The {@link ObjMaterialLibrary material library}
 * has support for numerous features, including support for {@link Identifier} textures (non-standard).
 */
public class ObjModel implements UnbakedModel, LayerUnbakedModel {
    private final StandardModelParameters parameters;
    private final ObjGeometry geometry;
    @Nullable
    private UnbakedModel parent;

    public ObjModel(StandardModelParameters parameters, ObjGeometry geometry) {
        this.parameters = parameters;
        this.geometry = geometry;
    }

    public ObjGeometry getGeometry() {
        return geometry;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return parameters.parent() != null ? List.of(parameters.parent()) : List.of();
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelGetter) {
        parent = parameters.parent() != null ? modelGetter.apply(parameters.parent()) : null;
    }

    @Override
    @Nullable
    public RenderLayer create$getBlockRenderLayer() {
        if (parameters.layer() != null) {
            return parameters.layer();
        }
        if (parent instanceof LayerUnbakedModel layered) {
            return layered.create$getBlockRenderLayer();
        }
        return null;
    }

    boolean resolveAmbientOcclusion() {
        if (parameters.ambientOcclusion() != null) {
            return parameters.ambientOcclusion();
        }
        if (parent instanceof JsonUnbakedModel json) {
            return json.useAmbientOcclusion();
        }
        if (parent instanceof ObjModel obj) {
            return obj.resolveAmbientOcclusion();
        }
        return true;
    }

    boolean resolveSideLit() {
        if (parameters.guiLight() != null) {
            return parameters.guiLight().isSide();
        }
        if (parent instanceof JsonUnbakedModel json) {
            JsonUnbakedModel.GuiLight light = json.getGuiLight();
            return light == null || light.isSide();
        }
        if (parent instanceof ObjModel obj) {
            return obj.resolveSideLit();
        }
        return true;
    }

    ModelTransformation resolveTransformations() {
        if (parameters.itemTransforms() != null) {
            return parameters.itemTransforms();
        }
        if (parent instanceof JsonUnbakedModel json) {
            return json.getTransformations();
        }
        if (parent instanceof ObjModel obj) {
            return obj.resolveTransformations();
        }
        return ModelTransformation.NONE;
    }

    SpriteIdentifier resolveSprite(String reference) {
        if (reference.startsWith("#")) {
            String variable = reference.substring(1);
            String local = parameters.textures().get(variable);
            if (local != null) {
                return resolveSprite(local);
            }
            if (parent instanceof JsonUnbakedModel json) {
                return json.resolveSprite(variable);
            }
            if (parent instanceof ObjModel obj) {
                return obj.resolveSprite(reference);
            }
            return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, MissingSprite.getMissingSpriteId());
        }
        return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.of(reference));
    }

    @Override
    public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings settings) {
        SimpleQuadBakedModel.Builder builder = new SimpleQuadBakedModel.Builder();
        geometry.bake(builder, this::resolveSprite, textureGetter, settings);
        Sprite particle = textureGetter.apply(resolveSprite("particle"));
        return new SimpleQuadBakedModel(builder, resolveAmbientOcclusion(), resolveSideLit(), particle, resolveTransformations());
    }
}
