package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.render.RenderLayer;

import com.zurrtum.create.client.flywheel.api.material.Material;
import org.jetbrains.annotations.Nullable;

public interface BlockMaterialFunction {
    @Nullable Material apply(RenderLayer chunkRenderType, boolean shaded, boolean ambientOcclusion);
}
