package com.zurrtum.create.client.catnip.gui;

import net.minecraft.client.render.DiffuseLighting;

public interface ILightingSettings {

    void applyLighting();

    static final ILightingSettings DEFAULT_3D = DiffuseLighting::enableForLevel;
    static final ILightingSettings DEFAULT_FLAT = DiffuseLighting::enableGuiDepthLighting;

}
