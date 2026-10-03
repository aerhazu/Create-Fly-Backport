package com.zurrtum.create.client.compat.accessories;

import com.zurrtum.create.AllItems;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class GoggleAccessoryRenderer implements AccessoryRenderer {
    public static void register() {
        AccessoriesRendererRegistry.registerRenderer(AllItems.GOGGLES, GoggleAccessoryRenderer::new);
    }

    @Override
    public <M extends LivingEntity> void render(
        ItemStack stack,
        SlotReference reference,
        MatrixStack matrices,
        EntityModel<M> model,
        VertexConsumerProvider buffers,
        int light,
        float limbAngle,
        float limbDistance,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        float partialTicks
    ) {
        if (model instanceof PlayerEntityModel<M> entityModel) {
            matrices.push();
            AccessoryRenderer.translateToFace(matrices, entityModel, reference.entity());
            MinecraftClient mc = MinecraftClient.getInstance();
            mc.getItemRenderer().renderItem(stack, ModelTransformationMode.HEAD, light, OverlayTexture.DEFAULT_UV, matrices, buffers, mc.world, 0);
            matrices.pop();
        }
    }
}
