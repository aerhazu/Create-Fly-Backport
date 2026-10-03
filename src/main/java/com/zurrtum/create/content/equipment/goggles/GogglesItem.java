package com.zurrtum.create.content.equipment.goggles;

import com.zurrtum.create.AllItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class GogglesItem extends Item implements Equipment {
    private static final List<Predicate<PlayerEntity>> IS_WEARING_PREDICATES = new ArrayList<>();

    static {
        addIsWearingPredicate(player -> player.getEquippedStack(EquipmentSlot.HEAD).isOf(AllItems.GOGGLES));
    }

    public GogglesItem(Item.Settings properties) {
        super(properties);
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        return equipAndSwap(this, world, player, hand);
    }

    public static boolean isWearingGoggles(PlayerEntity player) {
        for (Predicate<PlayerEntity> predicate : IS_WEARING_PREDICATES) {
            if (predicate.test(player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Use this method to add custom entry points to the goggles overlay, e.g. custom
     * armor, handheld alternatives, etc.
     */
    public static synchronized void addIsWearingPredicate(Predicate<PlayerEntity> predicate) {
        IS_WEARING_PREDICATES.add(predicate);
    }
}