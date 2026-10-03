package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
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

import static com.zurrtum.create.compat.rei.IngredientHelper.getEntryIngredients;
import static com.zurrtum.create.compat.rei.IngredientHelper.getFluidIngredientStream;

public record CompactingDisplay(List<EntryIngredient> inputs, List<ProcessingOutput> outputs, HeatCondition heat,
                                Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<CompactingDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public NbtCompound save(NbtCompound tag, CompactingDisplay display) {
            DisplayNbt.putIngredientList(tag, "inputs", display.inputs());
            DisplayNbt.put(tag, "outputs", ProcessingOutput.CODEC.listOf(), display.outputs());
            DisplayNbt.put(tag, "heat", HeatCondition.CODEC, display.heat());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public CompactingDisplay read(NbtCompound tag) {
            return new CompactingDisplay(
                DisplayNbt.getIngredientList(tag, "inputs"),
                DisplayNbt.get(tag, "outputs", ProcessingOutput.CODEC.listOf()),
                DisplayNbt.get(tag, "heat", HeatCondition.CODEC),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public CompactingDisplay(RecipeEntry<CompactingRecipe> entry) {
        this(entry.id(), entry.value());
    }

    public CompactingDisplay(Identifier id, CompactingRecipe recipe) {
        this(
            getEntryIngredients(IngredientHelper.getSizedIngredientStream(recipe.ingredients()), getFluidIngredientStream(recipe.fluidIngredients())),
            recipe.results(),
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
        for (ProcessingOutput output : outputs) {
            list.add(EntryIngredients.of(output.create()));
        }
        return list;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.PACKING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
