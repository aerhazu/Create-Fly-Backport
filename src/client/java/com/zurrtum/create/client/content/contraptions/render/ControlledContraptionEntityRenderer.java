package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;

public class ControlledContraptionEntityRenderer extends ContraptionEntityRenderer<ControlledContraptionEntity> {
    public ControlledContraptionEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void transform(ControlledContraptionEntity entity, MatrixStack matrixStack, float partialTicks) {
        float angle = MathHelper.RADIANS_PER_DEGREE * (partialTicks == 1.0F ? entity.angle : AngleHelper.angleLerp(
            partialTicks,
            entity.prevAngle,
            entity.angle
        ));
        Axis axis = entity.getRotationAxis();
        int seed = entity.getId();
        PoseTransformStack transformStack = TransformStack.of(matrixStack).nudge(seed);
        if (axis != null) {
            transformStack.center().rotate(angle, axis).uncenter();
        }
    }
}
