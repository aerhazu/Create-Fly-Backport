package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.AllContraptionTypeTags;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;


public class OrientedContraptionEntityRenderer<C extends OrientedContraptionEntity> extends ContraptionEntityRenderer<C> {
    public OrientedContraptionEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(C entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        if (!super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ))
            return false;
        return entity.getVehicle() != null || !entity.getContraption().getType().is(AllContraptionTypeTags.REQUIRES_VEHICLE_FOR_RENDER);
    }

    @Override
    public void transform(C entity, MatrixStack matrixStack, float partialTicks) {
        int seed = entity.getId();
        float angleInitialYaw = entity.getInitialYaw();
        float angleYaw = -(partialTicks == 1.0F ? entity.yaw : AngleHelper.angleLerp(partialTicks, entity.prevYaw, entity.yaw));
        float anglePitch = partialTicks == 1.0F ? entity.pitch : AngleHelper.angleLerp(partialTicks, entity.prevPitch, entity.pitch);
        matrixStack.translate(-.5f, 0, -.5f);

        Entity ridingEntity = entity.getVehicle();
        AbstractMinecartEntity cart = null;
        AbstractContraptionEntity riding = null;
        if (ridingEntity instanceof AbstractMinecartEntity c) {
            cart = c;
        } else if (ridingEntity instanceof AbstractContraptionEntity be) {
            if (ridingEntity.getVehicle() instanceof AbstractMinecartEntity c) {
                cart = c;
            } else {
                riding = be;
            }
        }

        if (cart != null) {
            OrientedContraptionVisual.repositionOnCart(matrixStack, partialTicks, cart);
        } else if (riding != null) {
            OrientedContraptionVisual.repositionOnContraption(entity, matrixStack, partialTicks, riding);
        }

        TransformStack.of(matrixStack).nudge(seed).center().rotateYDegrees(angleYaw).rotateZDegrees(anglePitch).rotateYDegrees(angleInitialYaw)
            .uncenter();
    }
}
