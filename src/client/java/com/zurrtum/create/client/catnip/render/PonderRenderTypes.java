package com.zurrtum.create.client.catnip.render;

import com.zurrtum.create.client.ponder.enums.PonderSpecialTextures;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class PonderRenderTypes {
    public static RenderLayer getGui() {
        return RenderLayer.getGui();
    }

    public static RenderLayer translucent() {
        return RenderLayer.getTranslucentMovingBlock();
    }

    public static RenderLayer outlineSolid() {
        return RenderLayer.getEntitySolid(PonderSpecialTextures.BLANK.getLocation());
    }

    public static RenderLayer outlineTranslucent(Identifier texture, boolean cull) {
        return RenderLayer.getEntityTranslucent(texture, cull);
    }

    //TODO vanilla uses the translucent render type for fluids, need to investigate if this is even needed
    public static RenderLayer fluid() {
        return RenderLayer.getTranslucentMovingBlock();
    }
}
