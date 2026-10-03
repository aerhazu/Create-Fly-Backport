package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapedDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapelessDisplay;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * REI 16.x has no equivalent of the newer Minecraft recipe display system (net.minecraft.recipe.display),
 * which doesn't exist in 1.21.1 either, so this only handles the plain ShapelessRecipe/ShapedRecipe cases.
 */
public interface AutoCompactingDisplay {
    @SuppressWarnings("unchecked")
    static Display of(RecipeEntry<?> entry) {
        Recipe<?> recipe = entry.value();
        if (!MechanicalPressBlockEntity.canCompress(recipe) || AllRecipeTypes.shouldIgnoreInAutomation(entry)) {
            return null;
        }
        if (recipe instanceof ShapelessRecipe) {
            return new ShapelessDisplay((RecipeEntry<ShapelessRecipe>) entry);
        } else if (recipe instanceof ShapedRecipe) {
            return new ShapedDisplay((RecipeEntry<ShapedRecipe>) entry);
        }
        return null;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    class ShapelessDisplay extends DefaultCustomShapelessDisplay implements AutoCompactingDisplay {
        public static final DisplaySerializer<ShapelessDisplay> SERIALIZER = new DisplaySerializer<>() {
            @Override
            public NbtCompound save(NbtCompound tag, ShapelessDisplay display) {
                DisplayNbt.putIngredientList(tag, "inputs", display.getInputEntries());
                DisplayNbt.putIngredientList(tag, "outputs", display.getOutputEntries());
                DisplayNbt.putLocation(tag, display.getDisplayLocation());
                return tag;
            }

            @Override
            public ShapelessDisplay read(NbtCompound tag) {
                return new ShapelessDisplay(
                    DisplayNbt.getIngredientList(tag, "inputs"),
                    DisplayNbt.getIngredientList(tag, "outputs"),
                    DisplayNbt.getLocation(tag)
                );
            }
        };

        public ShapelessDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<Identifier> location) {
            super(location.orElse(null), null, input, output);
        }

        public ShapelessDisplay(RecipeEntry<ShapelessRecipe> recipe) {
            super(
                recipe.id(),
                recipe,
                CollectionUtils.map(recipe.value().getIngredients(), EntryIngredients::ofIngredient),
                List.of(EntryIngredients.of(recipe.value().result))
            );
        }

        @Override
        public List<InputIngredient<EntryStack<?>>> getInputIngredients(@Nullable ScreenHandler menu, @Nullable PlayerEntity player) {
            return CollectionUtils.mapIndexed(getInputEntries(), InputIngredient::of);
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return ReiCommonPlugin.AUTOMATIC_PACKING;
        }

                public DisplaySerializer<? extends Display> getSerializer() {
            return SERIALIZER;
        }
    }

    class ShapedDisplay extends DefaultCustomShapedDisplay implements AutoCompactingDisplay {
        public static final DisplaySerializer<ShapedDisplay> SERIALIZER = new DisplaySerializer<>() {
            @Override
            public NbtCompound save(NbtCompound tag, ShapedDisplay display) {
                DisplayNbt.putIngredientList(tag, "inputs", display.getInputEntries());
                DisplayNbt.putIngredientList(tag, "outputs", display.getOutputEntries());
                DisplayNbt.putLocation(tag, display.getDisplayLocation());
                tag.putInt("width", display.getWidth());
                tag.putInt("height", display.getHeight());
                return tag;
            }

            @Override
            public ShapedDisplay read(NbtCompound tag) {
                return new ShapedDisplay(
                    DisplayNbt.getIngredientList(tag, "inputs"),
                    DisplayNbt.getIngredientList(tag, "outputs"),
                    DisplayNbt.getLocation(tag),
                    tag.getInt("width"),
                    tag.getInt("height")
                );
            }
        };

        public ShapedDisplay(RecipeEntry<ShapedRecipe> recipe) {
            super(
                recipe.id(),
                recipe,
                CollectionUtils.map(recipe.value().getIngredients(), EntryIngredients::ofIngredient),
                List.of(EntryIngredients.of(recipe.value().result)),
                recipe.value().getWidth(),
                recipe.value().getHeight()
            );
        }

        public ShapedDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<Identifier> location, int width, int height) {
            super(location.orElse(null), null, input, output, width, height);
        }

        @Override
        public List<InputIngredient<EntryStack<?>>> getInputIngredients(@Nullable ScreenHandler menu, @Nullable PlayerEntity player) {
            return CollectionUtils.mapIndexed(getInputEntries(), InputIngredient::of);
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return ReiCommonPlugin.AUTOMATIC_PACKING;
        }

                public DisplaySerializer<? extends Display> getSerializer() {
            return SERIALIZER;
        }
    }
}
