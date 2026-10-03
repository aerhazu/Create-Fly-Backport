package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.Codec;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * REI 16.x's {@code DisplaySerializer} serializes displays via plain {@link NbtCompound} save/read rather than
 * the Codec/PacketCodec pair this codebase's displays (written against a later REI version) were built around.
 * These helpers bridge that: most display fields already have a Mojang {@link Codec} that can be driven through
 * {@link NbtOps}; {@link EntryIngredient} is REI's own type and uses its own NBT save/read instead.
 */
public class DisplayNbt {
    public static <T> void put(NbtCompound tag, String key, Codec<T> codec, T value) {
        codec.encodeStart(NbtOps.INSTANCE, value).result().ifPresent(element -> tag.put(key, element));
    }

    public static <T> T get(NbtCompound tag, String key, Codec<T> codec) {
        return codec.parse(NbtOps.INSTANCE, tag.get(key)).result().orElseThrow();
    }

    public static <T> void putOptional(NbtCompound tag, String key, Codec<T> codec, Optional<T> value) {
        value.ifPresent(v -> put(tag, key, codec, v));
    }

    public static <T> Optional<T> getOptional(NbtCompound tag, String key, Codec<T> codec) {
        return tag.contains(key) ? Optional.of(get(tag, key, codec)) : Optional.empty();
    }

    public static void putLocation(NbtCompound tag, Optional<Identifier> location) {
        location.ifPresent(id -> tag.putString("location", id.toString()));
    }

    public static Optional<Identifier> getLocation(NbtCompound tag) {
        return tag.contains("location") ? Optional.of(Identifier.of(tag.getString("location"))) : Optional.empty();
    }

    public static void putIngredient(NbtCompound tag, String key, EntryIngredient ingredient) {
        tag.put(key, ingredient.save());
    }

    public static EntryIngredient getIngredient(NbtCompound tag, String key) {
        return EntryIngredient.read((NbtList) tag.get(key));
    }

    public static void putIngredientList(NbtCompound tag, String key, List<EntryIngredient> ingredients) {
        NbtList list = new NbtList();
        for (EntryIngredient ingredient : ingredients)
            list.add(ingredient.save());
        tag.put(key, list);
    }

    public static List<EntryIngredient> getIngredientList(NbtCompound tag, String key) {
        NbtList list = (NbtList) tag.get(key);
        return list.stream().map(element -> EntryIngredient.read((NbtList) element)).toList();
    }

    public static <T extends NbtElement> T get(NbtCompound tag, String key) {
        //noinspection unchecked
        return (T) tag.get(key);
    }
}
