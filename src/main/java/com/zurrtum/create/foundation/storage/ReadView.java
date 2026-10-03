package com.zurrtum.create.foundation.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Backport shim for {@code net.minecraft.storage.ReadView}, introduced after 1.21.1.
 * Mirrors the method surface of the later API so downstream code is unchanged; backed by {@link NbtReadView}.
 */
public interface ReadView {
    <T> Optional<T> read(String key, Codec<T> codec);

    <T> Optional<T> read(MapCodec<T> codec);

    Optional<ReadView> getOptionalReadView(String key);

    ReadView getReadView(String key);

    Optional<ListReadView> getOptionalListReadView(String key);

    ListReadView getListReadView(String key);

    <T> Optional<TypedListReadView<T>> getOptionalTypedListView(String key, Codec<T> codec);

    <T> TypedListReadView<T> getTypedListView(String key, Codec<T> codec);

    boolean getBoolean(String key, boolean fallback);

    byte getByte(String key, byte fallback);

    int getShort(String key, short fallback);

    Optional<Integer> getOptionalInt(String key);

    int getInt(String key, int fallback);

    long getLong(String key, long fallback);

    Optional<Long> getOptionalLong(String key);

    float getFloat(String key, float fallback);

    double getDouble(String key, double fallback);

    Optional<String> getOptionalString(String key);

    String getString(String key, String fallback);

    Optional<int[]> getOptionalIntArray(String key);

    RegistryWrapper.WrapperLookup getRegistries();

    NbtCompound getNbt();

    interface ListReadView extends Iterable<ReadView> {
        boolean isEmpty();

        Stream<ReadView> stream();
    }

    interface TypedListReadView<T> extends Iterable<T> {
        boolean isEmpty();

        Stream<T> stream();
    }
}
