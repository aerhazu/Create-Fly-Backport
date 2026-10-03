package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkState;
import net.minecraft.network.NetworkStateBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.state.PlayStateFactories;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(PlayStateFactories.class)
public class PlayStateFactoriesMixin {
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkStateBuilder;s2c(Lnet/minecraft/network/NetworkPhase;Ljava/util/function/Consumer;)Lnet/minecraft/network/NetworkState$Factory;"))
    private static NetworkState.Factory<ClientPlayPacketListener, RegistryByteBuf> addS2CPacket(
        NetworkPhase type,
        Consumer<NetworkStateBuilder<ClientPlayPacketListener, RegistryByteBuf>> registrar,
        Operation<NetworkState.Factory<ClientPlayPacketListener, RegistryByteBuf>> original
    ) {
        return original.call(
            type, (Consumer<NetworkStateBuilder<ClientPlayPacketListener, RegistryByteBuf>>) (builder -> {
                registrar.accept(builder);
                AllPackets.S2C.forEach(builder::add);
            })
        );
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkStateBuilder;c2s(Lnet/minecraft/network/NetworkPhase;Ljava/util/function/Consumer;)Lnet/minecraft/network/NetworkState$Factory;"))
    private static NetworkState.Factory<ServerPlayPacketListener, RegistryByteBuf> addC2SPacket(
        NetworkPhase type,
        Consumer<NetworkStateBuilder<ServerPlayPacketListener, RegistryByteBuf>> registrar,
        Operation<NetworkState.Factory<ServerPlayPacketListener, RegistryByteBuf>> original
    ) {
        return original.call(
            type, (Consumer<NetworkStateBuilder<ServerPlayPacketListener, RegistryByteBuf>>) (builder -> {
                registrar.accept(builder);
                AllPackets.C2S.forEach(builder::add);
            })
        );
    }
}
