package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
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

public record ManualApplicationDisplay(
    EntryIngredient input, EntryIngredient target, List<ProcessingOutput> outputs, boolean keepHeldItem,
    Optional<Identifier> location
) implements Display {
    public static final DisplaySerializer<ManualApplicationDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public NbtCompound save(NbtCompound tag, ManualApplicationDisplay display) {
            DisplayNbt.putIngredient(tag, "input", display.input());
            DisplayNbt.putIngredient(tag, "target", display.target());
            DisplayNbt.put(tag, "outputs", ProcessingOutput.CODEC.listOf(), display.outputs());
            tag.putBoolean("keep_held_item", display.keepHeldItem());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public ManualApplicationDisplay read(NbtCompound tag) {
            return new ManualApplicationDisplay(
                DisplayNbt.getIngredient(tag, "input"),
                DisplayNbt.getIngredient(tag, "target"),
                DisplayNbt.get(tag, "outputs", ProcessingOutput.CODEC.listOf()),
                tag.getBoolean("keep_held_item"),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public ManualApplicationDisplay(RecipeEntry<ManualApplicationRecipe> entry) {
        this(entry.id(), entry.value());
    }

    public ManualApplicationDisplay(Identifier id, ManualApplicationRecipe recipe) {
        this(
            EntryIngredients.ofIngredient(recipe.ingredient()),
            EntryIngredients.ofIngredient(recipe.target()),
            recipe.results(),
            recipe.keepHeldItem(),
            Optional.of(id)
        );
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input, target);
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
        return ReiCommonPlugin.ITEM_APPLICATION;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
