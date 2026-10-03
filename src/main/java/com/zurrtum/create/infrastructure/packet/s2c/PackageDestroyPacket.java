package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.Vec3d;

public record PackageDestroyPacket(Vec3d location, ItemStack box) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, PackageDestroyPacket> CODEC = PacketCodec.tuple(
        CatnipStreamCodecs.VEC3D,
        PackageDestroyPacket::location,
        ItemStack.PACKET_CODEC,
        PackageDestroyPacket::box,
        PackageDestroyPacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onPackageDestroy(listener, this);
    }

    @Override
    public PacketType<PackageDestroyPacket> getPacketId() {
        return AllPackets.PACKAGE_DESTROYED;
    }
}
