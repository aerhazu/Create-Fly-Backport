package com.zurrtum.create.content.contraptions.bearing;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WindmillBearingBlock extends BearingBlock implements IBE<WindmillBearingBlockEntity> {

    public WindmillBearingBlock(Settings properties) {
        super(properties);
    }

    @Override
    protected ItemActionResult onUseWithItem(
        ItemStack stack,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        if (!player.canModifyBlocks())
            return ItemActionResult.FAIL;
        if (player.isSneaking())
            return ItemActionResult.FAIL;
        if (stack.isEmpty()) {
            if (level.isClient)
                return ItemActionResult.SUCCESS;
            withBlockEntityDo(
                level, pos, be -> {
                    if (be.running) {
                        be.disassemble();
                        return;
                    }
                    be.assembleNextTick = true;
                }
            );
            return ItemActionResult.SUCCESS;
        }
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public Class<WindmillBearingBlockEntity> getBlockEntityClass() {
        return WindmillBearingBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WindmillBearingBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.WINDMILL_BEARING;
    }
}
