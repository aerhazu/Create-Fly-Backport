package com.zurrtum.create.mixin;

import com.zurrtum.create.Create;
import com.zurrtum.create.api.behaviour.display.DisplayHolder;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import com.zurrtum.create.foundation.storage.ErrorReporter;
import com.zurrtum.create.foundation.storage.NbtReadView;
import com.zurrtum.create.foundation.storage.NbtWriteView;
import com.zurrtum.create.foundation.storage.ReadView;
import com.zurrtum.create.foundation.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternBlockEntity.class)
public class LecternBlockEntityMixin implements DisplayHolder {
    @Unique
    private NbtCompound displayLink;

    @Override
    public NbtCompound getDisplayLinkData() {
        return displayLink;
    }

    @Override
    public void setDisplayLinkData(NbtCompound data) {
        displayLink = data;
    }

    @Inject(method = "writeNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)V", at = @At("TAIL"))
    private void writeData(NbtCompound nbt, RegistryWrapper.WrapperLookup registries, CallbackInfo ci) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(Create.LOGGER)) {
            WriteView view = NbtWriteView.create(logging, registries, nbt);
            writeDisplayLink(view);
        }
    }

    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)V", at = @At("TAIL"))
    private void readData(NbtCompound nbt, RegistryWrapper.WrapperLookup registries, CallbackInfo ci) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(Create.LOGGER)) {
            ReadView view = NbtReadView.create(logging, registries, nbt);
            readDisplayLink(view);
        }
    }
}
