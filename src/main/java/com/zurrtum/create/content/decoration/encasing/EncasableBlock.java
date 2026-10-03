package com.zurrtum.create.content.decoration.encasing;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Implement this interface to indicate that this block is encasable.
 */
public interface EncasableBlock {
    /**
     * This method should be called in the {@link Block#onUseWithItem(ItemStack, BlockState, World, BlockPos, PlayerEntity, Hand, BlockHitResult)} method.
     */
    default ItemActionResult tryEncase(
        BlockState state,
        World level,
        BlockPos pos,
        ItemStack heldItem,
        PlayerEntity player,
        Hand hand,
        BlockHitResult ray
    ) {
        List<Block> encasedVariants = EncasingRegistry.getVariants(state.getBlock());
        for (Block block : encasedVariants) {
            if (block instanceof EncasedBlock encased) {
                if (encased.getCasing().asItem() != heldItem.getItem())
                    continue;

                if (level.isClient)
                    return ItemActionResult.SUCCESS;

                encased.handleEncasing(state, level, pos, heldItem, player, hand, ray);
                playEncaseSound(level, pos);
                return ItemActionResult.SUCCESS;
            }
        }
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    default void playEncaseSound(World level, BlockPos pos) {
        BlockState newState = level.getBlockState(pos);
        BlockSoundGroup soundType = newState.getSoundGroup();
        level.playSound(
            null,
            pos,
            soundType.getPlaceSound(),
            SoundCategory.BLOCKS,
            (soundType.getVolume() + 1.0F) / 2.0F,
            soundType.getPitch() * 0.8F
        );
    }
}
