package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.Vec3d;

public record ClientMotionPacket(Vec3d motion, boolean onGround, float limbSwing) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, ClientMotionPacket> CODEC = PacketCodec.tuple(
        CatnipStreamCodecs.VEC3D,
        ClientMotionPacket::motion,
        PacketCodecs.BOOL,
        ClientMotionPacket::onGround,
        PacketCodecs.FLOAT,
        ClientMotionPacket::limbSwing,
        ClientMotionPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onClientMotion((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<ClientMotionPacket> getPacketId() {
        return AllPackets.CLIENT_MOTION;
    }
}
