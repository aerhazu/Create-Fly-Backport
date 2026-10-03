package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.infrastructure.component.SandPaperItemComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Unit;

public class SandPaperRenderer {
    private static final ItemStack stack = AllItems.SAND_PAPER.getDefaultStack();

    static {
        stack.set(AllDataComponents.SAND_PAPER_JEI, Unit.INSTANCE);
    }

    public static void render(DrawContext graphics, float x, float y, ItemStack target) {
        MatrixStack matrices = MachinePreviewHelper.begin(graphics, x, y, 28, 29, 32);

        matrices.translate(0, -0.35f, 0);
        matrices.scale(1, -1, -1);
        DiffuseLighting.disableGuiDepthLighting();
        stack.set(AllDataComponents.SAND_PAPER_POLISHING, new SandPaperItemComponent(target));

        MinecraftClient mc = MinecraftClient.getInstance();
        BakedModel model = mc.getItemRenderer().getModels().getModel(stack);
        VertexConsumerProvider.Immediate vertexConsumers = MachinePreviewHelper.buffer();
        mc.getItemRenderer().renderItem(
            stack,
            ModelTransformationMode.GUI,
            false,
            matrices,
            vertexConsumers,
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
            OverlayTexture.DEFAULT_UV,
            model
        );
        DiffuseLighting.enableGuiDepthLighting();

        MachinePreviewHelper.end(matrices, vertexConsumers);
    }
}
