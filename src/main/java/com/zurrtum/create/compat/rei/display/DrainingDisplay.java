package com.zurrtum.create.compat.rei.display;

import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
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

public record DrainingDisplay(
    EntryIngredient input, EntryIngredient output, EntryIngredient result, Optional<Identifier> location
) implements Display {
    public static final Identifier POTIONS = Identifier.of(MOD_ID, "potions");
    public static final DisplaySerializer<DrainingDisplay> SERIALIZER = new DisplaySerializer<>() {
        @Override
        public net.minecraft.nbt.NbtCompound save(net.minecraft.nbt.NbtCompound tag, DrainingDisplay display) {
            DisplayNbt.putIngredient(tag, "input", display.input());
            DisplayNbt.putIngredient(tag, "output", display.output());
            DisplayNbt.putIngredient(tag, "result", display.result());
            DisplayNbt.putLocation(tag, display.location());
            return tag;
        }

        @Override
        public DrainingDisplay read(net.minecraft.nbt.NbtCompound tag) {
            return new DrainingDisplay(
                DisplayNbt.getIngredient(tag, "input"),
                DisplayNbt.getIngredient(tag, "output"),
                DisplayNbt.getIngredient(tag, "result"),
                DisplayNbt.getLocation(tag)
            );
        }
    };

    public DrainingDisplay(RecipeEntry<EmptyingRecipe> entry) {
        this(entry.id(), entry.value());
    }

    public DrainingDisplay(Identifier id, EmptyingRecipe recipe) {
        this(
            EntryIngredients.ofIngredient(recipe.ingredient()),
            IngredientHelper.createEntryIngredient(recipe.fluidResult()),
            EntryIngredients.of(recipe.result()),
            Optional.of(id)
        );
    }

    public static void register(Stream<EntryStack<?>> itemStream, DisplayRegistry registry) {
        itemStream.forEach(entry -> {
            ItemStack stack = entry.castValue();
            if (PotionFluidHandler.isPotionItem(stack)) {
                registry.add(new DrainingDisplay(
                    EntryIngredients.of(stack),
                    IngredientHelper.createEntryIngredient(PotionFluidHandler.getFluidFromPotionItem(stack)),
                    EntryIngredients.of(Items.GLASS_BOTTLE),
                    Optional.of(POTIONS)
                ));
                return;
            }
            try (FluidItemInventory capability = FluidHelper.getFluidInventory(stack.copy())) {
                if (capability == null) {
                    return;
                }
                FluidStack fluid = capability.extractAny(81000);
                if (fluid.isEmpty()) {
                    return;
                }
                Identifier itemName = Registries.ITEM.getId(stack.getItem());
                Identifier fluidName = Registries.FLUID.getId(fluid.getFluid());
                Identifier id = Identifier.of(
                    MOD_ID,
                    "empty_" + itemName.getNamespace() + "_" + itemName.getPath() + "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()
                );
                registry.add(new DrainingDisplay(
                    EntryIngredients.of(stack),
                    IngredientHelper.createEntryIngredient(fluid),
                    EntryIngredients.of(capability.getContainer()),
                    Optional.of(id)
                ));
            }
        });
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(output, result);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.DRAINING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

        public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
