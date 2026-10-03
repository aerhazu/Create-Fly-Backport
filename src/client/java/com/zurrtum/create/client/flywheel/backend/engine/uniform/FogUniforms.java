package com.zurrtum.create.client.flywheel.backend.engine.uniform;

import org.joml.Vector4f;

public final class FogUniforms extends UniformWriter {
    private static final int SIZE = 4 * 8;
    static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.FOG_INDEX, SIZE);
    static final float[] CACHE = new float[8];

    public static void update(
        Vector4f fogColor,
        float environmentalStart,
        float environmentalEnd,
        float renderDistanceStart,
        float renderDistanceEnd
    ) {
        CACHE[0] = fogColor.x;
        CACHE[1] = fogColor.y;
        CACHE[2] = fogColor.z;
        CACHE[3] = fogColor.w;
        CACHE[4] = environmentalStart;
        CACHE[5] = environmentalEnd;
        CACHE[6] = renderDistanceStart;
        CACHE[7] = renderDistanceEnd;

        long ptr = BUFFER.ptr();
        for (float v : CACHE) {
            ptr = writeFloat(ptr, v);
        }
        BUFFER.markDirty();
    }

}
