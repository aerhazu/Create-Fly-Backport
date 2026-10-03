package com.zurrtum.create.client.compat.rei.renderer;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.infrastructure.component.BottleType;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public record FluidStackRenderer(EntryRenderer<FluidStack> origin) implements EntryRenderer<FluidStack> {
    @Override
    public void render(EntryStack<FluidStack> entry, DrawContext graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        FluidStack stack = entry.getValue();
        Fluid fluid = stack.getFluid();
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config == null) {
            return;
        }
        int color = config.tint().apply(stack.getComponents().getChanges()) | 0xff000000;
        float a = (color >>> 24 & 0xFF) / 255f;
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        graphics.drawSprite(bounds.x, bounds.y, 0, bounds.width, bounds.height, config.still().get(), r, g, b, a);
    }

    @Override
    public Tooltip getTooltip(EntryStack<FluidStack> entry, TooltipContext context) {
        Tooltip tooltip = origin.getTooltip(entry, context);
        if (tooltip == null) {
            return null;
        }
        List<Tooltip.Entry> entries = tooltip.entries();
        Tooltip.Entry first = entries.getFirst();
        if (first.isText()) {
            FluidStack stack = entry.getValue();
            if (stack.getFluid() == AllFluids.POTION) {
                ComponentMapImpl components = stack.getComponents();
                PotionContentsComponent contents = components.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
                BottleType bottleType = components.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
                String prefix = PotionFluidHandler.itemFromBottleType(bottleType).getTranslationKey() + ".effect.";
                Text name = contents.potion()
                    .<Text>map(potion -> Text.translatable(prefix + Registries.POTION.getId(potion.value()).getPath()))
                    .orElse(Text.translatable(prefix.substring(0, prefix.length() - 1)));
                List<Tooltip.Entry> list = new ArrayList<>();
                list.add(Tooltip.entry(name));
                float scale = 1f;
                PotionContentsComponent.buildTooltip(
                    contents.getEffects(),
                    text -> list.add(Tooltip.entry(text)),
                    scale,
                    context.vanillaContext().getUpdateTickRate()
                );
                entries.removeFirst();
                entries.addAll(0, list);
            }
        }
        return tooltip;
    }
}
