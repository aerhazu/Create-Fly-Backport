package com.zurrtum.create.client.flywheel.backend.engine;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.flywheel.backend.Samplers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;

public class TextureBinder {
    public static void bind(Identifier resourceLocation) {
        GlStateManager._bindTexture(byName(resourceLocation));
    }

    public static void bindCrumbling(Identifier resourceLocation) {
        Samplers.CRUMBLING.makeActive();
        AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(resourceLocation);
        setupTexture(texture.getGlId());
    }

    public static void bindLightAndOverlay() {
        var gameRenderer = MinecraftClient.getInstance().gameRenderer;

        Samplers.OVERLAY.makeActive();
        gameRenderer.getOverlayTexture().setupOverlayColor();
        setupTexture(RenderSystem.getShaderTexture(1));

        Samplers.LIGHT.makeActive();
        gameRenderer.getLightmapTextureManager().enable();
        setupTexture(RenderSystem.getShaderTexture(2));
    }

    private static void setupTexture(int glId) {
        GlStateManager._bindTexture(glId);
    }

    public static void resetLightAndOverlay() {
        var gameRenderer = MinecraftClient.getInstance().gameRenderer;

        gameRenderer.getOverlayTexture().teardownOverlayColor();
        gameRenderer.getLightmapTextureManager().disable();
    }

    /**
     * Get a built-in texture by its resource location.
     *
     * @param texture The texture's resource location.
     * @return The texture.
     */
    public static int byName(Identifier texture) {
        return MinecraftClient.getInstance().getTextureManager().getTexture(texture).getGlId();
    }
}
