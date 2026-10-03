package com.zurrtum.create.client.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModelPart.class)
public interface ModelPartInvoker {
    @Invoker("renderCuboids")
    void invokeRenderCuboids(MatrixStack.Entry pose, VertexConsumer consumer, int light, int overlay, int color);
}
