package com.zurrtum.create.client.ponder.foundation;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4fStack;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

public class PonderWorldParticles {

    private final Map<ParticleTextureSheet, Queue<Particle>> byType = Maps.newIdentityHashMap();
    private final Queue<Particle> queue = Queues.newArrayDeque();

    PonderLevel world;

    public PonderWorldParticles(PonderLevel world) {
        this.world = world;
    }

    public void addParticle(Particle p) {
        this.queue.add(p);
    }

    public void tick() {
        this.byType.forEach((p_228347_1_, p_228347_2_) -> this.tickParticleList(p_228347_2_));

        Particle particle;
        if (queue.isEmpty())
            return;
        while ((particle = this.queue.poll()) != null)
            this.byType.computeIfAbsent(particle.getType(), $ -> EvictingQueue.create(16384)).add(particle);
    }

    private void tickParticleList(Collection<Particle> p_187240_1_) {
        if (p_187240_1_.isEmpty())
            return;

        Iterator<Particle> iterator = p_187240_1_.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.tick();
            if (!particle.isAlive())
                iterator.remove();
        }
    }

    public void renderParticles(MatrixStack ms, SuperRenderTypeBuffer buffer, Camera renderInfo, float pt) {
        buffer.draw();

        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.mul(ms.peek().getPositionMatrix());

        MinecraftClient mc = MinecraftClient.getInstance();
        Tessellator tessellator = Tessellator.getInstance();
        for (ParticleTextureSheet sheet : this.byType.keySet()) {
            if (sheet == ParticleTextureSheet.NO_RENDER)
                continue;
            Queue<Particle> particles = this.byType.get(sheet);
            if (particles == null || particles.isEmpty())
                continue;
            RenderSystem.setShader(GameRenderer::getParticleProgram);
            BufferBuilder bufferBuilder = sheet.begin(tessellator, mc.getTextureManager());
            if (bufferBuilder != null) {
                for (Particle particle : particles)
                    particle.buildGeometry(bufferBuilder, renderInfo, pt);
                BuiltBuffer built = bufferBuilder.endNullable();
                if (built != null) {
                    BufferRenderer.drawWithGlobalProgram(built);
                }
            }
        }

        buffer.draw();
        stack.popMatrix();
    }

    public void clearEffects() {
        this.byType.clear();
    }

}