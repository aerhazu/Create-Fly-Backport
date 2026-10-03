package com.zurrtum.create.compat.rei.display;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record SequencedAssemblyDisplay(
    EntryIngredient input, SequenceData sequences, ProcessingOutput output, int loop, Optional<Identifier> location
) implements Display {
    public static final DisplaySerializer<SequencedAssemblyDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public NbtCompound save(NbtCompound tag, SequencedAssemblyDisplay display) {
            DisplayNbt.putIngredient(tag, "input", display.input());
            tag.put("sequences", SequenceData.save(display.sequences()));
            DisplayNbt.put(tag, "output", ProcessingOutput.CODEC, display.output());
            tag.putInt("loop", display.loop());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public SequencedAssemblyDisplay read(NbtCompound tag) {
            return new SequencedAssemblyDisplay(
                DisplayNbt.getIngredient(tag, "input"),
                SequenceData.read((NbtCompound) tag.get("sequences")),
                DisplayNbt.get(tag, "output", ProcessingOutput.CODEC),
                tag.getInt("loop"),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public SequencedAssemblyDisplay(RecipeEntry<SequencedAssemblyRecipe> entry) {
        this(entry.id(), entry.value());
    }

    public SequencedAssemblyDisplay(Identifier id, SequencedAssemblyRecipe recipe) {
        this(EntryIngredients.ofIngredient(recipe.ingredient()), SequenceData.create(recipe), recipe.result(), recipe.loops(), Optional.of(id));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        List<EntryIngredient> inputs = new ArrayList<>();
        inputs.add(input);
        for (EntryIngredient ingredient : sequences.ingredients) {
            if (ingredient.isEmpty()) {
                continue;
            }
            inputs.add(ingredient);
        }
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(EntryIngredients.of(output.create()));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.SEQUENCED_ASSEMBLY;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }

    public record SequenceData(List<RecipeType<?>> types, List<List<Text>> tooltip, List<EntryIngredient> ingredients) {
        public static NbtCompound save(SequenceData data) {
            NbtCompound tag = new NbtCompound();
            NbtList typesList = new NbtList();
            for (RecipeType<?> type : data.types()) {
                Identifier id = Registries.RECIPE_TYPE.getId(type);
                typesList.add(NbtString.of(id == null ? "" : id.toString()));
            }
            tag.put("types", typesList);
            DisplayNbt.put(tag, "tooltip", TextCodecs.CODEC.listOf().listOf(), data.tooltip());
            DisplayNbt.putIngredientList(tag, "ingredients", data.ingredients());
            return tag;
        }

        public static SequenceData read(NbtCompound tag) {
            List<RecipeType<?>> types = new ArrayList<>();
            for (net.minecraft.nbt.NbtElement element : (NbtList) tag.get("types")) {
                types.add(Registries.RECIPE_TYPE.get(Identifier.of(element.asString())));
            }
            List<List<Text>> tooltip = DisplayNbt.get(tag, "tooltip", TextCodecs.CODEC.listOf().listOf());
            List<EntryIngredient> ingredients = DisplayNbt.getIngredientList(tag, "ingredients");
            return new SequenceData(types, tooltip, ingredients);
        }

        public static SequenceData create(SequencedAssemblyRecipe recipe) {
            ImmutableList.Builder<EntryIngredient> ingredientBuilder = ImmutableList.builder();
            ImmutableList.Builder<List<Text>> textBuilder = ImmutableList.builder();
            ImmutableList.Builder<RecipeType<?>> typeBuilder = ImmutableList.builder();
            List<Recipe<?>> recipes = recipe.sequence();
            for (int i = 0, size = recipes.size() / recipe.loops(); i < size; i++) {
                Recipe<?> sequence = recipes.get(i);
                typeBuilder.add(sequence.getType());
                ImmutableList.Builder<Text> tooltipBuilder = ImmutableList.builder();
                tooltipBuilder.add(Text.translatable("create.recipe.assembly.step", i + 1));
                if (sequence instanceof DeployerApplicationRecipe deployerApplicationRecipe) {
                    tooltipBuilder.add(Text.translatable("create.recipe.assembly.deploying_item", "").formatted(Formatting.DARK_GREEN));
                    ingredientBuilder.add(EntryIngredients.ofIngredient(deployerApplicationRecipe.ingredient()));
                } else if (sequence instanceof FillingRecipe fillingRecipe) {
                    tooltipBuilder.add(Text.translatable("create.recipe.assembly.spout_filling_fluid", "").formatted(Formatting.DARK_GREEN));
                    ingredientBuilder.add(IngredientHelper.createEntryIngredient(fillingRecipe.fluidIngredient()));
                } else {
                    ingredientBuilder.add(EntryIngredient.empty());
                    Identifier id = Registries.RECIPE_TYPE.getId(sequence.getType());
                    if (id != null) {
                        String namespace = id.getNamespace();
                        String recipeName;
                        if (namespace.equals("create")) {
                            recipeName = id.getPath();
                        } else {
                            recipeName = id.getNamespace() + "." + id.getPath();
                        }
                        tooltipBuilder.add(Text.translatable("create.recipe.assembly." + recipeName).formatted(Formatting.DARK_GREEN));
                    } else {
                        tooltipBuilder.add(ScreenTexts.EMPTY);
                    }
                }
                textBuilder.add(tooltipBuilder.build());
            }
            return new SequenceData(typeBuilder.build(), textBuilder.build(), ingredientBuilder.build());
        }
    }
}
