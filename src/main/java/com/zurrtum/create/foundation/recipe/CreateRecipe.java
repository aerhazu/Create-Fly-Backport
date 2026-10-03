package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.infrastructure.component.SequencedAssemblyJunk;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;

public interface CreateRecipe<T extends RecipeInput> extends Recipe<T> {
    @Override
    default boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    default boolean fits(int width, int height) {
        return true;
    }

    @Override
    default ItemStack getResult(RegistryWrapper.WrapperLookup registries) {
        return ItemStack.EMPTY;
    }

    @Nullable
    static ItemStack getJunk(ItemStack stack) {
        SequencedAssemblyJunk junk = stack.get(AllDataComponents.SEQUENCED_ASSEMBLY_JUNK);
        if (junk != null && junk.hasJunk()) {
            return junk.getJunk();
        }
        return null;
    }
}
