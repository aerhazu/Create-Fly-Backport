package com.zurrtum.create.compat.rei;

import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public interface IngredientHelper {
    static EntryIngredient createEntryIngredient(com.zurrtum.create.infrastructure.fluids.FluidStack stack) {
        return EntryIngredients.of(FluidStack.create(stack.getFluid(), stack.getAmount(), stack.getComponentChanges()));
    }

    static EntryIngredient createEntryIngredient(FluidIngredient ingredient) {
        EntryDefinition<FluidStack> definition = VanillaEntryTypes.FLUID.getDefinition();
        List<Fluid> fluids = ingredient.getMatchingFluids();
        EntryIngredient.Builder builder = EntryIngredient.builder(fluids.size());
        int amount = ingredient.amount();
        ComponentChanges patch = ComponentChanges.EMPTY;
        if (ingredient instanceof FluidStackIngredient stackIngredient) {
            patch = stackIngredient.components();
        }
        for (Fluid fluid : fluids) {
            FluidStack stack = FluidStack.create(fluid, amount, patch);
            builder.add(EntryStack.of(definition, stack));
        }
        return builder.build();
    }

    static List<EntryIngredient> getFluidIngredientList(List<com.zurrtum.create.infrastructure.fluids.FluidStack> stacks) {
        return stacks.stream().map(IngredientHelper::createEntryIngredient).toList();
    }

    static Stream<EntryIngredient> getFluidIngredientStream(@Nullable FluidIngredient ingredient) {
        return ingredient == null ? Stream.empty() : Stream.of(createEntryIngredient(ingredient));
    }

    static Stream<EntryIngredient> getFluidIngredientStream(List<FluidIngredient> ingredients) {
        if (ingredients.isEmpty()) {
            return Stream.empty();
        }
        Stream.Builder<EntryIngredient> builder = Stream.builder();
        for (FluidIngredient ingredient : ingredients) {
            builder.add(createEntryIngredient(ingredient));
        }
        return builder.build();
    }

    static Stream<EntryIngredient> getSizedIngredientStream(List<SizedIngredient> ingredients) {
        Stream.Builder<EntryIngredient> results = Stream.builder();
        EntryDefinition<ItemStack> definition = VanillaEntryTypes.ITEM.getDefinition();
        int size = ingredients.size();
        for (SizedIngredient ingredient : ingredients) {
            EntryIngredient.Builder builder = EntryIngredient.builder(size);
            for (ItemStack stack : ingredient.getIngredient().getMatchingStacks()) {
                ItemStack copy = stack.copy();
                copy.setCount(ingredient.getCount());
                builder.add(EntryStack.of(definition, copy));
            }
            results.add(builder.build());
        }
        return results.build();
    }

    static List<EntryIngredient> getEntryIngredients(Stream<EntryIngredient> first, Stream<EntryIngredient> second) {
        return Stream.concat(first, second).toList();
    }

    static EntryIngredient getInputEntryIngredient(Ingredient ingredient) {
        return EntryIngredients.ofIngredient(ingredient);
    }
}
