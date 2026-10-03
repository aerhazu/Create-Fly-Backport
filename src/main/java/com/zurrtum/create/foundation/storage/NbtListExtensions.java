package com.zurrtum.create.foundation.storage;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

/**
 * Backport shim injected onto {@code NbtList} (see loom:injected_interfaces in fabric.mod.json) to restore the
 * default-value convenience overloads that Mojang added to NbtList after 1.21.1.
 */
public interface NbtListExtensions {
    private NbtList self() {
        return (NbtList) (Object) this;
    }

    default short getShort(int index, short fallback) {
        return index >= 0 && index < self().size() ? self().getShort(index) : fallback;
    }

    default int getInt(int index, int fallback) {
        return index >= 0 && index < self().size() ? self().getInt(index) : fallback;
    }

    default float getFloat(int index, float fallback) {
        return index >= 0 && index < self().size() ? self().getFloat(index) : fallback;
    }

    default double getDouble(int index, double fallback) {
        return index >= 0 && index < self().size() ? self().getDouble(index) : fallback;
    }

    default String getString(int index, String fallback) {
        return index >= 0 && index < self().size() ? self().getString(index) : fallback;
    }

    default NbtCompound getCompoundOrEmpty(int index) {
        return self().getCompound(index);
    }

    default NbtList getListOrEmpty(int index) {
        return self().getList(index);
    }
}
