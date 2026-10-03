package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public record FanSmokingDisplay(EntryIngredient input, EntryIngredient output, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<FanSmokingDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public NbtCompound save(NbtCompound tag, FanSmokingDisplay display) {
            DisplayNbt.putIngredient(tag, "input", display.input());
            DisplayNbt.putIngredient(tag, "output", display.output());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public FanSmokingDisplay read(NbtCompound tag) {
            return new FanSmokingDisplay(
                DisplayNbt.getIngredient(tag, "input"),
                DisplayNbt.getIngredient(tag, "output"),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public static Display of(RecipeEntry<SmokingRecipe> entry) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(entry)) {
            return null;
        }
        SmokingRecipe recipe = entry.value();
        return new FanSmokingDisplay(
            EntryIngredients.ofIngredient(recipe.getIngredients().getFirst()),
            EntryIngredients.of(recipe.getResult(Create.SERVER.getRegistryManager())),
            Optional.of(entry.id())
        );
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
        return ReiCommonPlugin.FAN_SMOKING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
