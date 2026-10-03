package com.zurrtum.create.client.foundation.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.zurrtum.create.Create.MOD_ID;

public class RenderTypes extends RenderPhase {
    private static RenderLayer.MultiPhaseParameters blockAdditiveParameters(boolean mipmap) {
        return RenderLayer.MultiPhaseParameters.builder().program(new ShaderProgram(GameRenderer::getRenderTypeEntityCutoutProgram))
            .texture(mipmap ? MIPMAP_BLOCK_ATLAS_TEXTURE : BLOCK_ATLAS_TEXTURE).transparency(ADDITIVE_TRANSPARENCY)
            .depthTest(LEQUAL_DEPTH_TEST).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).layering(NO_LAYERING)
            .target(MAIN_TARGET).texturing(DEFAULT_TEXTURING).writeMaskState(ALL_MASK).lineWidth(FULL_LINE_WIDTH).colorLogic(NO_COLOR_LOGIC)
            .build(true);
    }

    private static final RenderLayer ADDITIVE = RenderLayer.of(
        createLayerName("additive"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        256,
        blockAdditiveParameters(false)
    );

    private static final RenderLayer ADDITIVE2 = RenderLayer.of(
        createLayerName("additive2"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        256,
        blockAdditiveParameters(false)
    );

    private static final RenderLayer ITEM_GLOWING_SOLID = RenderLayer.of(
        createLayerName("item_glowing_solid"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        256,
        blockAdditiveParameters(false)
    );

    private static final RenderLayer ITEM_GLOWING_TRANSLUCENT = RenderLayer.of(
        createLayerName("item_glowing_translucent"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        256,
        blockAdditiveParameters(false)
    );

    private static final Function<Identifier, RenderLayer> CHAIN = Util.memoize((location) -> RenderLayer.of(
        "chain_conveyor_chain",
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        256,
        RenderLayer.MultiPhaseParameters.builder().program(new ShaderProgram(GameRenderer::getRenderTypeCutoutMippedProgram))
            .texture(new Texture(location, true, false)).transparency(NO_TRANSPARENCY).depthTest(LEQUAL_DEPTH_TEST).cull(DISABLE_CULLING)
            .lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).layering(NO_LAYERING).target(MAIN_TARGET).texturing(DEFAULT_TEXTURING)
            .writeMaskState(ALL_MASK).lineWidth(FULL_LINE_WIDTH).colorLogic(NO_COLOR_LOGIC).build(false)
    ));

    public static RenderLayer entitySolidBlockMipped() {
        return TexturedRenderLayers.getEntitySolid();
    }

    public static RenderLayer entityCutoutBlockMipped() {
        return TexturedRenderLayers.getEntityCutout();
    }

    public static RenderLayer entityTranslucentBlockMipped() {
        return TexturedRenderLayers.getItemEntityTranslucentCull();
    }

    public static RenderLayer translucent() {
        return RenderLayer.getTranslucentMovingBlock();
    }

    public static RenderLayer additive() {
        return ADDITIVE;
    }

    public static RenderLayer additive2() {
        return ADDITIVE2;
    }

    public static BiFunction<Identifier, Boolean, RenderLayer> TRAIN_MAP = Util.memoize(RenderTypes::getTrainMap);

    private static RenderLayer getTrainMap(Identifier locationIn, boolean linearFiltering) {
        RenderLayer.MultiPhaseParameters rendertype$state = RenderLayer.MultiPhaseParameters.builder()
            .program(new ShaderProgram(GameRenderer::getRenderTypeTextProgram)).texture(new FilterTexture(locationIn, linearFiltering, false))
            .transparency(TRANSLUCENT_TRANSPARENCY).depthTest(LEQUAL_DEPTH_TEST).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP)
            .overlay(DISABLE_OVERLAY_COLOR).layering(NO_LAYERING).target(MAIN_TARGET).texturing(DEFAULT_TEXTURING).writeMaskState(ALL_MASK)
            .lineWidth(FULL_LINE_WIDTH).colorLogic(NO_COLOR_LOGIC).build(false);
        return RenderLayer.of(
            "create_train_map",
            VertexFormats.POSITION_COLOR_TEXTURE_LIGHT,
            VertexFormat.DrawMode.QUADS,
            256,
            rendertype$state
        );
    }

    public static RenderLayer itemGlowingSolid() {
        return ITEM_GLOWING_SOLID;
    }

    public static RenderLayer itemGlowingTranslucent() {
        return ITEM_GLOWING_TRANSLUCENT;
    }

    public static RenderLayer chain(Identifier pLocation) {
        return CHAIN.apply(pLocation);
    }

    private static String createLayerName(String name) {
        return MOD_ID + ":" + name;
    }

    // Mmm gimme those protected fields
    private RenderTypes() {
        super(null, null, null);
    }

    private static class FilterTexture extends RenderPhase.TextureBase {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private final Optional<Identifier> id;
        private final boolean mipmap;

        public FilterTexture(Identifier id, boolean bilinear, boolean mipmap) {
            super(
                () -> {
                    TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                    AbstractTexture abstractTexture = textureManager.getTexture(id);
                    abstractTexture.setFilter(bilinear, mipmap);
                    RenderSystem.setShaderTexture(0, abstractTexture.getGlId());
                }, () -> {
                }
            );
            this.id = Optional.of(id);
            this.mipmap = mipmap;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.id + "(mipmap=" + this.mipmap + ")]";
        }

        @Override
        public Optional<Identifier> getId() {
            return this.id;
        }
    }
}
