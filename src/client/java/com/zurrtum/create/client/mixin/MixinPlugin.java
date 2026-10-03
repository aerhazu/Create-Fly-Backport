package com.zurrtum.create.client.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    private List<String> mixins;

    @Override
    public void onLoad(String mixinPackage) {
        mixins = new ArrayList<>();
        FabricLoader loader = FabricLoader.getInstance();
        if (loader.isModLoaded("sodium")) {
            // No FabricModelAccessMixin here: upstream needs it to feed world/pos into
            // PlatformModelAccess#getQuads, but this backport routes every position-dependent model
            // through PositionAwareBakedModel#emitBlockQuads (isVanillaAdapter() == false), which
            // Sodium 0.6's built-in FRAPI support already calls with full context.
            mixins.add("AbstractBlockRenderContextMixin");
        }
        if (loader.isModLoaded("iris")) {
            mixins.add("GraphTranslucencyRenderOrderManagerMixin");
        }
        // No ModernFix branch: DynamicModelProviderMixin targeted loadBlockModelDefault, which does not
        // exist on 1.21.1's ModernFix (DynamicModelProvider only exposes getModel). Adding it crashed
        // startup for anyone running ModernFix, so the integration is dropped rather than half-present.
        if (!loader.isModLoaded("fabric-item-group-api-v1")) {
            mixins.add("CreativeInventoryScreenMixin");
        }
        if (loader.isModLoaded("fabric-renderer-indigo")) {
            mixins.add("BlockRenderInfoMixin");
            mixins.add("AbstractTerrainRenderContextMixin");
        }
        if (loader.isModLoaded("fabric-rendering-fluids-v1")) {
            mixins.add("WaterRenderHandlerMixin");
        }
        if (loader.isModLoaded("fabric-transfer-api-v1")) {
            mixins.add("FluidVariantRenderHandlerMixin");
        }
        // No LoadBakedModelMixin fallback: it hooked BakedModelManager#method_65750, which is a later
        // version's method and absent on 1.21.1, so it could never have applied. Its guard (Fabric's
        // model loading API missing) cannot occur anyway, since this mod requires Fabric API.
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return mixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
