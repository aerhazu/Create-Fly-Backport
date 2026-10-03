package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.Vec3d;

public record LimbSwingUpdatePacket(int entityId, Vec3d position, float limbSwing) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, LimbSwingUpdatePacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        LimbSwingUpdatePacket::entityId,
        CatnipStreamCodecs.VEC3D,
        LimbSwingUpdatePacket::position,
        PacketCodecs.FLOAT,
        LimbSwingUpdatePacket::limbSwing,
        LimbSwingUpdatePacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onLimbSwingUpdate(listener, this);
    }

    @Override
    public PacketType<LimbSwingUpdatePacket> getPacketId() {
        return AllPackets.LIMBSWING_UPDATE;
    }
}
