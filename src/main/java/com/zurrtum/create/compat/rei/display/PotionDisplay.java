package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
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

public record PotionDisplay(EntryIngredient input, FluidIngredient fluid, FluidStack output, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<PotionDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public NbtCompound save(NbtCompound tag, PotionDisplay display) {
            DisplayNbt.putIngredient(tag, "input", display.input());
            DisplayNbt.put(tag, "fluid", FluidIngredient.CODEC, display.fluid());
            DisplayNbt.put(tag, "output", FluidStack.CODEC, display.output());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public PotionDisplay read(NbtCompound tag) {
            return new PotionDisplay(
                DisplayNbt.getIngredient(tag, "input"),
                DisplayNbt.get(tag, "fluid", FluidIngredient.CODEC),
                DisplayNbt.get(tag, "output", FluidStack.CODEC),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public PotionDisplay(RecipeEntry<PotionRecipe> entry) {
        this(entry.id(), entry.value());
    }

    public PotionDisplay(Identifier id, PotionRecipe recipe) {
        this(EntryIngredients.ofIngredient(recipe.ingredient()), recipe.fluidIngredient(), recipe.result(), Optional.of(id));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input, IngredientHelper.createEntryIngredient(fluid));
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(IngredientHelper.createEntryIngredient(output));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.AUTOMATIC_BREWING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
