package com.zurrtum.create.foundation.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Backport shim for {@code net.minecraft.storage.NbtReadView}, introduced after 1.21.1.
 */
public class NbtReadView implements ReadView {
    private final ErrorReporter reporter;
    private final RegistryWrapper.WrapperLookup registries;
    private final DynamicOps<NbtElement> ops;
    private final NbtCompound nbt;

    private NbtReadView(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries, DynamicOps<NbtElement> ops, NbtCompound nbt) {
        this.reporter = reporter;
        this.registries = registries;
        this.ops = ops;
        this.nbt = nbt;
    }

    public static ReadView create(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries, NbtCompound nbt) {
        return new NbtReadView(reporter, registries, registries == null ? NbtOps.INSTANCE : registries.getOps(NbtOps.INSTANCE), nbt);
    }

    /**
     * Backport-only overload for call sites that only have a raw {@link DynamicOps} (e.g. decoded from a
     * network codec) rather than a {@link RegistryWrapper.WrapperLookup}. Registry-aware sub-codecs will not
     * resolve registry references in this mode.
     */
    public static ReadView create(ErrorReporter reporter, DynamicOps<NbtElement> ops, NbtCompound nbt) {
        return new NbtReadView(reporter, null, ops, nbt);
    }

    public static ReadView.ListReadView createList(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries, List<NbtCompound> list) {
        DynamicOps<NbtElement> ops = registries == null ? NbtOps.INSTANCE : registries.getOps(NbtOps.INSTANCE);
        List<ReadView> views = list.stream().map(c -> (ReadView) new NbtReadView(reporter, registries, ops, c)).toList();
        return listReadViewOf(views);
    }

    private static ReadView.ListReadView listReadViewOf(List<ReadView> views) {
        return new ReadView.ListReadView() {
            @Override
            public boolean isEmpty() {
                return views.isEmpty();
            }

            @Override
            public Stream<ReadView> stream() {
                return views.stream();
            }

            @Override
            public java.util.Iterator<ReadView> iterator() {
                return views.iterator();
            }
        };
    }

    @Override
    public <T> Optional<T> read(String key, Codec<T> codec) {
        if (!nbt.contains(key))
            return Optional.empty();
        return codec.parse(ops, nbt.get(key)).resultOrPartial(msg -> reporter.report(key + ": " + msg));
    }

    @Override
    public <T> Optional<T> read(MapCodec<T> codec) {
        return codec.codec().parse(ops, nbt).resultOrPartial(reporter::report);
    }

    @Override
    public Optional<ReadView> getOptionalReadView(String key) {
        NbtElement element = nbt.get(key);
        if (!(element instanceof NbtCompound compound))
            return Optional.empty();
        return Optional.of(new NbtReadView(reporter, registries, ops, compound));
    }

    @Override
    public ReadView getReadView(String key) {
        NbtElement element = nbt.get(key);
        NbtCompound compound = element instanceof NbtCompound c ? c : new NbtCompound();
        return new NbtReadView(reporter, registries, ops, compound);
    }

    private NbtList rawList(String key) {
        NbtElement element = nbt.get(key);
        return element instanceof NbtList list ? list : new NbtList();
    }

    @Override
    public Optional<ReadView.ListReadView> getOptionalListReadView(String key) {
        if (!(nbt.get(key) instanceof NbtList))
            return Optional.empty();
        return Optional.of(getListReadView(key));
    }

    @Override
    public ReadView.ListReadView getListReadView(String key) {
        NbtList list = rawList(key);
        List<ReadView> views = list.stream()
            .map(e -> (ReadView) new NbtReadView(reporter, registries, ops, e instanceof NbtCompound c ? c : new NbtCompound()))
            .toList();
        return listReadViewOf(views);
    }

    @Override
    public <T> Optional<ReadView.TypedListReadView<T>> getOptionalTypedListView(String key, Codec<T> codec) {
        if (!(nbt.get(key) instanceof NbtList))
            return Optional.empty();
        return Optional.of(getTypedListView(key, codec));
    }

    @Override
    public <T> ReadView.TypedListReadView<T> getTypedListView(String key, Codec<T> codec) {
        NbtList list = rawList(key);
        List<T> values = list.stream()
            .map(e -> codec.parse(ops, e).resultOrPartial(msg -> reporter.report(key + ": " + msg)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
        return new ReadView.TypedListReadView<>() {
            @Override
            public boolean isEmpty() {
                return values.isEmpty();
            }

            @Override
            public Stream<T> stream() {
                return values.stream();
            }

            @Override
            public java.util.Iterator<T> iterator() {
                return values.iterator();
            }
        };
    }

    @Override
    public boolean getBoolean(String key, boolean fallback) {
        return nbt.contains(key) ? nbt.getBoolean(key) : fallback;
    }

    @Override
    public byte getByte(String key, byte fallback) {
        return nbt.contains(key) ? (byte) nbt.getInt(key) : fallback;
    }

    @Override
    public int getShort(String key, short fallback) {
        return nbt.contains(key) ? nbt.getInt(key) : fallback;
    }

    @Override
    public Optional<Integer> getOptionalInt(String key) {
        return nbt.contains(key) ? Optional.of(nbt.getInt(key)) : Optional.empty();
    }

    @Override
    public int getInt(String key, int fallback) {
        return nbt.contains(key) ? nbt.getInt(key) : fallback;
    }

    @Override
    public long getLong(String key, long fallback) {
        return nbt.contains(key) ? nbt.getLong(key) : fallback;
    }

    @Override
    public Optional<Long> getOptionalLong(String key) {
        return nbt.contains(key) ? Optional.of(nbt.getLong(key)) : Optional.empty();
    }

    @Override
    public float getFloat(String key, float fallback) {
        return nbt.contains(key) ? nbt.getFloat(key) : fallback;
    }

    @Override
    public double getDouble(String key, double fallback) {
        return nbt.contains(key) ? nbt.getDouble(key) : fallback;
    }

    @Override
    public Optional<String> getOptionalString(String key) {
        return nbt.contains(key) ? Optional.of(nbt.getString(key)) : Optional.empty();
    }

    @Override
    public String getString(String key, String fallback) {
        return nbt.contains(key) ? nbt.getString(key) : fallback;
    }

    @Override
    public Optional<int[]> getOptionalIntArray(String key) {
        return nbt.contains(key) ? Optional.of(nbt.getIntArray(key)) : Optional.empty();
    }

    @Override
    public RegistryWrapper.WrapperLookup getRegistries() {
        return registries;
    }

    @Override
    public NbtCompound getNbt() {
        return nbt;
    }
}
