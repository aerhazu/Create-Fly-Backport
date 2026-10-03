package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.zurrtum.create.Create.MOD_ID;

public record SpoutFillingDisplay(
    EntryIngredient input, EntryIngredient fluid, EntryIngredient output, Optional<Identifier> location
) implements Display {
    public static final Identifier POTIONS = Identifier.of(MOD_ID, "potions");
    public static final DisplaySerializer<SpoutFillingDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public net.minecraft.nbt.NbtCompound save(net.minecraft.nbt.NbtCompound tag, SpoutFillingDisplay display) {
            DisplayNbt.putIngredient(tag, "input", display.input());
            DisplayNbt.putIngredient(tag, "fluid", display.fluid());
            DisplayNbt.putIngredient(tag, "output", display.output());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public SpoutFillingDisplay read(net.minecraft.nbt.NbtCompound tag) {
            return new SpoutFillingDisplay(
                DisplayNbt.getIngredient(tag, "input"),
                DisplayNbt.getIngredient(tag, "fluid"),
                DisplayNbt.getIngredient(tag, "output"),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public SpoutFillingDisplay(RecipeEntry<FillingRecipe> entry) {
        this(entry.id(), entry.value());
    }

    public SpoutFillingDisplay(Identifier id, FillingRecipe recipe) {
        this(
            EntryIngredients.ofIngredient(recipe.ingredient()),
            IngredientHelper.createEntryIngredient(recipe.fluidIngredient()),
            EntryIngredients.of(recipe.result()),
            Optional.of(id)
        );
    }

    public static void register(Stream<EntryStack<?>> itemStream, Stream<EntryStack<?>> fluidStream, DisplayRegistry registry) {
        List<FluidStack> fluids = fluidStream.map(entry -> {
            dev.architectury.fluid.FluidStack stack = entry.castValue();
            return new FluidStack(stack.getFluid(), stack.getAmount(), stack.getComponents().getChanges());
        }).toList();
        itemStream.forEach(entry -> {
            ItemStack stack = entry.castValue();
            if (PotionFluidHandler.isPotionItem(stack)) {
                registry.add(new SpoutFillingDisplay(
                    EntryIngredients.of(Items.GLASS_BOTTLE),
                    IngredientHelper.createEntryIngredient(PotionFluidHandler.getFluidFromPotionItem(stack)),
                    EntryIngredients.of(stack),
                    Optional.of(POTIONS)
                ));
                return;
            }
            try (FluidItemInventory capability = FluidHelper.getFluidInventory(stack.copy())) {
                if (capability == null) {
                    return;
                }
                int size = capability.size();
                FluidStack existingFluid = size == 1 ? capability.getStack(0) : FluidStack.EMPTY;
                for (FluidStack fluid : fluids) {
                    if (size == 1 && !existingFluid.isEmpty() && !FluidStack.areFluidsAndComponentsEqual(existingFluid, fluid)) {
                        continue;
                    }
                    int insert = capability.insert(fluid, 81000);
                    if (insert == 0) {
                        continue;
                    }
                    ItemStack result = capability.getContainer();
                    if (!result.isEmpty()) {
                        Item item = stack.getItem();
                        if (!result.isOf(item)) {
                            Identifier itemName = Registries.ITEM.getId(item);
                            Identifier fluidName = Registries.FLUID.getId(fluid.getFluid());
                            Identifier id = Identifier.of(
                                MOD_ID,
                                "fill_" + itemName.getNamespace() + "_" + itemName.getPath() + "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()
                            );
                            registry.add(new SpoutFillingDisplay(
                                EntryIngredients.of(stack),
                                EntryIngredients.of(dev.architectury.fluid.FluidStack.create(fluid.getFluid(), insert, fluid.getComponentChanges())),
                                EntryIngredients.of(result),
                                Optional.of(id)
                            ));
                        }
                    }
                    capability.extract(fluid, insert);
                }
            }
        });
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input, fluid);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(output);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.SPOUT_FILLING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
