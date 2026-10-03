package com.zurrtum.create.catnip.codecs.stream;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface CatnipStreamCodecs {
    PacketCodec<PacketByteBuf, Character> CHAR = new PacketCodec<>() {
        public @NotNull Character decode(PacketByteBuf buffer) {
            return buffer.readChar();
        }

        public void encode(PacketByteBuf buffer, @NotNull Character value) {
            buffer.writeChar(value);
        }
    };
    PacketCodec<RegistryByteBuf, RegistryEntry<Fluid>> HOLDER_FLUID = PacketCodecs.registryEntry(RegistryKeys.FLUID);
    PacketCodec<RegistryByteBuf, Fluid> FLUID = PacketCodecs.registryValue(RegistryKeys.FLUID);
    PacketCodec<ByteBuf, NbtElement> COMPOUND_AS_TAG = PacketCodecs.NBT_COMPOUND.xmap(Function.identity(), tag -> (NbtCompound) tag);
    PacketCodec<PacketByteBuf, NbtList> COMPOUND_LIST_TAG = new PacketCodec<>() {
        @Override
        public @NotNull NbtList decode(PacketByteBuf buffer) {
            return buffer.readCollection(size -> new NbtList(), COMPOUND_AS_TAG);
        }

        @Override
        public void encode(PacketByteBuf buffer, NbtList value) {
            buffer.writeCollection(value, COMPOUND_AS_TAG);
        }
    };
    PacketCodec<ByteBuf, BlockState> BLOCK_STATE = PacketCodecs.entryOf(Block.STATE_IDS);
    PacketCodec<ByteBuf, Vec3d> VEC3D = PacketCodecs.codec(Vec3d.CODEC);
    PacketCodec<ByteBuf, Vec3i> VEC3I = PacketCodecs.codec(Vec3i.CODEC);
    PacketCodec<ByteBuf, BlockPos> NULLABLE_BLOCK_POS = CatnipStreamCodecBuilders.nullable(BlockPos.PACKET_CODEC);
    PacketCodec<ByteBuf, Direction.Axis> AXIS = CatnipStreamCodecBuilders.ofEnum(Direction.Axis.class);
    PacketCodec<ByteBuf, BlockMirror> MIRROR = CatnipStreamCodecBuilders.ofEnum(BlockMirror.class);

    // optimization: 2 values, use bool instead of ofEnum
    PacketCodec<ByteBuf, Hand> HAND = PacketCodecs.BOOL.xmap(value -> value ? Hand.MAIN_HAND : Hand.OFF_HAND, hand -> hand == Hand.MAIN_HAND);

    PacketCodec<PacketByteBuf, BlockHitResult> BLOCK_HIT_RESULT = PacketCodec.tuple(
        PacketCodecs.BOOL,
        i -> i.getType() == HitResult.Type.MISS,
        VEC3D,
        HitResult::getPos,
        Direction.PACKET_CODEC,
        BlockHitResult::getSide,
        BlockPos.PACKET_CODEC,
        BlockHitResult::getBlockPos,
        PacketCodecs.BOOL,
        BlockHitResult::isInsideBlock,
        (miss, location, direction, blockPos, isInside) -> miss ? BlockHitResult.createMissed(location, direction, blockPos) : new BlockHitResult(
            location,
            direction,
            blockPos,
            isInside
        )
    );
}
