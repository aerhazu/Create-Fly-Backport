package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.crusher.CrushingRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import net.minecraft.nbt.NbtCompound;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record CrushingDisplay(EntryIngredient input, List<ProcessingOutput> outputs,
                              Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<CrushingDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public NbtCompound save(NbtCompound tag, CrushingDisplay display) {
            DisplayNbt.putIngredient(tag, "input", display.input());
            DisplayNbt.put(tag, "outputs", ProcessingOutput.CODEC.listOf(), display.outputs());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public CrushingDisplay read(NbtCompound tag) {
            return new CrushingDisplay(
                DisplayNbt.getIngredient(tag, "input"),
                DisplayNbt.get(tag, "outputs", ProcessingOutput.CODEC.listOf()),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public static CrushingDisplay of(RecipeEntry<?> entry) {
        Identifier id = entry.id();
        Recipe<?> recipe = entry.value();
        if (recipe instanceof CrushingRecipe crushingRecipe) {
            return new CrushingDisplay(id, crushingRecipe);
        } else if (recipe instanceof MillingRecipe millingRecipe) {
            return new CrushingDisplay(id, millingRecipe);
        }
        return null;
    }

    public CrushingDisplay(Identifier id, CrushingRecipe recipe) {
        this(EntryIngredients.ofIngredient(recipe.ingredient()), recipe.results(), Optional.of(id));
    }

    public CrushingDisplay(Identifier id, MillingRecipe recipe) {
        this(EntryIngredients.ofIngredient(recipe.ingredient()), recipe.results(), Optional.of(id));
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
        return ReiCommonPlugin.CRUSHING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
