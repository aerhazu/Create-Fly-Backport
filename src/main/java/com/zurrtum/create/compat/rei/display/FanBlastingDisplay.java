package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public record FanBlastingDisplay(EntryIngredient input, EntryIngredient output, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<FanBlastingDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public net.minecraft.nbt.NbtCompound save(net.minecraft.nbt.NbtCompound tag, FanBlastingDisplay display) {
            DisplayNbt.putIngredient(tag, "input", display.input());
            DisplayNbt.putIngredient(tag, "output", display.output());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public FanBlastingDisplay read(net.minecraft.nbt.NbtCompound tag) {
            return new FanBlastingDisplay(
                DisplayNbt.getIngredient(tag, "input"),
                DisplayNbt.getIngredient(tag, "output"),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public static Display of(RecipeEntry<?> entry) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(entry)) {
            return null;
        }
        AbstractCookingRecipe recipe = (AbstractCookingRecipe) entry.value();
        Ingredient ingredient = recipe.getIngredients().getFirst();
        ItemStack[] matchingStacks = ingredient.getMatchingStacks();
        Optional<ItemStack> firstInput = matchingStacks.length > 0 ? Optional.of(matchingStacks[0]) : Optional.empty();
        if (firstInput.isEmpty()) {
            return null;
        }
        SingleStackRecipeInput input = new SingleStackRecipeInput(firstInput.get());
        MinecraftServer server = Create.SERVER;
        ServerWorld world = server.getWorld(World.OVERWORLD);
        RecipeManager recipeManager = server.getRecipeManager();
        if (recipe instanceof SmeltingRecipe) {
            Optional<RecipeEntry<BlastingRecipe>> blastingRecipe = recipeManager.getFirstMatch(RecipeType.BLASTING, input, world)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
            if (blastingRecipe.isPresent()) {
                return null;
            }
        }
        Optional<RecipeEntry<SmokingRecipe>> smokingRecipe = recipeManager.getFirstMatch(RecipeType.SMOKING, input, world)
            .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
        if (smokingRecipe.isPresent()) {
            return null;
        }
        return new FanBlastingDisplay(
            EntryIngredients.ofIngredient(ingredient),
            EntryIngredients.of(recipe.getResult(server.getRegistryManager())),
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
        return ReiCommonPlugin.FAN_BLASTING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
