package com.zurrtum.create.client.content.equipment.extendoGrip;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class ExtendoGripRenderHandler {

    public static float mainHandAnimation;
    public static float lastMainHandAnimation;
    public static boolean holding;

    public static void tick(MinecraftClient mc) {
        lastMainHandAnimation = mainHandAnimation;
        mainHandAnimation *= MathHelper.clamp(mainHandAnimation, 0.8f, 0.99f);

        holding = false;
        if (!getRenderedOffHandStack(mc).isOf(AllItems.EXTENDO_GRIP))
            return;
        ItemStack main = getRenderedMainHandStack(mc);
        if (main.isEmpty())
            return;
        if (!(main.getItem() instanceof BlockItem))
            return;
        BakedModel model = mc.getItemRenderer().getModel(main, mc.world, mc.player, 0);
        if (!model.isSideLit())
            return;
        holding = true;
    }

    public static boolean onRenderPlayerHand(
        ItemStack heldItem,
        MinecraftClient mc,
        EntityRenderDispatcher entityRenderDispatcher,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        Hand hand,
        float equipProgress,
        float swingProgress
    ) {
        ItemStack offhandItem = getRenderedOffHandStack(mc);
        boolean inOffhand = offhandItem.isOf(AllItems.EXTENDO_GRIP);
        boolean inHeldItem = heldItem.isOf(AllItems.EXTENDO_GRIP);
        if (!inOffhand && !inHeldItem)
            return false;
        ClientPlayerEntity player = mc.player;
        boolean rightHand = hand == Hand.MAIN_HAND ^ player.getMainArm() == Arm.LEFT;

        var msr = TransformStack.of(ms);
        float flip = rightHand ? 1.0F : -1.0F;
        boolean blockItem = heldItem.getItem() instanceof BlockItem;
        equipProgress = blockItem ? 0 : equipProgress / 4;

        ms.push();
        if (hand == Hand.MAIN_HAND) {
            if (1 - swingProgress > mainHandAnimation && swingProgress > 0 && swingProgress < 0.1)
                mainHandAnimation = 0.95f;

            ms.translate(flip * (0.64000005F - .1f), -0.4F + equipProgress * -0.6F, -0.71999997F + .3f);

            ms.push();
            msr.rotateYDegrees(flip * 75.0F);
            ms.translate(flip * -1.0F, 3.6F, 3.5F);
            msr.rotateZDegrees(flip * 120).rotateXDegrees(200).rotateYDegrees(flip * -135.0F);
            ms.translate(flip * 5.6F, 0.0F, 0.0F);
            msr.rotateYDegrees(flip * 40.0F);
            ms.translate(flip * 0.05f, -0.3f, -0.3f);

            PlayerEntityRenderer playerrenderer = (PlayerEntityRenderer) entityRenderDispatcher.getRenderer(player);
            if (rightHand)
                playerrenderer.renderRightArm(ms, buffer, light, player);
            else
                playerrenderer.renderLeftArm(ms, buffer, light, player);
            ms.pop();

            // Render gun
            ms.push();
            ms.translate(flip * -0.1f, 0, -0.3f);
            ItemStack toRender = inHeldItem && inOffhand ? ItemStack.EMPTY : heldItem;
            if (!toRender.isEmpty()) {
                ModelTransformationMode displayContext = rightHand ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND;
                BakedModel model = mc.getItemRenderer().getModel(toRender, mc.world, player, 0);
                mc.getItemRenderer().renderItem(toRender, displayContext, false, ms, buffer, light, OverlayTexture.DEFAULT_UV, model);
            }
            ms.pop();
        }
        ms.pop();
        return true;
    }

    private static ItemStack getRenderedMainHandStack(MinecraftClient mc) {
        return mc.getEntityRenderDispatcher().getHeldItemRenderer().mainHand;
    }

    private static ItemStack getRenderedOffHandStack(MinecraftClient mc) {
        return mc.getEntityRenderDispatcher().getHeldItemRenderer().offHand;
    }

}
