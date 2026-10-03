package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.zurrtum.create.AllFuelTimes;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(AbstractFurnaceBlockEntity.class)
public class FuelRegistryMixin {
    @ModifyReturnValue(method = "createFuelTimeMap", at = @At("RETURN"))
    private static Map<Item, Integer> addFuelTimes(Map<Item, Integer> map) {
        AllFuelTimes.ALL.forEach((item, time) -> map.put(item.asItem(), time));
        return map;
    }
}
