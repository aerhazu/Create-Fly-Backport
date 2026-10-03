package com.zurrtum.create.client.content.equipment.armor;

import com.google.common.cache.Cache;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.logistics.box.PackageRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.equipment.armor.CardboardArmorHandler;
import com.zurrtum.create.foundation.utility.TickBasedCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;
import java.util.concurrent.ExecutionException;

public class CardboardArmorHandlerClient {
    private static final Cache<Integer, Integer> BOXES_PLAYERS_ARE_HIDING_AS = new TickBasedCache<>(20, true);
    private static final Random RANDOM = new Random();

    public static void keepCacheAliveDesignDespiteNotRendering(AbstractClientPlayerEntity player) {
        if (!CardboardArmorHandler.testForStealth(player))
            return;
        try {
            getCurrentBoxIndex(player.getId());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static boolean playerRendersAsBoxWhenSneaking(
        PlayerEntityRenderer renderer,
        AbstractClientPlayerEntity player,
        float tickProgress,
        MatrixStack ms,
        VertexConsumerProvider vertexConsumerProvider,
        int light
    ) {
        if (player.getPose() != EntityPose.CROUCHING || !CardboardArmorHandler.isCardboardArmor(player.getEquippedStack(EquipmentSlot.HEAD)) || !CardboardArmorHandler.isCardboardArmor(
            player.getEquippedStack(EquipmentSlot.CHEST)) || !CardboardArmorHandler.isCardboardArmor(player.getEquippedStack(EquipmentSlot.LEGS)) || !CardboardArmorHandler.isCardboardArmor(
            player.getEquippedStack(EquipmentSlot.FEET))) {
            return false;
        }

        if (player.getAbilities().flying)
            return false;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (player == mc.player && mc.options.getPerspective() == Perspective.FIRST_PERSON)
            return true;

        ms.push();

        Vec3d renderOffset = renderer.getPositionOffset(player, tickProgress);
        ms.translate(0, -renderOffset.y, 0);

        if (player.isOnGround()) {
            double movement = player.getPos().subtract(player.prevX, player.prevY, player.prevZ).length();
            ms.translate(
                0, Math.min(
                    Math.abs(MathHelper.cos((AnimationTickHolder.getRenderTime() % 256) / 2.0f)) * -renderOffset.y,
                    movement * 5
                ), 0
            );
        }

        float scale = player.getScale();
        ms.scale(scale, scale, scale);

        try {
            PartialModel model = AllPartialModels.PACKAGES_TO_HIDE_AS.get(getCurrentBoxIndex(player.getId()));
            float interpolatedYaw = MathHelper.lerp(tickProgress, player.prevYaw, player.getYaw());
            PackageRenderer.renderBox(player.getId(), interpolatedYaw, ms, vertexConsumerProvider, light, model);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        ms.pop();
        return true;
    }

    private static Integer getCurrentBoxIndex(int id) throws ExecutionException {
        return BOXES_PLAYERS_ARE_HIDING_AS.get(id, () -> RANDOM.nextInt(AllPartialModels.PACKAGES_TO_HIDE_AS.size()));
    }
}
