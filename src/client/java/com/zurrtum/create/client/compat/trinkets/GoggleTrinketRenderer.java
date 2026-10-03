package com.zurrtum.create.client.compat.trinkets;

import com.zurrtum.create.AllItems;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class GoggleTrinketRenderer implements TrinketRenderer {
    public static void register() {
        TrinketRendererRegistry.registerRenderer(AllItems.GOGGLES, new GoggleTrinketRenderer());
    }

    @Override
    public void render(
        ItemStack stack,
        SlotReference slotReference,
        EntityModel<? extends LivingEntity> contextModel,
        MatrixStack matrices,
        VertexConsumerProvider buffers,
        int light,
        LivingEntity entity,
        float limbAngle,
        float limbDistance,
        float ageInTicks,
        float headYaw,
        float headPitch,
        float partialTicks
    ) {
        if (stack.isOf(AllItems.GOGGLES) && contextModel instanceof PlayerEntityModel && entity instanceof AbstractClientPlayerEntity player) {
            @SuppressWarnings("unchecked")
            PlayerEntityModel<AbstractClientPlayerEntity> entityModel = (PlayerEntityModel<AbstractClientPlayerEntity>) contextModel;
            matrices.push();
            TrinketRenderer.translateToFace(matrices, entityModel, player, headYaw, headPitch);
            if (headOccupied(player, slotReference)) {
                matrices.translate(0.0F, -0.5F, 0.0F);
            }
            MinecraftClient mc = MinecraftClient.getInstance();
            mc.getItemRenderer().renderItem(stack, ModelTransformationMode.HEAD, light, OverlayTexture.DEFAULT_UV, matrices, buffers, mc.world, 0);
            matrices.pop();
        }
    }

    public static boolean headOccupied(LivingEntity entity, SlotReference slotReference) {
        if (!entity.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            return true;
        }
        TrinketInventory inv = slotReference.inventory().getComponent().getInventory().get("head").get("hat");
        return inv != null && !inv.isEmpty();
    }
}
