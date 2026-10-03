package com.zurrtum.create.foundation.storage;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Backport shim injected onto {@code World} (see loom:injected_interfaces in fabric.mod.json) to restore the
 * {@code BlockPos}-based overload of {@code isPosLoaded} that Mojang added after 1.21.1.
 */
public interface WorldExtensions {
    private World self() {
        return (World) (Object) this;
    }

    default boolean isPosLoaded(BlockPos pos) {
        return self().isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4);
    }

    /**
     * 1.21.1's HeightLimitView only has the exclusive {@code getTopY()}; later versions added these
     * inclusive-top convenience methods that this codebase (written against a later version) expects.
     */
    default int getTopYInclusive() {
        World self = self();
        return self.getBottomY() + self.getHeight() - 1;
    }

    default boolean isInHeightLimit(int y) {
        return y >= self().getBottomY() && y <= getTopYInclusive();
    }
}
