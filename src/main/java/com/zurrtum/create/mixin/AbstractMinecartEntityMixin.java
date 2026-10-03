package com.zurrtum.create.mixin;

import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.contraptions.minecart.capability.MinecartController;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import com.zurrtum.create.foundation.storage.ErrorReporter;
import com.zurrtum.create.foundation.storage.NbtReadView;
import com.zurrtum.create.foundation.storage.NbtWriteView;
import com.zurrtum.create.foundation.storage.ReadView;
import com.zurrtum.create.foundation.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AbstractMinecartEntity.class)
public class AbstractMinecartEntityMixin {
    @Inject(method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    private void readCustomData(NbtCompound nbt, CallbackInfo ci) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(Create.LOGGER)) {
            ReadView input = NbtReadView.create(logging, (net.minecraft.registry.RegistryWrapper.WrapperLookup) null, nbt);
            input.read("create:minecart_controller", MinecartController.CODEC).ifPresent(controller -> {
                AbstractMinecartEntity minecart = (AbstractMinecartEntity) (Object) this;
                controller.setCart(minecart);
                AllSynchedDatas.MINECART_CONTROLLER.set(minecart, Optional.of(controller));
            });
        }
    }

    @Inject(method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    private void writeCustomData(NbtCompound nbt, CallbackInfo ci) {
        AllSynchedDatas.MINECART_CONTROLLER.get((AbstractMinecartEntity) (Object) this).ifPresent(controller -> {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(Create.LOGGER)) {
                WriteView output = NbtWriteView.create(logging, null, nbt);
                output.put("create:minecart_controller", MinecartController.CODEC, controller);
            }
        });
    }
}
