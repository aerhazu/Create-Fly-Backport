package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;
import com.zurrtum.create.client.flywheel.lib.model.SimpleQuadMesh;
import com.zurrtum.create.client.flywheel.lib.vertex.FullVertexView;
import com.zurrtum.create.client.mixin.ModelPartAccessor;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public class ItemMeshEmitter implements VertexConsumer {
    private final RenderLayer renderType;
    private final BufferAllocator byteBufferBuilder;
    @UnknownNullability
    private BufferBuilder bufferBuilder;

    private BakedItemModelBufferer.ResultConsumer resultConsumer;
    private MeshResultConsumer meshResultConsumer;
    private boolean currentShade;
    private boolean ended = true;

    ItemMeshEmitter(RenderLayer renderType) {
        this.renderType = renderType;
        this.byteBufferBuilder = new BufferAllocator(renderType.getExpectedBufferSize());
    }

    public void prepare(BakedItemModelBufferer.ResultConsumer resultConsumer, MeshResultConsumer meshResultConsumer) {
        this.resultConsumer = resultConsumer;
        this.meshResultConsumer = meshResultConsumer;
        ended = false;
    }

    public boolean isEnd() {
        return ended;
    }

    public void end() {
        if (ended) {
            return;
        }
        if (bufferBuilder != null) {
            emit();
        }
        resultConsumer = null;
        meshResultConsumer = null;
        ended = true;
    }

    public BufferBuilder unwrap(boolean shade) {
        prepareForGeometry(shade);
        return bufferBuilder;
    }

    private void prepareForGeometry(boolean shade) {
        if (bufferBuilder == null) {
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        } else if (shade != currentShade) {
            emit();
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        }

        currentShade = shade;
    }

    private void prepareForGeometry(BakedQuad quad) {
        prepareForGeometry(quad.hasShade());
    }

    private void emit() {
        var data = bufferBuilder.endNullable();
        bufferBuilder = null;

        if (data != null) {
            resultConsumer.accept(renderType, currentShade, data);
            data.close();
        }
    }

    public void emit(ModelPart part, MatrixStack stack, Sprite meshSprite, ItemMeshEmitter glintEmitter, int light, int overlay, int color) {
        stack.push();
        part.rotate(stack);
        if (!part.isEmpty()) {
            float alpha = ((color >>> 24) & 0xFF) / 255.0F;
            boolean translucent = alpha < 1.0F;
            Mesh mesh = compile(part, stack, meshSprite, light, overlay, color, alpha);
            meshResultConsumer.accept(renderType, mesh, translucent);
            if (glintEmitter != null) {
                glintEmitter.meshResultConsumer.accept(glintEmitter.renderType, mesh, translucent);
            }
        }
        for (ModelPart child : ((ModelPartAccessor) (Object) part).getChildren().values()) {
            emit(child, stack, meshSprite, glintEmitter, light, overlay, color);
        }
        stack.pop();
    }

    private Mesh compile(ModelPart part, MatrixStack stack, Sprite meshSprite, int light, int overlay, int color, float alpha) {
        float red = (color & 0xFF) / 255.0F;
        float green = ((color >>> 8) & 0xFF) / 255.0F;
        float blue = ((color >>> 16) & 0xFF) / 255.0F;

        MatrixStack.Entry entry = stack.peek();
        CuboidVertexCollector collector = new CuboidVertexCollector(meshSprite, red, green, blue, alpha, light, overlay);
        for (ModelPart.Cuboid cuboid : ((ModelPartAccessor) (Object) part).getCuboids()) {
            cuboid.renderCuboid(entry, collector, light, overlay, color);
        }

        int vertexCount = collector.size();
        MemoryBlock memoryBlock = MemoryBlock.mallocTracked(vertexCount * FullVertexView.STRIDE);
        FullVertexView meshVertices = new FullVertexView();

        meshVertices.nativeMemoryOwner(memoryBlock);
        meshVertices.ptr(memoryBlock.ptr());
        meshVertices.vertexCount(vertexCount);

        for (int index = 0; index < vertexCount; index++) {
            meshVertices.x(index, collector.xs.getFloat(index));
            meshVertices.y(index, collector.ys.getFloat(index));
            meshVertices.z(index, collector.zs.getFloat(index));
            meshVertices.r(index, collector.rs.getFloat(index));
            meshVertices.g(index, collector.gs.getFloat(index));
            meshVertices.b(index, collector.bs.getFloat(index));
            meshVertices.a(index, collector.as.getFloat(index));
            meshVertices.u(index, collector.us.getFloat(index));
            meshVertices.v(index, collector.vs.getFloat(index));
            meshVertices.overlay(index, collector.overlays.getInt(index));
            meshVertices.light(index, collector.lights.getInt(index));
            meshVertices.normalX(index, collector.nxs.getFloat(index));
            meshVertices.normalY(index, collector.nys.getFloat(index));
            meshVertices.normalZ(index, collector.nzs.getFloat(index));
        }

        return new SimpleQuadMesh(meshVertices, "source=ItemMeshEmitter");
    }

    /**
     * Captures the low-level {@link VertexConsumer} calls issued by {@link ModelPart.Cuboid#renderCuboid}
     * (the only public way to read a cuboid's geometry in 1.21.1, since {@code Cuboid.sides} and the
     * {@code Quad}/{@code Vertex} nested classes are package-private) into flat arrays for mesh building.
     */
    private static final class CuboidVertexCollector implements VertexConsumer {
        final FloatArrayList xs = new FloatArrayList();
        final FloatArrayList ys = new FloatArrayList();
        final FloatArrayList zs = new FloatArrayList();
        final FloatArrayList rs = new FloatArrayList();
        final FloatArrayList gs = new FloatArrayList();
        final FloatArrayList bs = new FloatArrayList();
        final FloatArrayList as = new FloatArrayList();
        final FloatArrayList us = new FloatArrayList();
        final FloatArrayList vs = new FloatArrayList();
        final IntArrayList overlays = new IntArrayList();
        final IntArrayList lights = new IntArrayList();
        final FloatArrayList nxs = new FloatArrayList();
        final FloatArrayList nys = new FloatArrayList();
        final FloatArrayList nzs = new FloatArrayList();

        @Nullable
        private final Sprite sprite;
        private final float fallbackR;
        private final float fallbackG;
        private final float fallbackB;
        private final float fallbackA;
        private final int fallbackLight;
        private final int fallbackOverlay;

        CuboidVertexCollector(@Nullable Sprite sprite, float r, float g, float b, float a, int light, int overlay) {
            this.sprite = sprite;
            this.fallbackR = r;
            this.fallbackG = g;
            this.fallbackB = b;
            this.fallbackA = a;
            this.fallbackLight = light;
            this.fallbackOverlay = overlay;
        }

        int size() {
            return xs.size();
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            xs.add(x);
            ys.add(y);
            zs.add(z);
            rs.add(fallbackR);
            gs.add(fallbackG);
            bs.add(fallbackB);
            as.add(fallbackA);
            us.add(0);
            vs.add(0);
            overlays.add(fallbackOverlay);
            lights.add(fallbackLight);
            nxs.add(0);
            nys.add(0);
            nzs.add(0);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            int i = rs.size() - 1;
            rs.set(i, red / 255.0F);
            gs.set(i, green / 255.0F);
            bs.set(i, blue / 255.0F);
            as.set(i, alpha / 255.0F);
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            int i = us.size() - 1;
            us.set(i, sprite != null ? sprite.getFrameU(u) : u);
            vs.set(i, sprite != null ? sprite.getFrameV(v) : v);
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            overlays.set(overlays.size() - 1, OverlayTexture.packUv(u, v));
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            lights.set(lights.size() - 1, LightmapTextureManager.pack(u, v));
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            int i = nxs.size() - 1;
            nxs.set(i, x);
            nys.set(i, y);
            nzs.set(i, z);
            return this;
        }
    }

    public void quad(
        MatrixStack.Entry pose,
        BakedQuad quad,
        float red,
        float green,
        float blue,
        float alpha,
        int light,
        int overlay,
        boolean readExistingColor
    ) {
        prepareForGeometry(quad);
        bufferBuilder.quad(
            pose,
            quad,
            new float[]{1.0F, 1.0F, 1.0F, 1.0F},
            red,
            green,
            blue,
            alpha,
            new int[]{light, light, light, light},
            overlay,
            readExistingColor
        );
    }

    @Override
    public void quad(MatrixStack.Entry pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
        prepareForGeometry(quad);
        bufferBuilder.quad(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
    }

    @Override
    public void quad(
        MatrixStack.Entry pose,
        BakedQuad quad,
        float[] brightnesses,
        float red,
        float green,
        float blue,
        float alpha,
        int[] lights,
        int overlay,
        boolean readExistingColor
    ) {
        prepareForGeometry(quad);
        bufferBuilder.quad(pose, quad, brightnesses, red, green, blue, alpha, lights, overlay, readExistingColor);
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer light(int u, int v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer normal(float normalX, float normalY, float normalZ) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    public interface MeshResultConsumer {
        void accept(RenderLayer renderType, Mesh mesh, boolean translucent);
    }
}
