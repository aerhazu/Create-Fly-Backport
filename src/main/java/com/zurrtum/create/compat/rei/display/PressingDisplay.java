package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import net.minecraft.nbt.NbtCompound;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record PressingDisplay(EntryIngredient input, List<ProcessingOutput> outputs,
                              Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<PressingDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public NbtCompound save(NbtCompound tag, PressingDisplay display) {
            DisplayNbt.putIngredient(tag, "input", display.input());
            DisplayNbt.put(tag, "outputs", ProcessingOutput.CODEC.listOf(), display.outputs());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public PressingDisplay read(NbtCompound tag) {
            return new PressingDisplay(
                DisplayNbt.getIngredient(tag, "input"),
                DisplayNbt.get(tag, "outputs", ProcessingOutput.CODEC.listOf()),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public PressingDisplay(RecipeEntry<PressingRecipe> entry) {
        this(entry.id(), entry.value());
    }

    public PressingDisplay(Identifier id, PressingRecipe recipe) {
        this(IngredientHelper.getInputEntryIngredient(recipe.ingredient()), recipe.results(), Optional.of(id));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        List<EntryIngredient> list = new ArrayList<>();
        for (ProcessingOutput output : outputs) {
            list.add(EntryIngredients.of(output.create()));
        }
        return list;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.PRESSING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
