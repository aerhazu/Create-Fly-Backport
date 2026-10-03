package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public record PlaceExtendedCurvePacket(boolean mainHand, boolean ctrlDown) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, PlaceExtendedCurvePacket> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOL,
        PlaceExtendedCurvePacket::mainHand,
        PacketCodecs.BOOL,
        PlaceExtendedCurvePacket::ctrlDown,
        PlaceExtendedCurvePacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onPlaceExtendedCurve((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<PlaceExtendedCurvePacket> getPacketId() {
        return AllPackets.PLACE_CURVED_TRACK;
    }
}
