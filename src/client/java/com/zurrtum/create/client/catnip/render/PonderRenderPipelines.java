package com.zurrtum.create.client.catnip.render;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PonderRenderPipelines {
    private static final Map<Identifier, RenderLayer> GUI_TEXTURED_CACHE = new HashMap<>();

    public static RenderLayer guiTextured(Identifier texture) {
        return GUI_TEXTURED_CACHE.computeIfAbsent(
            texture, id -> RenderLayer.of(
                "create_gui_textured",
                VertexFormats.POSITION_TEXTURE_COLOR,
                VertexFormat.DrawMode.QUADS,
                256,
                RenderLayer.MultiPhaseParameters.builder().program(new RenderPhase.ShaderProgram(GameRenderer::getPositionTexColorProgram))
                    .texture(new RenderPhase.Texture(id, false, false)).transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .depthTest(RenderPhase.ALWAYS_DEPTH_TEST).cull(RenderPhase.DISABLE_CULLING).lightmap(RenderPhase.DISABLE_LIGHTMAP)
                    .overlay(RenderPhase.DISABLE_OVERLAY_COLOR).layering(RenderPhase.NO_LAYERING).target(RenderPhase.MAIN_TARGET)
                    .texturing(RenderPhase.DEFAULT_TEXTURING).writeMaskState(RenderPhase.ALL_MASK).lineWidth(RenderPhase.FULL_LINE_WIDTH)
                    .colorLogic(RenderPhase.NO_COLOR_LOGIC).build(false)
            )
        );
    }

    private static RenderLayer.MultiPhaseParameters positionColorParameters() {
        return RenderLayer.MultiPhaseParameters.builder().program(new RenderPhase.ShaderProgram(GameRenderer::getPositionColorProgram))
            .transparency(RenderPhase.NO_TRANSPARENCY).depthTest(RenderPhase.ALWAYS_DEPTH_TEST).cull(RenderPhase.DISABLE_CULLING)
            .lightmap(RenderPhase.DISABLE_LIGHTMAP).overlay(RenderPhase.DISABLE_OVERLAY_COLOR).layering(RenderPhase.NO_LAYERING)
            .target(RenderPhase.MAIN_TARGET).texturing(RenderPhase.DEFAULT_TEXTURING).writeMaskState(RenderPhase.ALL_MASK)
            .lineWidth(RenderPhase.FULL_LINE_WIDTH).colorLogic(RenderPhase.NO_COLOR_LOGIC).build(false);
    }

    public static final RenderLayer TRIANGLE_FAN = RenderLayer.of(
        "create_triangle_fan",
        VertexFormats.POSITION_COLOR,
        VertexFormat.DrawMode.TRIANGLE_FAN,
        256,
        positionColorParameters()
    );
    public static final RenderLayer POSITION_COLOR_TRIANGLES = RenderLayer.of(
        "create_position_color_triangles",
        VertexFormats.POSITION_COLOR,
        VertexFormat.DrawMode.TRIANGLES,
        256,
        positionColorParameters()
    );
    public static final RenderLayer POSITION_COLOR_STRIP = RenderLayer.of(
        "create_position_color_strip",
        VertexFormats.POSITION_COLOR,
        VertexFormat.DrawMode.TRIANGLE_STRIP,
        256,
        positionColorParameters()
    );
}
