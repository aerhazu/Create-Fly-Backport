package com.zurrtum.create.client.catnip.gui.element;

import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import com.zurrtum.create.client.model.LayerBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

import java.util.function.BiConsumer;

public class GuiGameElement {
    public static GuiItemRenderBuilder of(ItemStack stack) {
        return new GuiItemRenderBuilder(stack);
    }

    public static GuiItemRenderBuilder of(Item item) {
        return new GuiItemRenderBuilder(item.getDefaultStack());
    }

    public static GuiBlockStateRenderBuilder of(BlockState block) {
        return new GuiBlockStateRenderBuilder(block);
    }

    public static GuiPartialRenderBuilder partial() {
        return new GuiPartialRenderBuilder();
    }

    public static GuiPartialRenderBuilder of(PartialModel model) {
        return new GuiPartialRenderBuilder(model);
    }

    public static abstract class GuiRenderBuilder<T extends GuiRenderBuilder<T>> extends AbstractRenderElement {
        protected float xRot, yRot, zRot;
        protected float scale = 1;
        protected int padding;

        abstract T self();

        public T padding(int padding) {
            this.padding = padding;
            return self();
        }

        public T rotate(float x, float y, float z) {
            xRot = MathHelper.RADIANS_PER_DEGREE * x;
            yRot = MathHelper.RADIANS_PER_DEGREE * y;
            zRot = MathHelper.RADIANS_PER_DEGREE * z;
            return self();
        }

        public T scale(float scale) {
            this.scale = scale;
            return self();
        }
    }

    private static void applyRotation(MatrixStack matrices, float xRot, float yRot, float zRot) {
        if (zRot != 0) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(zRot));
        }
        if (xRot != 0) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(xRot));
        }
        if (yRot != 0) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(yRot));
        }
    }

    public static class GuiItemRenderBuilder extends GuiRenderBuilder<GuiItemRenderBuilder> {
        private final ItemStack stack;

        public GuiItemRenderBuilder(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        GuiItemRenderBuilder self() {
            return this;
        }

        @Override
        public void render(DrawContext graphics) {
            if (scale <= 1 && xRot == 0 && yRot == 0 && zRot == 0) {
                if (scale == 1) {
                    graphics.drawItem(stack, (int) x, (int) y);
                } else {
                    MatrixStack matrices = graphics.getMatrices();
                    matrices.push();
                    matrices.scale(scale, 1, 1);
                    graphics.drawItem(stack, (int) x, (int) y);
                    matrices.pop();
                }
                return;
            }
            MinecraftClient mc = MinecraftClient.getInstance();
            BakedModel model = mc.getItemRenderer().getModels().getModel(stack);
            float boxSize = scale * 16 + padding;
            float contentSize = scale * 16;
            MatrixStack matrices = graphics.getMatrices();
            matrices.push();
            matrices.translate(x + boxSize / 2, y + boxSize / 2, 100 + z);
            matrices.scale(contentSize, -contentSize, contentSize);
            applyRotation(matrices, xRot, yRot, zRot);
            boolean sideLit = model.isSideLit();
            if (!sideLit) {
                DiffuseLighting.disableGuiDepthLighting();
            }
            VertexConsumerProvider.Immediate buffer = mc.getBufferBuilders().getEntityVertexConsumers();
            mc.getItemRenderer().renderItem(
                stack,
                ModelTransformationMode.GUI,
                false,
                matrices,
                buffer,
                LightmapTextureManager.MAX_LIGHT_COORDINATE,
                OverlayTexture.DEFAULT_UV,
                model
            );
            buffer.draw();
            if (!sideLit) {
                DiffuseLighting.enableGuiDepthLighting();
            }
            matrices.pop();
        }

        @Override
        public void clear() {
        }
    }

    public static class GuiBlockStateRenderBuilder extends GuiRenderBuilder<GuiBlockStateRenderBuilder> {
        private final BlockState block;

        public GuiBlockStateRenderBuilder(BlockState block) {
            this.block = block;
        }

        @Override
        GuiBlockStateRenderBuilder self() {
            return this;
        }

        @Override
        public void render(DrawContext graphics) {
            MinecraftClient mc = MinecraftClient.getInstance();
            float boxSize = scale * 16 + padding;
            float contentSize = scale * 16;
            MatrixStack matrices = graphics.getMatrices();
            matrices.push();
            matrices.translate(x + boxSize / 2, y + boxSize / 2, 100 + z);
            matrices.scale(contentSize, -contentSize, contentSize);
            applyRotation(matrices, xRot, yRot, zRot);
            matrices.translate(-0.5F, -0.5F, -0.5F);
            VertexConsumerProvider.Immediate buffer = mc.getBufferBuilders().getEntityVertexConsumers();
            mc.getBlockRenderManager()
                .renderBlockAsEntity(block, matrices, buffer, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
            buffer.draw();
            matrices.pop();
        }

        @Override
        public void clear() {
        }
    }

    public static class GuiPartialRenderBuilder extends AbstractRenderElement {
        private PartialModel model;
        private float scale = 1;
        private BiConsumer<MatrixStack, Float> transform;
        private float partialTicks;
        private int padding;
        private float xLocal, yLocal;

        public GuiPartialRenderBuilder() {
        }

        public GuiPartialRenderBuilder(PartialModel model) {
            this.model = model;
        }

        @Override
        public void render(DrawContext graphics) {
            if (model == null) {
                return;
            }
            SimpleQuadBakedModel bakedModel = model.get();
            if (bakedModel == null) {
                return;
            }
            MinecraftClient mc = MinecraftClient.getInstance();
            float size = scale * 16;
            MatrixStack matrices = graphics.getMatrices();
            matrices.push();
            matrices.translate(x + xLocal, y + yLocal, 100 + z);
            matrices.scale(size, size, size);
            if (transform != null) {
                transform.accept(matrices, partialTicks);
            }
            RenderLayer blockRenderLayer = LayerBakedModel.getBlockRenderLayer(bakedModel, RenderLayer::getSolid);
            RenderLayer layer = blockRenderLayer == RenderLayer.getTranslucent() ? TexturedRenderLayers.getItemEntityTranslucentCull() : TexturedRenderLayers.getEntityCutout();
            VertexConsumerProvider.Immediate buffer = mc.getBufferBuilders().getEntityVertexConsumers();
            SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
            mc.getBlockRenderManager().getModelRenderer().render(
                world,
                bakedModel,
                Blocks.AIR.getDefaultState(),
                BlockPos.ORIGIN,
                matrices,
                buffer.getBuffer(layer),
                false,
                Random.create(),
                42L,
                OverlayTexture.DEFAULT_UV
            );
            buffer.draw();
            matrices.pop();
        }

        public GuiPartialRenderBuilder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public GuiPartialRenderBuilder transform(BiConsumer<MatrixStack, Float> transform) {
            this.transform = transform;
            return this;
        }

        public GuiPartialRenderBuilder partial(PartialModel model) {
            this.model = model;
            return this;
        }

        public GuiPartialRenderBuilder padding(int padding) {
            this.padding = padding;
            return this;
        }

        public GuiPartialRenderBuilder atLocal(float x, float y) {
            xLocal = x;
            yLocal = y;
            return this;
        }

        public void markDirty() {
        }

        public void tick(float partialTicks) {
            this.partialTicks = partialTicks;
        }

        @Override
        public void clear() {
        }
    }
}
