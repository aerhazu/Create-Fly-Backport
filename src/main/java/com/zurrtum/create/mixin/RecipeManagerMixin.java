package com.zurrtum.create.mixin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Shadow
    private Multimap<RecipeType<?>, RecipeEntry<?>> recipesByType;
    @Shadow
    private Map<Identifier, RecipeEntry<?>> recipesById;

    @Inject(
        method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
        at = @At("RETURN")
    )
    private void addSequencedAssemblyRecipe(
        Map<Identifier, JsonElement> prepared,
        ResourceManager resourceManager,
        Profiler profiler,
        CallbackInfo ci
    ) {
        Map<Identifier, Recipe<?>> generated = new HashMap<>(SequencedAssemblyRecipe.Serializer.GENERATE_RECIPES);
        PotionRecipe.register(generated);
        if (generated.isEmpty()) {
            return;
        }

        // 1.21.1's RecipeManager.apply builds recipesById/recipesByType via ImmutableMap/ImmutableMultimap
        // builders, so they must be rebuilt as mutable copies here rather than mutated in place.
        Map<Identifier, RecipeEntry<?>> newRecipesById = new HashMap<>(recipesById);
        ArrayListMultimap<RecipeType<?>, RecipeEntry<?>> newRecipesByType = ArrayListMultimap.create(recipesByType);
        for (Map.Entry<Identifier, Recipe<?>> entry : generated.entrySet()) {
            RecipeEntry<?> recipeEntry = new RecipeEntry<>(entry.getKey(), entry.getValue());
            newRecipesById.put(entry.getKey(), recipeEntry);
            newRecipesByType.put(entry.getValue().getType(), recipeEntry);
        }
        recipesById = newRecipesById;
        recipesByType = newRecipesByType;
    }
}
