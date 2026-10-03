package com.zurrtum.create.content.equipment.armor;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.mixin.ArmorItemInvoker;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllArmorMaterials {
    public static final int COPPER_DURABILITY = 7;
    public static final RegistryEntry<ArmorMaterial> COPPER = register(
        "copper",
        createDefenseMap(1, 3, 4, 2, 4),
        7,
        SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
        0.0F,
        0.0F,
        () -> Ingredient.fromTag(AllItemTags.REPAIRS_COPPER_ARMOR)
    );
    public static final int CARDBOARD_DURABILITY = 4;
    public static final RegistryEntry<ArmorMaterial> CARDBOARD = register(
        "cardboard",
        createDefenseMap(1, 1, 1, 1, 2),
        4,
        SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
        0.0F,
        0.0F,
        () -> Ingredient.fromTag(AllItemTags.REPAIRS_CARDBOARD_ARMOR)
    );
    public static final int NETHERITE_DURABILITY = 37;
    public static final RegistryEntry<ArmorMaterial> NETHERITE = register(
        "netherite",
        createDefenseMap(3, 6, 8, 3, 11),
        15,
        SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
        3.0F,
        0.1F,
        () -> Ingredient.ofItems(Items.NETHERITE_INGOT)
    );

    private static RegistryEntry<ArmorMaterial> register(
        String name,
        Map<ArmorItem.Type, Integer> defense,
        int enchantability,
        RegistryEntry<SoundEvent> equipSound,
        float toughness,
        float knockbackResistance,
        Supplier<Ingredient> repairIngredient
    ) {
        ArmorMaterial material = new ArmorMaterial(
            defense,
            enchantability,
            equipSound,
            repairIngredient,
            List.of(new ArmorMaterial.Layer(Identifier.of(MOD_ID, name))),
            toughness,
            knockbackResistance
        );
        return Registry.registerReference(Registries.ARMOR_MATERIAL, Identifier.of(MOD_ID, name), material);
    }

    private static Map<ArmorItem.Type, Integer> createDefenseMap(
        int bootsDefense,
        int leggingsDefense,
        int chestplateDefense,
        int helmetDefense,
        int bodyDefense
    ) {
        Map<ArmorItem.Type, Integer> map = new EnumMap<>(ArmorItem.Type.class);
        map.put(ArmorItem.Type.BOOTS, bootsDefense);
        map.put(ArmorItem.Type.LEGGINGS, leggingsDefense);
        map.put(ArmorItem.Type.CHESTPLATE, chestplateDefense);
        map.put(ArmorItem.Type.HELMET, helmetDefense);
        map.put(ArmorItem.Type.BODY, bodyDefense);
        return map;
    }

    // Vanilla's ArmorItem computes its AttributeModifiersComponent internally from the material
    // and type; borrow that computation via a throwaway instance instead of re-deriving the
    // vanilla attribute ids/uuids ourselves.
    public static AttributeModifiersComponent createAttributeModifiers(RegistryEntry<ArmorMaterial> material, ArmorItem.Type type) {
        return ArmorItemInvoker.createAttributeModifiers(material, type);
    }

    public static Item.Settings chest(RegistryEntry<ArmorMaterial> material) {
        return new Item.Settings().maxCount(1).attributeModifiers(createAttributeModifiers(material, ArmorItem.Type.CHESTPLATE));
    }

    public static void register() {
    }
}
