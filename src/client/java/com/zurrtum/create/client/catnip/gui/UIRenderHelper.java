package com.zurrtum.create.client.catnip.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.render.BreadcrumbArrowRenderState;
import com.zurrtum.create.client.catnip.gui.render.GradientRectRenderState;
import com.zurrtum.create.client.catnip.gui.render.RadialSectorRenderState;
import com.zurrtum.create.client.catnip.gui.render.TexturedQuadRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class UIRenderHelper {
    public static final Couple<Color> COLOR_TEXT = Couple.create(new Color(0xff_eeeeee), new Color(0xff_a3a3a3)).map(Color::setImmutable);
    public static final Couple<Color> COLOR_TEXT_DARKER = Couple.create(new Color(0xff_a3a3a3), new Color(0xff_808080)).map(Color::setImmutable);
    public static final Couple<Color> COLOR_TEXT_ACCENT = Couple.create(new Color(0xff_ddeeff), new Color(0xff_a0b0c0)).map(Color::setImmutable);
    public static final Couple<Color> COLOR_TEXT_STRONG_ACCENT = Couple.create(new Color(0xff_8ab6d6), new Color(0xff_6e92ab))
        .map(Color::setImmutable);

    public static final Color COLOR_STREAK = new Color(0x101010, false).setImmutable();

    /**
     * An FBO that has a stencil buffer for use wherever stencil are necessary. Forcing the main FBO to have a stencil
     * buffer will cause GL error spam when using fabulous graphics.
     */
    @Nullable
    public static CustomRenderTarget framebuffer;

    public static void init() {
        RenderSystem.assertOnRenderThread();
        Window mainWindow = MinecraftClient.getInstance().getWindow();
        framebuffer = CustomRenderTarget.create(mainWindow);
    }

    public static void updateWindowSize(Window mainWindow) {
        if (framebuffer != null)
            framebuffer.resize(mainWindow.getFramebufferWidth(), mainWindow.getFramebufferHeight(), false);
    }

    /**
     * Switch from src to dst, after copying the contents of src to dst.
     */
    public static void swapAndBlitColor(Framebuffer src, Framebuffer dst) {
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, src.fbo);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, dst.fbo);
        GlStateManager._glBlitFrameBuffer(
            0,
            0,
            src.viewportWidth,
            src.viewportHeight,
            0,
            0,
            dst.viewportWidth,
            dst.viewportHeight,
            GL30.GL_COLOR_BUFFER_BIT,
            GL20.GL_LINEAR
        );

        GlStateManager._glBindFramebuffer(com.mojang.blaze3d.platform.GlConst.GL_FRAMEBUFFER, dst.fbo);
    }

    /**
     * @param angle   angle in degrees, 0 means fading to the right
     * @param x       x-position of the starting edge middle point
     * @param y       y-position of the starting edge middle point
     * @param breadth total width of the streak
     * @param length  total length of the streak
     */
    public static void streak(DrawContext graphics, float angle, int x, int y, int breadth, int length) {
        streak(graphics, angle, x, y, breadth, length, COLOR_STREAK);
    }

    public static void streak(DrawContext graphics, float angle, int x, int y, int breadth, int length, Color c) {
        Color color = c.copy().setImmutable();
        Color c1 = color.scaleAlpha(0.625f);
        Color c2 = color.scaleAlpha(0.5f);
        Color c3 = color.scaleAlpha(0.0625f);
        Color c4 = color.scaleAlpha(0f);

        MatrixStack poseStack = graphics.getMatrices();
        poseStack.push();
        poseStack.translate(x, y, 0);
        poseStack.multiply(RotationAxis.POSITIVE_Z.rotation((float) ((angle - 90) * (Math.PI / 180.0))));

        streak(graphics, breadth / 2, length, c1, c2, c3, c4);

        poseStack.pop();
    }

    private static void streak(DrawContext graphics, int width, int height, Color c1, Color c2, Color c3, Color c4) {
        if (NavigatableSimiScreen.isCurrentlyRenderingPreviousScreen())
            return;

        double split1 = .5;
        double split2 = .75;
        graphics.fillGradient(-width, 0, width, (int) (split1 * height), c1.getRGB(), c2.getRGB());
        graphics.fillGradient(-width, (int) (split1 * height), width, (int) (split2 * height), c2.getRGB(), c3.getRGB());
        graphics.fillGradient(-width, (int) (split2 * height), width, height, c3.getRGB(), c4.getRGB());
    }

    /**
     * @see #angledGradient(DrawContext, float, int, int, float, float, Color, Color)
     */
    public static void angledGradient(DrawContext graphics, float angle, int x, int y, float breadth, float length, Couple<Color> c) {
        angledGradient(graphics, angle, x, y, breadth, length, c.getFirst(), c.getSecond());
    }

    /**
     * x and y specify the middle point of the starting edge
     *
     * @param angle      the angle of the gradient in degrees; 0° means from left to right
     * @param startColor the color at the starting edge
     * @param endColor   the color at the ending edge
     * @param breadth    the total width of the gradient
     */
    public static void angledGradient(
        DrawContext graphics,
        float angle,
        int x,
        int y,
        float breadth,
        float length,
        Color startColor,
        Color endColor
    ) {
        MatrixStack poseStack = graphics.getMatrices();
        poseStack.push();
        poseStack.translate(x, y, 0);
        poseStack.multiply(RotationAxis.POSITIVE_Z.rotation((float) ((angle - 90) * (Math.PI / 180.0))));

        float w = breadth / 2;
        //graphics.fillGradient(-w, 0, w, length, startColor.getRGB(), endColor.getRGB());
        drawGradientRect(graphics, -w, 0f, w, length, startColor, endColor);

        poseStack.pop();
    }

    public static void drawGradientRect(DrawContext graphics, float left, float top, float right, float bottom, Color startColor, Color endColor) {
        new GradientRectRenderState(
            graphics.getMatrices().peek().getPositionMatrix(),
            left,
            top,
            right,
            bottom,
            startColor,
            endColor
        ).render(graphics.getVertexConsumers());
    }

    public static void breadcrumbArrow(DrawContext graphics, int x, int y, int width, int height, int indent, Couple<Color> colors) {
        breadcrumbArrow(graphics, x, y, width, height, indent, colors.getFirst(), colors.getSecond());
    }

    // draws a wide chevron-style breadcrumb arrow pointing left
    public static void breadcrumbArrow(DrawContext graphics, int x, int y, int width, int height, int indent, Color startColor, Color endColor) {
        MatrixStack poseStack = graphics.getMatrices();
        poseStack.push();
        poseStack.translate(x - indent, y, 0);

        breadcrumbArrow(graphics, width, height, indent, startColor, endColor);

        poseStack.pop();
    }

    private static void breadcrumbArrow(DrawContext graphics, int width, int height, int indent, Color c1, Color c2) {

        /*
         * 0,0       x1,y0 ********************* x2,y0 ***** x3,y0
         *       ****                                     ****
         *   ****                                     ****
         * x0,y1     x1,y1                       x2,y1
         *   ****                                     ****
         *       ****                                     ****
         *           x1,y2 ********************* x2,y2 ***** x3,y2
         *
         */

        float x0 = 0;
        float x1 = indent;
        float x2 = width;
        float x3 = indent + width;

        float y0 = 0;
        float y1 = height / 2f;
        float y2 = height;

        indent = Math.abs(indent);
        width = Math.abs(width);
        Color fc1 = Color.mixColors(c1, c2, 0);
        Color fc2 = Color.mixColors(c1, c2, (indent) / (width + 2f * indent));
        Color fc3 = Color.mixColors(c1, c2, (indent + width) / (width + 2f * indent));
        Color fc4 = Color.mixColors(c1, c2, 1);

        new BreadcrumbArrowRenderState(
            graphics.getMatrices().peek().getPositionMatrix(),
            x0,
            x1,
            x2,
            x3,
            y0,
            y1,
            y2,
            fc1,
            fc2,
            fc3,
            fc4,
            indent + width,
            height
        ).render(graphics.getVertexConsumers());
    }

    /**
     * centered on 0, 0
     *
     * @param arcAngle length of the sector arc
     */
    public static void drawRadialSector(
        DrawContext graphics,
        float innerRadius,
        float outerRadius,
        float startAngle,
        float arcAngle,
        Color innerColor,
        Color outerColor
    ) {
        List<Vec2f> innerPoints = getPointsForCircleArc(innerRadius, startAngle, arcAngle);
        List<Vec2f> outerPoints = getPointsForCircleArc(outerRadius, startAngle, arcAngle);
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (Vec2f point : innerPoints) {
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }
        for (Vec2f point : outerPoints) {
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }

        new RadialSectorRenderState(
            graphics.getMatrices().peek().getPositionMatrix(),
            minX,
            maxX,
            minY,
            maxY,
            innerPoints,
            outerPoints,
            innerColor,
            outerColor
        ).render(graphics.getVertexConsumers());
    }

    private static List<Vec2f> getPointsForCircleArc(float radius, float startAngle, float arcAngle) {
        int segmentCount = Math.abs(arcAngle) <= 90 ? 16 : 32;
        List<Vec2f> points = new ArrayList<>(segmentCount);


        float theta = (MathHelper.RADIANS_PER_DEGREE * arcAngle) / (float) (segmentCount - 1);
        float t = MathHelper.RADIANS_PER_DEGREE * startAngle;

        for (int i = 0; i < segmentCount; i++) {
            points.add(new Vec2f((float) (radius * Math.cos(t)), (float) (radius * Math.sin(t))));

            t += theta;
        }

        return points;
    }


    //just like AbstractGui#drawTexture, but with a color at every vertex
    public static void drawColoredTexture(
        DrawContext graphics,
        Identifier texture,
        Color c,
        int x,
        int y,
        int tex_left,
        int tex_top,
        int width,
        int height
    ) {
        drawColoredTexture(graphics, texture, c, x, y, (float) tex_left, (float) tex_top, width, height, 256, 256);
    }

    public static void drawColoredTexture(
        DrawContext graphics,
        Identifier texture,
        Color c,
        int x,
        int y,
        float tex_left,
        float tex_top,
        int width,
        int height,
        int sheet_width,
        int sheet_height
    ) {
        drawColoredTexture(graphics, texture, c, x, x + width, y, y + height, width, height, tex_left, tex_top, sheet_width, sheet_height);
    }

    public static void drawStretched(DrawContext graphics, int left, int top, int w, int h, TextureSheetSegment tex) {
        drawTexturedQuad(
            graphics,
            tex.bind(),
            Color.WHITE,
            left,
            left + w,
            top,
            top + h,
            tex.getStartX() / 256f,
            (tex.getStartX() + tex.getWidth()) / 256f,
            tex.getStartY() / 256f,
            (tex.getStartY() + tex.getHeight()) / 256f
        );
    }

    public static void drawCropped(DrawContext graphics, int left, int top, int w, int h, TextureSheetSegment tex) {
        drawTexturedQuad(
            graphics,
            tex.bind(),
            Color.WHITE,
            left,
            left + w,
            top,
            top + h,
            tex.getStartX() / 256f,
            (tex.getStartX() + w) / 256f,
            tex.getStartY() / 256f,
            (tex.getStartY() + h) / 256f
        );
    }

    private static void drawColoredTexture(
        DrawContext graphics,
        Identifier texture,
        Color c,
        int left,
        int right,
        int top,
        int bot,
        int tex_width,
        int tex_height,
        float tex_left,
        float tex_top,
        int sheet_width,
        int sheet_height
    ) {
        drawTexturedQuad(
            graphics,
            texture,
            c,
            left,
            right,
            top,
            bot,
            (tex_left + 0.0F) / (float) sheet_width,
            (tex_left + (float) tex_width) / (float) sheet_width,
            (tex_top + 0.0F) / (float) sheet_height,
            (tex_top + (float) tex_height) / (float) sheet_height
        );
    }

    private static void drawTexturedQuad(
        DrawContext graphics,
        Identifier texture,
        Color c,
        int left,
        int right,
        int top,
        int bot,
        float u1,
        float u2,
        float v1,
        float v2
    ) {
        new TexturedQuadRenderState(
            graphics.getMatrices().peek().getPositionMatrix(),
            texture,
            left,
            right,
            top,
            bot,
            c,
            u1,
            u2,
            v1,
            v2
        ).render(graphics.getVertexConsumers());
    }

    public static void flipForGuiRender(MatrixStack poseStack) {
        poseStack.multiplyPositionMatrix(new Matrix4f().scaling(1, -1, 1));
    }

    public static class CustomRenderTarget extends Framebuffer {
        public CustomRenderTarget(boolean useDepth) {
            super(useDepth);
        }

        public static CustomRenderTarget create(Window mainWindow) {
            CustomRenderTarget framebuffer = new CustomRenderTarget(true);
            framebuffer.resize(mainWindow.getWidth(), mainWindow.getHeight(), false);
            return framebuffer;
        }
    }

}
