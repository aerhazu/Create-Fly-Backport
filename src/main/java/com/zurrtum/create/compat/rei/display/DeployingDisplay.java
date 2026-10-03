package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record DeployingDisplay(
    EntryIngredient input, EntryIngredient target, List<ProcessingOutput> outputs, boolean keepHeldItem,
    Optional<Identifier> location
) implements Display {
    public static final DisplaySerializer<DeployingDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public NbtCompound save(NbtCompound tag, DeployingDisplay display) {
            DisplayNbt.putIngredient(tag, "input", display.input());
            DisplayNbt.putIngredient(tag, "target", display.target());
            DisplayNbt.put(tag, "outputs", ProcessingOutput.CODEC.listOf(), display.outputs());
            tag.putBoolean("keep_held_item", display.keepHeldItem());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public DeployingDisplay read(NbtCompound tag) {
            return new DeployingDisplay(
                DisplayNbt.getIngredient(tag, "input"),
                DisplayNbt.getIngredient(tag, "target"),
                DisplayNbt.get(tag, "outputs", ProcessingOutput.CODEC.listOf()),
                tag.getBoolean("keep_held_item"),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public static DeployingDisplay of(RecipeEntry<?> entry) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(entry)) {
            return null;
        }
        Identifier id = entry.id();
        Recipe<?> recipe = entry.value();
        if (recipe instanceof ItemApplicationRecipe itemApplicationRecipe) {
            return new DeployingDisplay(id, itemApplicationRecipe);
        } else if (recipe instanceof SandPaperPolishingRecipe sandPaperPolishingRecipe) {
            return new DeployingDisplay(id, sandPaperPolishingRecipe);
        }
        return null;
    }

    public DeployingDisplay(Identifier id, ItemApplicationRecipe recipe) {
        this(
            EntryIngredients.ofIngredient(recipe.ingredient()),
            IngredientHelper.getInputEntryIngredient(recipe.target()),
            recipe.results(),
            recipe.keepHeldItem(),
            Optional.of(id)
        );
    }

    public DeployingDisplay(Identifier id, SandPaperPolishingRecipe recipe) {
        this(
            EntryIngredients.ofItemTag(AllItemTags.SANDPAPER),
            EntryIngredients.ofIngredient(recipe.ingredient()),
            List.of(new ProcessingOutput(recipe.result())),
            false,
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
        return ReiCommonPlugin.DEPLOYING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
