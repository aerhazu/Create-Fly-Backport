package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidVariantRenderHandler.class)
public interface FluidVariantRenderHandlerMixin {
    @Inject(method = "getSprites(Lnet/fabricmc/fabric/api/transfer/v1/fluid/FluidVariant;)[Lnet/minecraft/client/texture/Sprite;", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private void getSprites(FluidVariant variant, CallbackInfoReturnable<Sprite[]> cir) {
        if (cir.getReturnValue() != null) {
            return;
        }
        Fluid fluid = variant.getFluid();
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config != null) {
            cir.setReturnValue(config.toSprite());
        }
    }

    @Inject(method = "getColor(Lnet/fabricmc/fabric/api/transfer/v1/fluid/FluidVariant;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private void getColor(FluidVariant variant, BlockRenderView view, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() != -1) {
            return;
        }
        Fluid fluid = variant.getFluid();
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config != null) {
            cir.setReturnValue(config.tint().apply(variant.getComponents()));
        }
    }
}
