package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import net.minecraft.util.Identifier;

import java.util.Map;

public final class PartialModelEventHandler {
    private PartialModelEventHandler() {
    }

    public static Map<Identifier, PartialModel> getRegisterAdditional() {
        return PartialModel.ALL;
    }

    public static void onBakingCompleted(PartialModel partial, SimpleQuadBakedModel bakedModel) {
        partial.bakedModel = bakedModel;
    }
}
