package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkState;
import net.minecraft.network.NetworkStateBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientConfigurationPacketListener;
import net.minecraft.network.state.ConfigurationStates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(ConfigurationStates.class)
public class ConfigurationStatesMixin {
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkStateBuilder;s2c(Lnet/minecraft/network/NetworkPhase;Ljava/util/function/Consumer;)Lnet/minecraft/network/NetworkState$Factory;"))
    private static NetworkState.Factory<ClientConfigurationPacketListener, PacketByteBuf> addS2CPacket(
        NetworkPhase type,
        Consumer<NetworkStateBuilder<ClientConfigurationPacketListener, PacketByteBuf>> registrar,
        Operation<NetworkState.Factory<ClientConfigurationPacketListener, PacketByteBuf>> original
    ) {
        return original.call(
            type, (Consumer<NetworkStateBuilder<ClientConfigurationPacketListener, PacketByteBuf>>) (builder -> {
                registrar.accept(builder);
                AllPackets.S2C_CONFIG.forEach(builder::add);
            })
        );
    }
}
