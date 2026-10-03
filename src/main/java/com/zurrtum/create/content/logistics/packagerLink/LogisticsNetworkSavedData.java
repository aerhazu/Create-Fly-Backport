package com.zurrtum.create.content.logistics.packagerLink;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LogisticsNetworkSavedData extends PersistentState {
    private static final String NAME = "create_logistics";
    public static final Codec<LogisticsNetworkSavedData> CODEC = Codec.list(LogisticsNetwork.CODEC)
        .xmap(LogisticsNetworkSavedData::createMap, LogisticsNetworkSavedData::toList)
        .xmap(LogisticsNetworkSavedData::new, LogisticsNetworkSavedData::getLogisticsNetworks);
    private static final PersistentState.Type<LogisticsNetworkSavedData> TYPE = new PersistentState.Type<>(
        LogisticsNetworkSavedData::new,
        (nbt, registries) -> CODEC.decode(RegistryOps.of(NbtOps.INSTANCE, registries), nbt.get("Networks")).getOrThrow().getFirst(),
        null
    );

    private final Map<UUID, LogisticsNetwork> logisticsNetworks;

    public Map<UUID, LogisticsNetwork> getLogisticsNetworks() {
        return logisticsNetworks;
    }

    private LogisticsNetworkSavedData() {
        logisticsNetworks = new HashMap<>();
    }

    private LogisticsNetworkSavedData(Map<UUID, LogisticsNetwork> logisticsNetworks) {
        this.logisticsNetworks = logisticsNetworks;
    }

    private static Map<UUID, LogisticsNetwork> createMap(List<LogisticsNetwork> list) {
        Map<UUID, LogisticsNetwork> logisticsNetworks = new HashMap<>();
        list.forEach(network -> logisticsNetworks.put(network.id, network));
        return logisticsNetworks;
    }

    private static List<LogisticsNetwork> toList(Map<UUID, LogisticsNetwork> logisticsNetworks) {
        return logisticsNetworks.values().stream().toList();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, registries);
        nbt.put("Networks", CODEC.encodeStart(ops, this).getOrThrow());
        return nbt;
    }

    public static LogisticsNetworkSavedData load(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, NAME);
    }
}
