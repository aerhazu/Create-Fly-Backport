package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.List;
import java.util.Optional;

public record MechanicalCraftingDisplay(
    int width, int height, List<Optional<Ingredient>> inputs, EntryIngredient output, Optional<Identifier> location
) implements Display {
    private static final com.mojang.serialization.Codec<List<Optional<Ingredient>>> INPUTS_CODEC = Codecs.optional(Ingredient.DISALLOW_EMPTY_CODEC)
        .listOf();

    public static final DisplaySerializer<MechanicalCraftingDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public NbtCompound save(NbtCompound tag, MechanicalCraftingDisplay display) {
            tag.putInt("width", display.width());
            tag.putInt("height", display.height());
            DisplayNbt.put(tag, "inputs", INPUTS_CODEC, display.inputs());
            DisplayNbt.putIngredient(tag, "output", display.output());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public MechanicalCraftingDisplay read(NbtCompound tag) {
            return new MechanicalCraftingDisplay(
                tag.getInt("width"),
                tag.getInt("height"),
                DisplayNbt.get(tag, "inputs", INPUTS_CODEC),
                DisplayNbt.getIngredient(tag, "output"),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public MechanicalCraftingDisplay(RecipeEntry<MechanicalCraftingRecipe> entry) {
        this(entry.id(), entry.value());
    }

    public MechanicalCraftingDisplay(Identifier id, MechanicalCraftingRecipe recipe) {
        this(
            recipe.raw().getWidth(),
            recipe.raw().getHeight(),
            recipe.raw().getIngredients().stream().map(i -> i.isEmpty() ? Optional.<Ingredient>empty() : Optional.of(i)).toList(),
            EntryIngredients.of(recipe.result()),
            Optional.of(id)
        );
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs.stream().filter(Optional::isPresent).map(Optional::get).map(EntryIngredients::ofIngredient).toList();
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(output);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.MECHANICAL_CRAFTING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
