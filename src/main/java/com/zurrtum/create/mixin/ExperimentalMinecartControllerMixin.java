package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.block.MinecartPassBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartEntity.class)
public abstract class ExperimentalMinecartControllerMixin {
    @Inject(method = "moveOnRail(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V", at = @At("HEAD"))
    private void onMinecartPass(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (state.getBlock() instanceof MinecartPassBlock block) {
            AbstractMinecartEntity self = (AbstractMinecartEntity) (Object) this;
            block.onMinecartPass(state, self.getWorld(), pos, self);
        }
    }
}
