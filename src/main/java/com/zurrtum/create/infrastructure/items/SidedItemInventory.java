package com.zurrtum.create.infrastructure.items;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;

public interface SidedItemInventory extends SidedInventory, ItemInventory {
    @Override
    default java.util.Iterator<ItemStack> iterator() {
        return ItemInventory.super.iterator();
    }
}
