package com.zurrtum.create.client.catnip.client.render.model;

import net.minecraft.client.render.RenderLayer;

import net.minecraft.client.render.BuiltBuffer;

public interface ShadeSeparatedResultConsumer {
    void accept(RenderLayer renderType, boolean shaded, BuiltBuffer data);
}
