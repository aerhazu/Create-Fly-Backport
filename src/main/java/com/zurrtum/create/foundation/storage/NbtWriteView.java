package com.zurrtum.create.foundation.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;

/**
 * Backport shim for {@code net.minecraft.storage.NbtWriteView}, introduced after 1.21.1.
 */
public class NbtWriteView implements WriteView {
    private final ErrorReporter reporter;
    public final DynamicOps<NbtElement> ops;
    private final NbtCompound nbt;

    public NbtWriteView(ErrorReporter reporter, DynamicOps<NbtElement> ops, NbtCompound nbt) {
        this.reporter = reporter;
        this.ops = ops;
        this.nbt = nbt;
    }

    public static NbtWriteView create(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries) {
        return new NbtWriteView(reporter, registries == null ? NbtOps.INSTANCE : registries.getOps(NbtOps.INSTANCE), new NbtCompound());
    }

    public static NbtWriteView create(ErrorReporter reporter) {
        return new NbtWriteView(reporter, NbtOps.INSTANCE, new NbtCompound());
    }

    /**
     * Backport-only overload that writes directly into an existing compound, for bridging into
     * vanilla callbacks like {@code BlockEntity.writeNbt} that mutate a caller-supplied NbtCompound.
     */
    public static NbtWriteView create(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries, NbtCompound target) {
        return new NbtWriteView(reporter, registries == null ? NbtOps.INSTANCE : registries.getOps(NbtOps.INSTANCE), target);
    }

    @Override
    public <T> void put(String key, Codec<T> codec, T value) {
        codec.encodeStart(ops, value).resultOrPartial(msg -> reporter.report(key + ": " + msg)).ifPresent(element -> nbt.put(key, element));
    }

    @Override
    public <T> void putNullable(String key, Codec<T> codec, T value) {
        if (value != null)
            put(key, codec, value);
    }

    @Override
    public <T> void put(MapCodec<T> codec, T value) {
        codec.codec()
            .encodeStart(ops, value)
            .resultOrPartial(reporter::report)
            .ifPresent(element -> {
                if (element instanceof NbtCompound compound)
                    for (String childKey : compound.getKeys())
                        nbt.put(childKey, compound.get(childKey));
            });
    }

    @Override
    public void putBoolean(String key, boolean value) {
        nbt.putBoolean(key, value);
    }

    @Override
    public void putByte(String key, byte value) {
        nbt.putInt(key, value);
    }

    @Override
    public void putShort(String key, short value) {
        nbt.putInt(key, value);
    }

    @Override
    public void putInt(String key, int value) {
        nbt.putInt(key, value);
    }

    @Override
    public void putLong(String key, long value) {
        nbt.putLong(key, value);
    }

    @Override
    public void putFloat(String key, float value) {
        nbt.putFloat(key, value);
    }

    @Override
    public void putDouble(String key, double value) {
        nbt.putDouble(key, value);
    }

    @Override
    public void putString(String key, String value) {
        nbt.putString(key, value);
    }

    @Override
    public void putIntArray(String key, int[] value) {
        nbt.putIntArray(key, value);
    }

    @Override
    public WriteView get(String key) {
        NbtCompound child = new NbtCompound();
        nbt.put(key, child);
        return new NbtWriteView(reporter, ops, child);
    }

    @Override
    public WriteView.ListView getList(String key) {
        NbtList list = new NbtList();
        nbt.put(key, list);
        return new WriteView.ListView() {
            @Override
            public WriteView add() {
                NbtCompound child = new NbtCompound();
                list.add(child);
                return new NbtWriteView(reporter, ops, child);
            }

            @Override
            public void removeLast() {
                if (!list.isEmpty())
                    list.remove(list.size() - 1);
            }

            @Override
            public boolean isEmpty() {
                return list.isEmpty();
            }
        };
    }

    @Override
    public <T> WriteView.ListAppender<T> getListAppender(String key, Codec<T> codec) {
        NbtList list = new NbtList();
        nbt.put(key, list);
        return new WriteView.ListAppender<>() {
            @Override
            public void add(T value) {
                codec.encodeStart(ops, value).resultOrPartial(msg -> reporter.report(key + ": " + msg)).ifPresent(list::add);
            }

            @Override
            public boolean isEmpty() {
                return list.isEmpty();
            }
        };
    }

    @Override
    public void remove(String key) {
        nbt.remove(key);
    }

    @Override
    public boolean isEmpty() {
        return nbt.isEmpty();
    }

    public NbtCompound getNbt() {
        return nbt;
    }
}
