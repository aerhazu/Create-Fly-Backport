package com.zurrtum.create.client.content.logistics.depot;

import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.depot.EjectorItemEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class EjectorItemEntityRenderer extends ItemEntityRenderer {
    public EjectorItemEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected float getShadowRadius(ItemEntity itemEntity) {
        if (itemEntity.isAlive()) {
            return super.getShadowRadius(itemEntity);
        }
        return 0;
    }

    @Override
    public void render(
        ItemEntity itemEntity,
        float yaw,
        float tickDelta,
        MatrixStack matrixStack,
        VertexConsumerProvider vertexConsumerProvider,
        int light
    ) {
        EjectorItemEntity entity = (EjectorItemEntity) itemEntity;
        ItemStack stack = entity.getStack();
        if (stack.isEmpty())
            return;
        if (entity.isAlive()) {
            super.render(itemEntity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
            return;
        }

        boolean isPackage = PackageItem.isPackage(stack);
        float time = entity.progress + tickDelta;
        float rotateY;
        float rotateX;
        if (isPackage) {
            rotateY = MathHelper.RADIANS_PER_DEGREE * time * 20;
            rotateX = 0;
        } else {
            rotateY = entity.data.rotateY;
            rotateX = MathHelper.RADIANS_PER_DEGREE * time * 40;
        }
        Vec3d location = entity.getLaunchedItemLocation(time).subtract(entity.getPos());

        matrixStack.push();
        matrixStack.translate(0, entity.data.animateOffset + 0.0625F, -0.0625f);
        matrixStack.translate(location.x, location.y, location.z);
        matrixStack.translate(0, 0.25f, 0);
        if (isPackage) {
            matrixStack.translate(0, 0.25f, 0);
            matrixStack.scale(3f, 3f, 3f);
        }
        if (rotateY != 0) {
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(rotateY));
        }
        if (rotateX != 0) {
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(rotateX));
        }
        matrixStack.translate(0, -0.25f, 0);

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        ItemEntityRenderer.renderStack(itemRenderer, matrixStack, vertexConsumerProvider, light, stack, random, entity.getWorld());
        matrixStack.pop();
    }
}
