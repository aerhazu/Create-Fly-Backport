package com.zurrtum.create.content.equipment.tool;

import com.zurrtum.create.AllItemTags;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public class AllToolMaterials {
    public static final ToolMaterial CARDBOARD = register(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 1, 1, 2, 1, AllItemTags.REPAIRS_CARDBOARD_ARMOR);

    private static ToolMaterial register(
        TagKey<Block> incorrectBlocksForDrops,
        int durability,
        float speed,
        float attackDamageBonus,
        int enchantmentValue,
        TagKey<Item> repairItems
    ) {
        return new Simple(incorrectBlocksForDrops, durability, speed, attackDamageBonus, enchantmentValue, repairItems);
    }

    public static void register() {
    }

    private record Simple(
        TagKey<Block> inverseTag,
        int durability,
        float miningSpeedMultiplier,
        float attackDamage,
        int enchantability,
        TagKey<Item> repairItems
    ) implements ToolMaterial {
        @Override
        public int getDurability() {
            return durability;
        }

        @Override
        public float getMiningSpeedMultiplier() {
            return miningSpeedMultiplier;
        }

        @Override
        public float getAttackDamage() {
            return attackDamage;
        }

        @Override
        public TagKey<Block> getInverseTag() {
            return inverseTag;
        }

        @Override
        public int getEnchantability() {
            return enchantability;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.fromTag(repairItems);
        }
    }
}
