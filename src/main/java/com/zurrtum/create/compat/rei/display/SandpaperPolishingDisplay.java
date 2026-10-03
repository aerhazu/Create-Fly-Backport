package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public record SandpaperPolishingDisplay(EntryIngredient input, EntryIngredient output, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<SandpaperPolishingDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public NbtCompound save(NbtCompound tag, SandpaperPolishingDisplay display) {
            DisplayNbt.putIngredient(tag, "input", display.input());
            DisplayNbt.putIngredient(tag, "output", display.output());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public SandpaperPolishingDisplay read(NbtCompound tag) {
            return new SandpaperPolishingDisplay(
                DisplayNbt.getIngredient(tag, "input"),
                DisplayNbt.getIngredient(tag, "output"),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public SandpaperPolishingDisplay(RecipeEntry<SandPaperPolishingRecipe> entry) {
        this(entry.id(), entry.value());
    }

    public SandpaperPolishingDisplay(Identifier id, SandPaperPolishingRecipe recipe) {
        this(EntryIngredients.ofIngredient(recipe.ingredient()), EntryIngredients.of(recipe.result()), Optional.of(id));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(output);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.SANDPAPER_POLISHING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
