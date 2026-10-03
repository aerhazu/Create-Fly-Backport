package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.render.RenderLayer;

import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.lib.internal.FlwLibXplat;
import com.zurrtum.create.client.flywheel.lib.model.ModelUtil;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public final class BakedModelBuilder {
    final BakedModel model;
    @Nullable BlockRenderView level;
    @Nullable BlockPos pos;
    @Nullable MatrixStack poseStack;
    @Nullable BlockMaterialFunction materialFunc;

    public BakedModelBuilder(BakedModel model) {
        this.model = model;
    }

    public BakedModelBuilder level(@Nullable BlockRenderView level) {
        this.level = level;
        return this;
    }

    public BakedModelBuilder pos(@Nullable BlockPos pos) {
        this.pos = pos;
        return this;
    }

    public BakedModelBuilder poseStack(@Nullable MatrixStack poseStack) {
        this.poseStack = poseStack;
        return this;
    }

    @Deprecated(forRemoval = true)
    public BakedModelBuilder materialFunc(@Nullable BiFunction<RenderLayer, Boolean, Material> materialFunc) {
        if (materialFunc != null) {
            this.materialFunc = (chunkRenderType, shaded, ambientOcclusion) -> materialFunc.apply(chunkRenderType, shaded);
        } else {
            this.materialFunc = null;
        }
        return this;
    }

    public BakedModelBuilder materialFunc(@Nullable BlockMaterialFunction materialFunc) {
        this.materialFunc = materialFunc;
        return this;
    }

    public SimpleModel build() {
        if (level == null) {
            level = EmptyVirtualBlockGetter.FULL_DARK;
        }
        if (pos == null) {
            pos = BlockPos.ORIGIN;
        }
        if (materialFunc == null) {
            materialFunc = ModelUtil::getMaterial;
        }

        return FlwLibXplat.INSTANCE.buildBakedModelBuilder(this);
    }
}
