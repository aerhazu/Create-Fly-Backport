package com.zurrtum.create.content.equipment.armor;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.foundation.item.LayeredArmorItem;
import net.minecraft.block.Block;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import static com.zurrtum.create.Create.MOD_ID;

public class BacktankItem extends BlockItem implements Equipment {
    public static final EquipmentSlot SLOT = EquipmentSlot.CHEST;
    public static final int BAR_COLOR = 0xEFEFEF;

    public BacktankItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public EquipmentSlot getSlotType() {
        return SLOT;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        return equipAndSwap(this, world, player, hand);
    }

    public static BacktankItem copper(Settings settings) {
        return new BacktankItem(AllBlocks.COPPER_BACKTANK, settings);
    }

    public static BacktankItem netherite(Settings settings) {
        Identifier layer = Identifier.of(MOD_ID, "textures/models/armor/netherite_diving_layer.png");
        return new Layered(AllBlocks.NETHERITE_BACKTANK, settings, layer);
    }

    public ItemStack getMaxAirStack() {
        ItemStack stack = getDefaultStack();
        stack.set(AllDataComponents.BACKTANK_AIR, BacktankUtil.maxAirWithoutEnchants());
        return stack;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F * MathHelper.clamp(getRemainingAir(stack) / ((float) BacktankUtil.maxAir(stack)), 0, 1));
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    public static int getRemainingAir(ItemStack stack) {
        return stack.getOrDefault(AllDataComponents.BACKTANK_AIR, 0);
    }

    public static class Layered extends BacktankItem implements LayeredArmorItem {
        private final Identifier layer;

        public Layered(Block block, Settings settings, Identifier layer) {
            super(block, settings);
            this.layer = layer;
        }

        @Override
        public Identifier getLayerTexture() {
            return layer;
        }
    }
}
