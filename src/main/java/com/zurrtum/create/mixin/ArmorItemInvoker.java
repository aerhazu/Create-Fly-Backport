package com.zurrtum.create.mixin;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArmorItem.class)
public interface ArmorItemInvoker {
    @Invoker("method_56689")
    static AttributeModifiersComponent createAttributeModifiers(RegistryEntry<ArmorMaterial> material, ArmorItem.Type type) {
        throw new AssertionError();
    }
}
