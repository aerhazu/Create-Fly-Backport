package com.zurrtum.create.content.equipment;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class BuildersTeaItem extends Item {
    public BuildersTeaItem(Settings properties) {
        super(properties);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public SoundEvent getDrinkSound() {
        return SoundEvents.ENTITY_GENERIC_DRINK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return (int) (2.1F * 20);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World level, LivingEntity livingEntity) {
        ItemStack eatResult = super.finishUsing(stack, level, livingEntity);
        if (livingEntity instanceof PlayerEntity player && !player.getAbilities().creativeMode) {
            if (eatResult.isEmpty()) {
                return Items.GLASS_BOTTLE.getDefaultStack();
            } else {
                player.getInventory().insertStack(Items.GLASS_BOTTLE.getDefaultStack());
            }
        }
        return eatResult;
    }
}
