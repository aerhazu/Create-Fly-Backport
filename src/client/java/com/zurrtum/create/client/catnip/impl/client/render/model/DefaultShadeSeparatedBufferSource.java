package com.zurrtum.create.client.catnip.impl.client.render.model;

import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedBufferSource;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedResultConsumer;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;

import java.util.List;

class DefaultShadeSeparatedBufferSource implements ShadeSeparatedBufferSource {
    private static final List<RenderLayer> CHUNK_LAYERS = RenderLayer.getBlockLayers();
    private static final int CHUNK_LAYER_AMOUNT = CHUNK_LAYERS.size();

    private final MeshEmitter[] emitters = new MeshEmitter[CHUNK_LAYER_AMOUNT];
    private final Reference2ReferenceMap<RenderLayer, MeshEmitter> emitterMap = new Reference2ReferenceOpenHashMap<>();

    DefaultShadeSeparatedBufferSource() {
        for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
            RenderLayer renderType = CHUNK_LAYERS.get(layerIndex);
            MeshEmitter emitter = new MeshEmitter(renderType);
            emitters[layerIndex] = emitter;
            emitterMap.put(renderType, emitter);
        }
    }

    public void prepare(ShadeSeparatedResultConsumer resultConsumer) {
        for (MeshEmitter emitter : emitters) {
            emitter.prepare(resultConsumer);
        }
    }

    public void end() {
        for (MeshEmitter emitter : emitters) {
            emitter.end();
        }
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer chunkRenderType, boolean shade) {
        return emitterMap.get(chunkRenderType).getBuffer(shade);
    }
}
