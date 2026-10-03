package com.zurrtum.create.client.ponder.foundation.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.render.DefaultSuperRenderTypeBuffer;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.foundation.gui.render.MachinePreviewHelper;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.PonderScene.SceneTransform;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SceneRenderer {
    private static final Vector3f DIFFUSE_LIGHT_0 = new Vector3f(0.4F, -1.0F, 0.7F).normalize();
    private static final Vector3f DIFFUSE_LIGHT_1 = new Vector3f(-0.4F, -0.5F, 0.7F).normalize();

    public static void render(
        DrawContext graphics,
        PonderScene scene,
        int width,
        int height,
        double slide,
        boolean userViewMode,
        LerpedFloat finishingFlash,
        float partialTicks
    ) {
        MatrixStack matrices = graphics.getMatrices();
        matrices.push();

        RenderSystem.setShaderLights(DIFFUSE_LIGHT_0, DIFFUSE_LIGHT_1);
        VertexConsumerProvider.Immediate vertexConsumers = MachinePreviewHelper.buffer();
        renderScene(scene, width, height, slide, finishingFlash, partialTicks, matrices, vertexConsumers);
        DiffuseLighting.enableGuiDepthLighting();

        MachinePreviewHelper.end(matrices, vertexConsumers);
    }

    private static void renderScene(
        PonderScene scene,
        int width,
        int height,
        double slide,
        LerpedFloat finishingFlash,
        float partialTicks,
        MatrixStack poseStack,
        VertexConsumerProvider.Immediate vertexConsumers
    ) {
        SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
        poseStack.translate(0, 0, -800);
        SceneTransform transform = scene.getTransform();
        transform.updateScreenParams(width, height, slide);
        transform.apply(poseStack, partialTicks);
        transform.updateSceneRVE(partialTicks);
        scene.renderScene(buffer, poseStack, partialTicks);
        buffer.draw();

        // kool shadow fx
        if (!scene.shouldHidePlatformShadow()) {
            poseStack.push();
            poseStack.translate(scene.getBasePlateOffsetX(), 0, scene.getBasePlateOffsetZ());
            UIRenderHelper.flipForGuiRender(poseStack);

            float flash = finishingFlash.getValue(partialTicks) * .9f;
            float alpha = flash;
            flash *= flash;
            flash = ((flash * 2) - 1);
            flash *= flash;
            flash = 1 - flash;

            for (int f = 0; f < 4; f++) {
                poseStack.translate(scene.getBasePlateSize(), 0, 0);
                poseStack.push();
                poseStack.translate(0, 0, -1 / 1024f);
                if (flash > 0) {
                    poseStack.push();
                    poseStack.scale(1, .5f + flash * .75f, 1);
                    fillGradient(
                        vertexConsumers,
                        poseStack,
                        0,
                        -1,
                        -scene.getBasePlateSize(),
                        0,
                        0,
                        new Color(0x00_c6ffc9).getRGB(),
                        new Color(0xaa_c6ffc9).scaleAlpha(alpha).getRGB()
                    );
                    poseStack.pop();
                }
                poseStack.translate(0, 0, 2 / 1024f);
                fillGradient(
                    vertexConsumers,
                    poseStack,
                    0,
                    0,
                    -scene.getBasePlateSize(),
                    4,
                    0,
                    new Color(0x66_000000).getRGB(),
                    new Color(0x00_000000).getRGB()
                );
                poseStack.pop();
                poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
            }
            poseStack.pop();
        }
    }

    public static void fillGradient(
        VertexConsumerProvider vertexConsumers,
        MatrixStack matrices,
        int x1,
        int y1,
        int x2,
        int y2,
        int z,
        int colorFrom,
        int colorTo
    ) {
        VertexConsumer buffer = vertexConsumers.getBuffer(PonderRenderTypes.getGui());
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        buffer.vertex(matrix4f, (float) x1, (float) y1, (float) z).color(colorFrom);
        buffer.vertex(matrix4f, (float) x1, (float) y2, (float) z).color(colorTo);
        buffer.vertex(matrix4f, (float) x2, (float) y2, (float) z).color(colorTo);
        buffer.vertex(matrix4f, (float) x2, (float) y1, (float) z).color(colorFrom);
    }
}
