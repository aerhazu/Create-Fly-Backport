package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.redstone.link.controller.LinkedControllerClientHandler;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.List;

/**
 * Backport replacement for 1.21.8's custom item-render-state-based button-press visualization on the
 * Linked Controller item. 1.21.1 has no equivalent state-object rendering pipeline, so this wraps the
 * item's normal baked model, swaps in a "powered" texture variant while any bound button is held, and
 * overlays a small highlight cuboid on top of whichever physical buttons are currently pressed.
 * <p>
 * Button positions were measured directly from the button cap elements authored in
 * {@code models/item/linked_controller/item.json}, relative to the reference button cuboid in
 * {@code models/item/linked_controller/button.json}.
 */
public class LinkedControllerModel implements BakedModel {
    // (dx, dz) in model space (1/16th block units / 16), matching ControlsUtil's control ordering
    private static final float[][] BUTTON_OFFSETS = {{6 / 16f, 8 / 16f}, {2 / 16f, 8 / 16f}, {4 / 16f, 10 / 16f}, {4 / 16f, 6 / 16f}, {5 / 16f, 3 / 16f}, {3 / 16f, 3 / 16f}};

    private static final float[] pressProgress = new float[BUTTON_OFFSETS.length];

    private final BakedModel delegate;

    public LinkedControllerModel(BakedModel delegate) {
        this.delegate = delegate;
    }

    public static void resetButtons() {
        for (int i = 0; i < pressProgress.length; i++) {
            pressProgress[i] = 0;
        }
    }

    public static void tick(MinecraftClient mc) {
        for (int i = 0; i < pressProgress.length; i++) {
            boolean pressed = LinkedControllerClientHandler.currentlyPressed.contains(i);
            float target = pressed ? 1 : 0;
            float speed = 0.5f;
            if (pressProgress[i] < target) {
                pressProgress[i] = Math.min(target, pressProgress[i] + speed);
            } else if (pressProgress[i] > target) {
                pressProgress[i] = Math.max(target, pressProgress[i] - speed);
            }
        }
    }

    public void renderInLectern(
        ModelTransformationMode mode,
        MatrixStack matrices,
        VertexConsumerProvider buffers,
        int light,
        int overlay,
        boolean active,
        boolean renderDepression
    ) {
        MinecraftClient mc = MinecraftClient.getInstance();
        BakedModel bodyModel = active ? poweredModel() : delegate;
        mc.getItemRenderer()
            .renderItem(AllItems.LINKED_CONTROLLER.getDefaultStack(), mode, false, matrices, buffers, light, overlay, bodyModel);

        if (!renderDepression) {
            return;
        }

        SimpleQuadBakedModel button = AllPartialModels.LINKED_CONTROLLER_BUTTON.get();
        if (button == null) {
            return;
        }

        RenderLayer layer = TexturedRenderLayers.getEntityCutout();
        VertexConsumer buffer = buffers.getBuffer(layer);
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        Random random = Random.create();

        for (int i = 0; i < BUTTON_OFFSETS.length; i++) {
            if (pressProgress[i] <= 0) {
                continue;
            }
            float dx = BUTTON_OFFSETS[i][0];
            float dz = BUTTON_OFFSETS[i][1];
            matrices.push();
            matrices.translate(dx, 0, dz);
            mc.getBlockRenderManager()
                .getModelRenderer()
                .render(world, button, Blocks.AIR.getDefaultState(), BlockPos.ORIGIN, matrices, buffer, false, random, 42L, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }
    }

    private static BakedModel poweredModel;

    private static BakedModel poweredModel() {
        if (poweredModel == null) {
            poweredModel = AllPartialModels.LINKED_CONTROLLER_POWERED.get();
        }
        return poweredModel;
    }

    @Override
    public List<BakedQuad> getQuads(net.minecraft.block.BlockState state, Direction face, Random random) {
        return delegate.getQuads(state, face, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return delegate.useAmbientOcclusion();
    }

    @Override
    public boolean hasDepth() {
        return delegate.hasDepth();
    }

    @Override
    public boolean isSideLit() {
        return delegate.isSideLit();
    }

    @Override
    public boolean isBuiltin() {
        return delegate.isBuiltin();
    }

    @Override
    public Sprite getParticleSprite() {
        return delegate.getParticleSprite();
    }

    @Override
    public ModelTransformation getTransformation() {
        return delegate.getTransformation();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return delegate.getOverrides();
    }
}
