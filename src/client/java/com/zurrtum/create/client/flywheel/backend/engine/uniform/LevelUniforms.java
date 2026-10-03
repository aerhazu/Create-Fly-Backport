package com.zurrtum.create.client.flywheel.backend.engine.uniform;

import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

public final class LevelUniforms extends UniformWriter {
    private static final int SIZE = 16 * 4 + 4 * 12;
    static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.LEVEL_INDEX, SIZE);

    private static final Vector3f LIGHT0 = new Vector3f();
    private static final Vector3f LIGHT1 = new Vector3f();

    private LevelUniforms() {
    }

    public static void updateLights(Vector3f light0Diffusion, Vector3f light1Diffusion) {
        LIGHT0.set(light0Diffusion);
        LIGHT1.set(light1Diffusion);
    }

    public static Vector3f getLight0() {
        return LIGHT0;
    }

    public static Vector3f getLight1() {
        return LIGHT1;
    }

    public static void update(RenderContext context) {
        long ptr = BUFFER.ptr();

        ClientWorld level = context.level();
        float partialTick = context.partialTick();

        Vec3d skyColor = level.getSkyColor(context.camera().getPos(), partialTick);
        Vec3d cloudColor = level.getCloudsColor(partialTick);
        ptr = writeVec4(ptr, (float) skyColor.x, (float) skyColor.y, (float) skyColor.z, 1f);
        ptr = writeVec4(ptr, (float) cloudColor.x, (float) cloudColor.y, (float) cloudColor.z, 1f);

        ptr = writeVec3(ptr, LIGHT0.x, LIGHT0.y, LIGHT0.z);
        ptr = writeVec3(ptr, LIGHT1.x, LIGHT1.y, LIGHT1.z);

        long dayTime = level.getTimeOfDay();
        long levelDay = dayTime / 24000L;
        float timeOfDay = (float) (dayTime - levelDay * 24000L) / 24000f;
        ptr = writeInt(ptr, (int) (levelDay % 0x7FFFFFFFL));
        ptr = writeFloat(ptr, timeOfDay);

        ptr = writeInt(ptr, level.getDimension().hasSkyLight() ? 1 : 0);

        ptr = writeFloat(ptr, level.getSkyAngleRadians(partialTick));

        ptr = writeFloat(ptr, level.getMoonSize());
        ptr = writeInt(ptr, level.getMoonPhase());

        ptr = writeInt(ptr, level.isRaining() ? 1 : 0);
        ptr = writeFloat(ptr, level.getRainGradient(partialTick));
        ptr = writeInt(ptr, level.isThundering() ? 1 : 0);
        ptr = writeFloat(ptr, level.getThunderGradient(partialTick));

        ptr = writeFloat(ptr, level.getSkyBrightness(partialTick));

        ptr = writeInt(ptr, level.getDimensionEffects().isDarkened() ? 1 : 0);

        // TODO: use defines for custom dimension ids
        int dimensionId;
        RegistryKey<World> dimension = level.getRegistryKey();
        if (World.OVERWORLD.equals(dimension)) {
            dimensionId = 0;
        } else if (World.NETHER.equals(dimension)) {
            dimensionId = 1;
        } else if (World.END.equals(dimension)) {
            dimensionId = 2;
        } else {
            dimensionId = -1;
        }
        ptr = writeInt(ptr, dimensionId);

        BUFFER.markDirty();
    }
}
