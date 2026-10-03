package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;

import java.util.function.UnaryOperator;

public class BlockEntityDataInstruction extends WorldModifyInstruction {

    private final boolean redraw;
    private final UnaryOperator<NbtCompound> data;
    private final Class<? extends BlockEntity> type;

    public BlockEntityDataInstruction(Selection selection, Class<? extends BlockEntity> type, UnaryOperator<NbtCompound> data, boolean redraw) {
        super(selection);
        this.type = type;
        this.data = data;
        this.redraw = redraw;
    }

    @Override
    protected void runModification(Selection selection, PonderScene scene) {
        PonderLevel level = scene.getWorld();
        selection.forEach(pos -> {
            if (!level.getBounds().contains(pos))
                return;
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!type.isInstance(blockEntity))
                return;
            DynamicRegistryManager registryManager = level.getRegistryManager();
            NbtCompound apply = data.apply(blockEntity.createNbtWithIdentifyingData(registryManager));
            //if (blockEntity instanceof SyncedBlockEntity) //TODO
            //	((SyncedBlockEntity) blockEntity).readClient(apply);
            blockEntity.read(apply, registryManager);
        });
    }

    @Override
    protected boolean needsRedraw() {
        return redraw;
    }

}
