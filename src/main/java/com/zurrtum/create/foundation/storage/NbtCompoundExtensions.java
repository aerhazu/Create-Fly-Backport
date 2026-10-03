package com.zurrtum.create.foundation.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;

import java.util.Optional;

/**
 * Backport shim injected onto {@code NbtCompound} (see loom:injected_interfaces in fabric.mod.json) to restore
 * the default-value and Codec-based convenience overloads that Mojang added to NbtCompound after 1.21.1. This
 * codebase was originally written against that later API and calls these overloads throughout.
 */
public interface NbtCompoundExtensions {
    private NbtCompound self() {
        return (NbtCompound) (Object) this;
    }

    default byte getByte(String key, byte fallback) {
        return self().contains(key) ? self().getByte(key) : fallback;
    }

    default short getShort(String key, short fallback) {
        return self().contains(key) ? self().getShort(key) : fallback;
    }

    default int getInt(String key, int fallback) {
        return self().contains(key) ? self().getInt(key) : fallback;
    }

    default long getLong(String key, long fallback) {
        return self().contains(key) ? self().getLong(key) : fallback;
    }

    default float getFloat(String key, float fallback) {
        return self().contains(key) ? self().getFloat(key) : fallback;
    }

    default double getDouble(String key, double fallback) {
        return self().contains(key) ? self().getDouble(key) : fallback;
    }

    default String getString(String key, String fallback) {
        return self().contains(key) ? self().getString(key) : fallback;
    }

    default boolean getBoolean(String key, boolean fallback) {
        return self().contains(key) ? self().getBoolean(key) : fallback;
    }

    default NbtCompound getCompoundOrEmpty(String key) {
        return self().getCompound(key);
    }

    default Optional<NbtList> getList(String key) {
        NbtElement element = self().get(key);
        return element instanceof NbtList list ? Optional.of(list) : Optional.empty();
    }

    default NbtList getListOrEmpty(String key) {
        NbtElement element = self().get(key);
        return element instanceof NbtList list ? list : new NbtList();
    }

    default <T> void put(String key, Codec<T> codec, T value) {
        put(key, codec, NbtOps.INSTANCE, value);
    }

    default <T> void put(String key, Codec<T> codec, DynamicOps<NbtElement> ops, T value) {
        codec.encodeStart(ops, value).result().ifPresent(element -> self().put(key, element));
    }

    default <T> void putNullable(String key, Codec<T> codec, T value) {
        if (value != null)
            put(key, codec, value);
    }

    default <T> void putNullable(String key, Codec<T> codec, DynamicOps<NbtElement> ops, T value) {
        if (value != null)
            put(key, codec, ops, value);
    }

    default <T> Optional<T> get(String key, Codec<T> codec) {
        return get(key, codec, NbtOps.INSTANCE);
    }

    default <T> Optional<T> get(String key, Codec<T> codec, DynamicOps<NbtElement> ops) {
        NbtElement element = self().get(key);
        return element == null ? Optional.empty() : codec.parse(ops, element).result();
    }
}
