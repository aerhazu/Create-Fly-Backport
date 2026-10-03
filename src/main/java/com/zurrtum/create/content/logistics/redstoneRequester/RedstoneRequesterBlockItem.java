package com.zurrtum.create.content.logistics.redstoneRequester;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RedstoneRequesterBlockItem extends LogisticallyLinkedBlockItem {

    public RedstoneRequesterBlockItem(Block pBlock, Settings pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void appendTooltip(
        @NotNull ItemStack stack,
        @NotNull TooltipContext tooltipContext,
        @NotNull List<Text> textConsumer,
        TooltipType type
    ) {
        if (!isTuned(stack))
            return;

        if (!stack.contains(AllDataComponents.AUTO_REQUEST_DATA)) {
            super.appendTooltip(stack, tooltipContext, textConsumer, type);
            return;
        }

        textConsumer.add(Text.translatable("create.logistically_linked.tooltip").formatted(Formatting.GOLD));
        RedstoneRequesterBlock.appendRequesterTooltip(stack, textConsumer::add);
    }

}
