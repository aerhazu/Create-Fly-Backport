package com.zurrtum.create.client.flywheel.impl;

import com.zurrtum.create.client.flywheel.impl.compat.IrisCompat;
import com.zurrtum.create.client.flywheel.impl.extension.PoseStackExtension;
import com.zurrtum.create.client.flywheel.lib.internal.FlwLibLink;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.mixin.MatrixStackAccessor;
import com.zurrtum.create.client.mixin.ModelPartAccessor;
import com.zurrtum.create.client.mixin.ModelPartInvoker;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FlwLibLinkImpl implements FlwLibLink {
    @Override
    public Logger getLogger() {
        return FlwImpl.LOGGER;
    }

    @Override
    public PoseTransformStack getPoseTransformStackOf(MatrixStack stack) {
        return ((PoseStackExtension) stack).flywheel$transformStack();
    }

    @Override
    public Map<String, ModelPart> getModelPartChildren(ModelPart part) {
        return ((ModelPartAccessor) (Object) part).getChildren();
    }

    @Override
    public void compileModelPart(ModelPart part, MatrixStack.Entry pose, VertexConsumer consumer, int light, int overlay, int color) {
        ((ModelPartInvoker) (Object) part).invokeRenderCuboids(pose, consumer, light, overlay, color);
    }

    @Override
    public List<MatrixStack.Entry> getPoseStack(MatrixStack stack) {
        return new ArrayList<>(((MatrixStackAccessor) (Object) stack).getStack());
    }

    @Override
    public boolean isIrisLoaded() {
        return IrisCompat.ACTIVE;
    }

    @Override
    public boolean isOptifineInstalled() {
        //        return OptifineCompat.IS_INSTALLED;
        return false;
    }

    @Override
    public boolean isShaderPackInUse() {
        if (IrisCompat.ACTIVE) {
            return IrisCompat.isShaderPackInUse();
        } /*else if (OptifineCompat.IS_INSTALLED) {
            return OptifineCompat.isShaderPackInUse();
        }*/ else {
            return false;
        }
    }

    @Override
    public boolean isRenderingShadowPass() {
        if (IrisCompat.ACTIVE) {
            return IrisCompat.isRenderingShadowPass();
        } /*else if (OptifineCompat.IS_INSTALLED) {
            return OptifineCompat.isRenderingShadowPass();
        }*/ else {
            return false;
        }
    }
}
