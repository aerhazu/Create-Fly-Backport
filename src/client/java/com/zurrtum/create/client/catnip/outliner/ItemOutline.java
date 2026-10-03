package com.zurrtum.create.client.catnip.outliner;

import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public class ItemOutline extends Outline {

    protected Vec3d pos;
    protected ItemStack stack;

    public ItemOutline(Vec3d pos, ItemStack stack) {
        this.pos = pos;
        this.stack = stack;
    }

    @Override
    public void render(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d camera, float pt) {
        ms.push();

        ms.translate(pos.x - camera.x, pos.y - camera.y, pos.z - camera.z);
        ms.scale(params.alpha, params.alpha, params.alpha);

        var itemRenderer = mc.getItemRenderer();
        var model = itemRenderer.getModels().getModel(stack);
        itemRenderer.renderItem(
            stack,
            ModelTransformationMode.FIXED,
            false,
            ms,
            buffer,
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
            OverlayTexture.DEFAULT_UV,
            model
        );

        ms.pop();
    }
}
