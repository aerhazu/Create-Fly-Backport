package com.zurrtum.create.content.logistics.tableCloth;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.logistics.redstoneRequester.RedstoneRequesterBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class TableClothBlockItem extends BlockItem {

    public TableClothBlockItem(Block pBlock, Settings pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public boolean hasGlint(ItemStack pStack) {
        return pStack.contains(AllDataComponents.AUTO_REQUEST_DATA);
    }

    @Override
    public void appendTooltip(
        ItemStack stack,
        TooltipContext context,
        List<Text> textConsumer,
        TooltipType type
    ) {
        super.appendTooltip(stack, context, textConsumer, type);
        if (!hasGlint(stack))
            return;

        textConsumer.add(Text.translatable("create.table_cloth.shop_configured").formatted(Formatting.GOLD));

        RedstoneRequesterBlock.appendRequesterTooltip(stack, textConsumer::add);
    }

}
