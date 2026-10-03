package com.zurrtum.create;

import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.crusher.CrushingRecipe;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.HauntingRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.SplashingRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;

import java.util.function.Function;

public class AllRecipeSets {
    public static final RecipeSet<ManualApplicationRecipe> ITEM_APPLICATION_TARGET = new RecipeSet<>(
        AllRecipeTypes.ITEM_APPLICATION,
        ManualApplicationRecipe::target
    );
    public static final RecipeSet<ManualApplicationRecipe> ITEM_APPLICATION_INGREDIENT = new RecipeSet<>(
        AllRecipeTypes.ITEM_APPLICATION,
        ManualApplicationRecipe::ingredient
    );
    public static final RecipeSet<EmptyingRecipe> EMPTYING = new RecipeSet<>(AllRecipeTypes.EMPTYING, EmptyingRecipe::ingredient);
    public static final RecipeSet<FillingRecipe> FILLING = new RecipeSet<>(AllRecipeTypes.FILLING, FillingRecipe::ingredient);
    public static final RecipeSet<SandPaperPolishingRecipe> SAND_PAPER_POLISHING = new RecipeSet<>(
        AllRecipeTypes.SANDPAPER_POLISHING,
        SandPaperPolishingRecipe::ingredient
    );
    public static final RecipeSet<SplashingRecipe> SPLASHING = new RecipeSet<>(AllRecipeTypes.SPLASHING, SplashingRecipe::ingredient);
    public static final RecipeSet<HauntingRecipe> HAUNTING = new RecipeSet<>(AllRecipeTypes.HAUNTING, HauntingRecipe::ingredient);
    public static final RecipeSet<CrushingRecipe> CRUSHING = new RecipeSet<>(AllRecipeTypes.CRUSHING, CrushingRecipe::ingredient);
    public static final RecipeSet<MillingRecipe> MILLING = new RecipeSet<>(AllRecipeTypes.MILLING, MillingRecipe::ingredient);

    public static void register() {
    }

    // Stands in for 1.21.2+'s RecipePropertySet: 1.21.1 has no fast precomputed ingredient
    // lookup, so this just searches the matching RecipeType directly.
    public static class RecipeSet<T extends Recipe<?>> {
        @SuppressWarnings("rawtypes")
        private final RecipeType type;
        private final Function<T, Ingredient> ingredientGetter;

        public RecipeSet(RecipeType<T> type, Function<T, Ingredient> ingredientGetter) {
            this.type = type;
            this.ingredientGetter = ingredientGetter;
        }

        @SuppressWarnings("unchecked")
        public boolean canUse(RecipeManager manager, ItemStack stack) {
            for (RecipeEntry<?> entry : (java.util.List<RecipeEntry<?>>) (java.util.List) manager.listAllOfType(type)) {
                if (ingredientGetter.apply((T) entry.value()).test(stack))
                    return true;
            }
            return false;
        }
    }
}
