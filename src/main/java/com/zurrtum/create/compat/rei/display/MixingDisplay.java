package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.zurrtum.create.compat.rei.IngredientHelper.*;

public record MixingDisplay(
    List<EntryIngredient> inputs, List<ProcessingOutput> results, List<EntryIngredient> fluidResults,
    HeatCondition heat, Optional<Identifier> location
) implements Display {
    public static final DisplaySerializer<MixingDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public NbtCompound save(NbtCompound tag, MixingDisplay display) {
            DisplayNbt.putIngredientList(tag, "inputs", display.inputs());
            DisplayNbt.put(tag, "results", ProcessingOutput.CODEC.listOf(), display.results());
            DisplayNbt.putIngredientList(tag, "fluid_results", display.fluidResults());
            DisplayNbt.put(tag, "heat", HeatCondition.CODEC, display.heat());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public MixingDisplay read(NbtCompound tag) {
            return new MixingDisplay(
                DisplayNbt.getIngredientList(tag, "inputs"),
                DisplayNbt.get(tag, "results", ProcessingOutput.CODEC.listOf()),
                DisplayNbt.getIngredientList(tag, "fluid_results"),
                DisplayNbt.get(tag, "heat", HeatCondition.CODEC),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public MixingDisplay(RecipeEntry<MixingRecipe> entry) {
        this(entry.id(), entry.value());
    }

    public MixingDisplay(Identifier id, MixingRecipe recipe) {
        this(
            getEntryIngredients(getSizedIngredientStream(recipe.ingredients()), getFluidIngredientStream(recipe.fluidIngredients())),
            recipe.results(),
            getFluidIngredientList(recipe.fluidResults()),
            recipe.heat(),
            Optional.of(id)
        );
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        List<EntryIngredient> list = new ArrayList<>();
        for (ProcessingOutput output : results) {
            list.add(EntryIngredients.of(output.create()));
        }
        list.addAll(fluidResults);
        return list;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.MIXING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
