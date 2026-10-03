package com.zurrtum.create.client.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public final class NamedBlockRenderLayer {
    private static final ImmutableMap<String, RenderLayer> RENDER_TYPES = Util.make(() -> {
        ImmutableMap.Builder<String, RenderLayer> builder = ImmutableMap.builder();
        builder.put("minecraft:solid", RenderLayer.getSolid());
        builder.put("minecraft:cutout", RenderLayer.getCutout());
        builder.put("minecraft:cutout_mipped", RenderLayer.getCutoutMipped());
        builder.put("minecraft:cutout_mipped_all", RenderLayer.getCutoutMipped());
        builder.put("minecraft:translucent", RenderLayer.getTranslucent());
        builder.put("minecraft:tripwire", RenderLayer.getTripwire());
        return builder.build();
    });

    @Nullable
    public static RenderLayer get(String name) {
        return RENDER_TYPES.get(name);
    }
}
