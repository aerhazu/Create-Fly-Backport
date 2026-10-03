package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public record TrainRelocationPacket(
    UUID trainId, BlockPos pos, Vec3d lookAngle, int entityId, boolean direction, BezierTrackPointLocation hoveredBezier
) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, TrainRelocationPacket> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC,
        TrainRelocationPacket::trainId,
        BlockPos.PACKET_CODEC,
        TrainRelocationPacket::pos,
        CatnipStreamCodecs.VEC3D,
        TrainRelocationPacket::lookAngle,
        PacketCodecs.INTEGER,
        TrainRelocationPacket::entityId,
        PacketCodecs.BOOL,
        TrainRelocationPacket::direction,
        CatnipStreamCodecBuilders.nullable(BezierTrackPointLocation.STREAM_CODEC),
        TrainRelocationPacket::hoveredBezier,
        TrainRelocationPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onTrainRelocation((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<TrainRelocationPacket> getPacketId() {
        return AllPackets.RELOCATE_TRAIN;
    }
}
