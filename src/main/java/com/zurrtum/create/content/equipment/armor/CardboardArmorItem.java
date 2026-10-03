package com.zurrtum.create.content.equipment.armor;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.registry.entry.RegistryEntry;

public class CardboardArmorItem extends ArmorItem {
    public CardboardArmorItem(RegistryEntry<ArmorMaterial> material, ArmorItem.Type type, Settings settings) {
        super(material, type, settings);
    }
}
