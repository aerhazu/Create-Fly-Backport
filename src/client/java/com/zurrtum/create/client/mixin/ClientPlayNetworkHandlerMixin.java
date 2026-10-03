package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.CommandDispatcher;
import com.zurrtum.create.Create;
import com.zurrtum.create.client.flywheel.impl.FlwCommands;
import com.zurrtum.create.foundation.blockEntity.SyncedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import com.zurrtum.create.foundation.storage.ErrorReporter;
import com.zurrtum.create.foundation.storage.NbtReadView;
import com.zurrtum.create.foundation.storage.ReadView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private CommandDispatcher<CommandSource> commandDispatcher;

    @Inject(method = "onCommandTree(Lnet/minecraft/network/packet/s2c/play/CommandTreeS2CPacket;)V", at = @At("TAIL"))
    private void addCommand(CommandTreeS2CPacket packet, CallbackInfo ci) {
        FlwCommands.registerClientCommands(commandDispatcher);
    }

    @WrapOperation(method = "method_38542(Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;Lnet/minecraft/block/entity/BlockEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;read(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)V"))
    private void onDataPacket(BlockEntity blockEntity, NbtCompound nbt, RegistryWrapper.WrapperLookup registries, Operation<Void> original) {
        if (blockEntity instanceof SyncedBlockEntity syncedBlockEntity) {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(Create.LOGGER)) {
                ReadView view = NbtReadView.create(logging, registries, nbt);
                syncedBlockEntity.onDataPacket(view);
            }
        } else {
            original.call(blockEntity, nbt, registries);
        }
    }
}
