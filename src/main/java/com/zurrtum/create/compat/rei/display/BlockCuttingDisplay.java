package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;

import java.util.*;

import static com.zurrtum.create.Create.MOD_ID;

public record BlockCuttingDisplay(EntryIngredient input, List<EntryIngredient> outputs, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<BlockCuttingDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public NbtCompound save(NbtCompound tag, BlockCuttingDisplay display) {
            DisplayNbt.putIngredient(tag, "input", display.input());
            DisplayNbt.putIngredientList(tag, "outputs", display.outputs());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public BlockCuttingDisplay read(NbtCompound tag) {
            return new BlockCuttingDisplay(
                DisplayNbt.getIngredient(tag, "input"),
                DisplayNbt.getIngredientList(tag, "outputs"),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public static void register(DisplayRegistry registry) {
        Object2ObjectMap<Ingredient, Pair<EntryIngredient, List<ItemStack>>> map = new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
            public boolean equals(Ingredient ingredient, Ingredient other) {
                return Objects.equals(ingredient, other);
            }

            public int hashCode(Ingredient ingredient) {
                return ingredient.hashCode();
            }
        });
        for (RecipeEntry<StonecuttingRecipe> entry : Create.SERVER.getRecipeManager().listAllOfType(RecipeType.STONECUTTING)) {
            if (AllRecipeTypes.shouldIgnoreInAutomation(entry)) {
                continue;
            }
            StonecuttingRecipe recipe = entry.value();
            Ingredient ingredient = recipe.getIngredients().getFirst();
            Pair<EntryIngredient, List<ItemStack>> display = map.computeIfAbsent(
                ingredient,
                (Ingredient i) -> Pair.of(EntryIngredients.ofIngredient(i), new ArrayList<>())
            );
            display.getSecond().add(recipe.getResult(Create.SERVER.getRegistryManager()));
        }
        for (Pair<EntryIngredient, List<ItemStack>> pair : map.values()) {
            EntryIngredient input = pair.getFirst();
            List<ItemStack> outputs = pair.getSecond();
            Identifier itemName = Registries.ITEM.getId(input.getFirst().<ItemStack>castValue().getItem());
            Optional<Identifier> location = Optional.of(Identifier.of(MOD_ID, "block_cutting/" + itemName.getNamespace() + "_" + itemName.getPath()));
            int size = outputs.size();
            if (size <= 15) {
                List<EntryIngredient> outputIngredients = Arrays.asList(new EntryIngredient[size]);
                for (int i = 0; i < size; i++) {
                    outputIngredients.set(i, EntryIngredients.of(outputs.get(i)));
                }
                registry.add(new BlockCuttingDisplay(input, outputIngredients, location));
            } else {
                EntryIngredient.Builder[] builders = new EntryIngredient.Builder[15];
                for (int i = 0; i < 15; i++) {
                    builders[i] = EntryIngredient.builder().add(EntryStacks.of(outputs.get(i)));
                }
                for (int i = 15; i < size; i++) {
                    builders[i % 15].add(EntryStacks.of(outputs.get(i)));
                }
                List<EntryIngredient> outputIngredients = Arrays.asList(new EntryIngredient[15]);
                for (int i = 0; i < 15; i++) {
                    outputIngredients.set(i, builders[i].build());
                }
                registry.add(new BlockCuttingDisplay(input, outputIngredients, location));
            }
        }
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.BLOCK_CUTTING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
