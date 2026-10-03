package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.lib.material.Materials;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import com.zurrtum.create.client.mixin.RenderLayerMultiPhaseAccessor;
import com.zurrtum.create.client.mixin.RenderLayerMultiPhaseParametersAccessor;
import com.zurrtum.create.client.mixin.RenderPhaseTextureBaseAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.BlockRenderView;

import java.util.*;

public class BakedItemModelBufferer {
    static final List<RenderLayer> CHUNK_LAYERS = List.of(
        TexturedRenderLayers.getEntitySolid(),
        TexturedRenderLayers.getEntityCutout(),
        TexturedRenderLayers.getItemEntityTranslucentCull(),
        RenderLayer.getGlint(),
        RenderLayer.getGlintTranslucent(),
        RenderLayer.getEntityGlint()
    );

    public static void bufferItemStack(
        ItemStack stack,
        BlockRenderView level,
        ModelTransformationMode displayContext,
        ResultConsumer resultConsumer,
        MeshResultConsumer meshResultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        MatrixStack poseStack = objects.identityPoseStack;
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModels().getModel(stack);
        ItemMeshEmitterProvider provider = objects.provider;
        provider.setResultConsumer(resultConsumer, meshResultConsumer);
        itemRenderer.renderItem(stack, displayContext, false, poseStack, provider, 0, OverlayTexture.DEFAULT_UV, model);
        provider.end();
    }

    public static class ItemMeshEmitterProvider implements VertexConsumerProvider {
        private final ThreadLocalObjects objects;
        private ResultConsumer resultConsumer;
        private MeshResultConsumer meshResultConsumer;

        private ItemMeshEmitterProvider(ThreadLocalObjects objects) {
            this.objects = objects;
        }

        public void setResultConsumer(ResultConsumer resultConsumer, MeshResultConsumer meshResultConsumer) {
            this.resultConsumer = resultConsumer;
            this.meshResultConsumer = meshResultConsumer;
        }

        private void emitMesh(RenderLayer renderType, Mesh mesh, boolean translucent) {
            Material material = objects.materials.computeIfAbsent(renderType, ItemMeshEmitterProvider::createMaterial);
            meshResultConsumer.accept(renderType, material, mesh, translucent);
        }

        private static Material createMaterial(RenderLayer renderLayer) {
            if (!(renderLayer instanceof RenderLayer.MultiPhase)) {
                return Materials.TRANSLUCENT_ENTITY;
            }
            RenderLayer.MultiPhaseParameters phases = ((RenderLayerMultiPhaseAccessor) (Object) renderLayer).getPhases();
            RenderPhase.TextureBase texture = ((RenderLayerMultiPhaseParametersAccessor) (Object) phases).getTexture();
            Optional<Identifier> id = ((RenderPhaseTextureBaseAccessor) (Object) texture).invokeGetId();
            if (id.isPresent()) {
                return SimpleMaterial.builder().texture(id.get()).mipmap(false).build();
            }
            return Materials.TRANSLUCENT_ENTITY;
        }

        @Override
        public VertexConsumer getBuffer(RenderLayer layer) {
            Integer index = objects.chunkLayers.get(layer);
            ItemMeshEmitter emitter;
            if (index == null) {
                objects.chunkLayers.put(layer, objects.chunkLayers.size());
                emitter = new ItemMeshEmitter(layer);
                emitter.prepare(resultConsumer, this::emitMesh);
                objects.emitters.add(emitter);
            } else {
                emitter = objects.emitters.get(index);
                if (emitter.isEnd()) {
                    emitter.prepare(resultConsumer, this::emitMesh);
                }
            }
            return emitter;
        }

        public void end() {
            for (ItemMeshEmitter emitter : objects.emitters) {
                emitter.end();
            }
        }
    }

    public interface ResultConsumer {
        void accept(RenderLayer renderType, boolean shaded, BuiltBuffer data);
    }

    public interface MeshResultConsumer {
        void accept(RenderLayer renderType, Material material, Mesh mesh, boolean translucent);
    }

    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    public static Map<RenderLayer, Integer> getChunkLayers() {
        return THREAD_LOCAL_OBJECTS.get().chunkLayers;
    }

    private static class ThreadLocalObjects {
        public final MatrixStack identityPoseStack = new MatrixStack();
        public final ItemMeshEmitterProvider provider = new ItemMeshEmitterProvider(this);
        public final Map<RenderLayer, Material> materials = new HashMap<>();
        public final Map<RenderLayer, Integer> chunkLayers = new HashMap<>();
        public final List<ItemMeshEmitter> emitters = new ArrayList<>();

        {
            for (int i = 0, size = CHUNK_LAYERS.size(); i < size; i++) {
                RenderLayer renderType = CHUNK_LAYERS.get(i);
                chunkLayers.put(renderType, i);
                emitters.add(new ItemMeshEmitter(renderType));
            }
        }
    }
}
