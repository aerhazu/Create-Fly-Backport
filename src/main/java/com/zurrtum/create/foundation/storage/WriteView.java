package com.zurrtum.create.foundation.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/**
 * Backport shim for {@code net.minecraft.storage.WriteView}, introduced after 1.21.1.
 * Mirrors the method surface of the later API so downstream code is unchanged; backed by {@link NbtWriteView}.
 */
public interface WriteView {
    <T> void put(String key, Codec<T> codec, T value);

    <T> void putNullable(String key, Codec<T> codec, T value);

    <T> void put(MapCodec<T> codec, T value);

    void putBoolean(String key, boolean value);

    void putByte(String key, byte value);

    void putShort(String key, short value);

    void putInt(String key, int value);

    void putLong(String key, long value);

    void putFloat(String key, float value);

    void putDouble(String key, double value);

    void putString(String key, String value);

    void putIntArray(String key, int[] value);

    WriteView get(String key);

    ListView getList(String key);

    <T> ListAppender<T> getListAppender(String key, Codec<T> codec);

    void remove(String key);

    boolean isEmpty();

    interface ListView {
        WriteView add();

        void removeLast();

        boolean isEmpty();
    }

    interface ListAppender<T> {
        void add(T value);

        boolean isEmpty();
    }
}
