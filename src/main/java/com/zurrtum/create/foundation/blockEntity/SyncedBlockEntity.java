package com.zurrtum.create.foundation.blockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import com.zurrtum.create.foundation.storage.NbtReadView;
import com.zurrtum.create.foundation.storage.NbtWriteView;
import com.zurrtum.create.foundation.storage.ReadView;
import com.zurrtum.create.foundation.storage.WriteView;
import com.zurrtum.create.foundation.storage.ErrorReporter;
import net.minecraft.util.math.BlockPos;

import static com.zurrtum.create.Create.LOGGER;

public abstract class SyncedBlockEntity extends BlockEntity {
    public SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(LOGGER)) {
            NbtWriteView view = NbtWriteView.create(logging, registries);
            writeClient(view);
            return view.getNbt();
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    /**
     * Bridges vanilla 1.21.1's NbtCompound-based save/load into the ReadView/WriteView-shaped
     * readData/writeData convention this codebase (originally written against a later Minecraft
     * version) uses throughout.
     */
    @Override
    protected final void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(LOGGER)) {
            readData(NbtReadView.create(logging, registryLookup, nbt));
        }
    }

    @Override
    protected final void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(LOGGER)) {
            writeData(NbtWriteView.create(logging, registryLookup, nbt));
        }
    }

    protected void readData(ReadView view) {
    }

    protected void writeData(WriteView view) {
    }

    public void handleUpdateTag(ReadView view) {
        readClient(view);
    }

    public void onDataPacket(ReadView view) {
        readClient(view);
    }

    // Special handling for client update packets
    public void readClient(ReadView view) {
        readData(view);
    }

    // Special handling for client update packets
    public void writeClient(WriteView view) {
        writeData(view);
    }

    public void sendData() {
        if (world instanceof ServerWorld serverLevel)
            serverLevel.getChunkManager().markForUpdate(getPos());
    }

    public void notifyUpdate() {
        markDirty();
        sendData();
    }

    public RegistryEntryLookup<Block> blockHolderGetter() {
        return world != null ? world.getRegistryManager().getWrapperOrThrow(RegistryKeys.BLOCK) : Registries.BLOCK.getReadOnlyWrapper();
    }

}
