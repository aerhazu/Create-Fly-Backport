package com.zurrtum.create.compat.rei;

import com.zurrtum.create.AllFluids;
import com.zurrtum.create.compat.rei.display.*;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.comparison.FluidComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;

import java.util.Objects;

import static com.zurrtum.create.Create.MOD_ID;

public class ReiCommonPlugin implements REIServerPlugin {
    public static final CategoryIdentifier<DefaultCraftingDisplay<?>> AUTOMATIC_PACKING = CategoryIdentifier.of(MOD_ID, "automatic_packing");
    public static final CategoryIdentifier<CompactingDisplay> PACKING = CategoryIdentifier.of(MOD_ID, "packing");
    public static final CategoryIdentifier<PressingDisplay> PRESSING = CategoryIdentifier.of(MOD_ID, "pressing");
    public static final CategoryIdentifier<DefaultCraftingDisplay<?>> AUTOMATIC_SHAPELESS = CategoryIdentifier.of(MOD_ID, "automatic_shapeless");
    public static final CategoryIdentifier<MixingDisplay> MIXING = CategoryIdentifier.of(MOD_ID, "mixing");
    public static final CategoryIdentifier<MillingDisplay> MILLING = CategoryIdentifier.of(MOD_ID, "milling");
    public static final CategoryIdentifier<SawingDisplay> SAWING = CategoryIdentifier.of(MOD_ID, "sawing");
    public static final CategoryIdentifier<CrushingDisplay> CRUSHING = CategoryIdentifier.of(MOD_ID, "crushing");
    public static final CategoryIdentifier<ManualApplicationDisplay> ITEM_APPLICATION = CategoryIdentifier.of(MOD_ID, "item_application");
    public static final CategoryIdentifier<DeployingDisplay> DEPLOYING = CategoryIdentifier.of(MOD_ID, "deploying");
    public static final CategoryIdentifier<DrainingDisplay> DRAINING = CategoryIdentifier.of(MOD_ID, "draining");
    public static final CategoryIdentifier<MechanicalCraftingDisplay> MECHANICAL_CRAFTING = CategoryIdentifier.of(MOD_ID, "mechanical_crafting");
    public static final CategoryIdentifier<SpoutFillingDisplay> SPOUT_FILLING = CategoryIdentifier.of(MOD_ID, "spout_filling");
    public static final CategoryIdentifier<SandpaperPolishingDisplay> SANDPAPER_POLISHING = CategoryIdentifier.of(MOD_ID, "sandpaper_polishing");
    public static final CategoryIdentifier<SequencedAssemblyDisplay> SEQUENCED_ASSEMBLY = CategoryIdentifier.of(MOD_ID, "sequenced_assembly");
    public static final CategoryIdentifier<BlockCuttingDisplay> BLOCK_CUTTING = CategoryIdentifier.of(MOD_ID, "block_cutting");
    public static final CategoryIdentifier<FanBlastingDisplay> FAN_BLASTING = CategoryIdentifier.of(MOD_ID, "fan_blasting");
    public static final CategoryIdentifier<FanHauntingDisplay> FAN_HAUNTING = CategoryIdentifier.of(MOD_ID, "fan_haunting");
    public static final CategoryIdentifier<FanSmokingDisplay> FAN_SMOKING = CategoryIdentifier.of(MOD_ID, "fan_smoking");
    public static final CategoryIdentifier<FanWashingDisplay> FAN_WASHING = CategoryIdentifier.of(MOD_ID, "fan_washing");
    public static final CategoryIdentifier<PotionDisplay> AUTOMATIC_BREWING = CategoryIdentifier.of(MOD_ID, "automatic_brewing");

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(
            CategoryIdentifier.of(AUTOMATIC_PACKING.getIdentifier().withSuffixedPath("/default/shapeless")),
            AutoCompactingDisplay.ShapelessDisplay.SERIALIZER
        );
        registry.register(
            CategoryIdentifier.of(AUTOMATIC_PACKING.getIdentifier().withSuffixedPath("/default/shaped")),
            AutoCompactingDisplay.ShapedDisplay.SERIALIZER
        );
        registry.register(PACKING, CompactingDisplay.SERIALIZER);
        registry.register(PRESSING, PressingDisplay.SERIALIZER);
        registry.register(
            CategoryIdentifier.of(AUTOMATIC_SHAPELESS.getIdentifier().withSuffixedPath("/default/shapeless")),
            AutoMixingDisplay.ShapelessDisplay.SERIALIZER
        );
        registry.register(MIXING, MixingDisplay.SERIALIZER);
        registry.register(MILLING, MillingDisplay.SERIALIZER);
        registry.register(SAWING, SawingDisplay.SERIALIZER);
        registry.register(CRUSHING, CrushingDisplay.SERIALIZER);
        registry.register(ITEM_APPLICATION, ManualApplicationDisplay.SERIALIZER);
        registry.register(DEPLOYING, DeployingDisplay.SERIALIZER);
        registry.register(DRAINING, DrainingDisplay.SERIALIZER);
        registry.register(MECHANICAL_CRAFTING, MechanicalCraftingDisplay.SERIALIZER);
        registry.register(SPOUT_FILLING, SpoutFillingDisplay.SERIALIZER);
        registry.register(SANDPAPER_POLISHING, SandpaperPolishingDisplay.SERIALIZER);
        registry.register(SEQUENCED_ASSEMBLY, SequencedAssemblyDisplay.SERIALIZER);
        registry.register(BLOCK_CUTTING, BlockCuttingDisplay.SERIALIZER);
        registry.register(FAN_BLASTING, FanBlastingDisplay.SERIALIZER);
        registry.register(FAN_HAUNTING, FanHauntingDisplay.SERIALIZER);
        registry.register(FAN_SMOKING, FanSmokingDisplay.SERIALIZER);
        registry.register(FAN_WASHING, FanWashingDisplay.SERIALIZER);
        registry.register(AUTOMATIC_BREWING, PotionDisplay.SERIALIZER);
    }

    @Override
    public void registerFluidComparators(FluidComparatorRegistry registry) {
        registry.register((context, stack) -> Objects.hash(stack.getFluid(), stack.getComponents()), AllFluids.POTION);
    }
}
