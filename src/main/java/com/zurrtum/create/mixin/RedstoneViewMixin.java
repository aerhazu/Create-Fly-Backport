package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.block.WeakPowerControlBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RedstoneView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneView.class)
public interface RedstoneViewMixin {
    @Inject(method = "getEmittedRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", at = @At("HEAD"), cancellable = true)
    private void create$weakPower(BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir) {
        RedstoneView self = (RedstoneView) this;
        BlockState state = self.getBlockState(pos);
        if (state.getBlock() instanceof WeakPowerControlBlock block) {
            int weakPower = state.getWeakRedstonePower(self, pos, direction);
            if (block.shouldCheckWeakPower(state, self, pos, direction)) {
                cir.setReturnValue(Math.max(weakPower, self.getReceivedStrongRedstonePower(pos)));
            } else {
                cir.setReturnValue(weakPower);
            }
        }
    }
}
