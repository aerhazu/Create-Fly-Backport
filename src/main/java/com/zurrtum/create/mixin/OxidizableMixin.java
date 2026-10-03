package com.zurrtum.create.mixin;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.zurrtum.create.AllBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.Oxidizable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Oxidizable.class)
public interface OxidizableMixin {
    @Inject(method = "method_34740", at = @At("RETURN"), cancellable = true, remap = false)
    private static void addWaxed(CallbackInfoReturnable<BiMap<Block, Block>> cir) {
        ImmutableBiMap.Builder<Block, Block> builder = ImmutableBiMap.builder();
        builder.putAll(cir.getReturnValue());
        builder.put(AllBlocks.COPPER_SHINGLES, AllBlocks.EXPOSED_COPPER_SHINGLES);
        builder.put(AllBlocks.EXPOSED_COPPER_SHINGLES, AllBlocks.WEATHERED_COPPER_SHINGLES);
        builder.put(AllBlocks.WEATHERED_COPPER_SHINGLES, AllBlocks.OXIDIZED_COPPER_SHINGLES);
        builder.put(AllBlocks.COPPER_SHINGLE_SLAB, AllBlocks.EXPOSED_COPPER_SHINGLE_SLAB);
        builder.put(AllBlocks.EXPOSED_COPPER_SHINGLE_SLAB, AllBlocks.WEATHERED_COPPER_SHINGLE_SLAB);
        builder.put(AllBlocks.WEATHERED_COPPER_SHINGLE_SLAB, AllBlocks.OXIDIZED_COPPER_SHINGLE_SLAB);
        builder.put(AllBlocks.COPPER_SHINGLE_STAIRS, AllBlocks.EXPOSED_COPPER_SHINGLE_STAIRS);
        builder.put(AllBlocks.EXPOSED_COPPER_SHINGLE_STAIRS, AllBlocks.WEATHERED_COPPER_SHINGLE_STAIRS);
        builder.put(AllBlocks.WEATHERED_COPPER_SHINGLE_STAIRS, AllBlocks.OXIDIZED_COPPER_SHINGLE_STAIRS);
        builder.put(AllBlocks.COPPER_TILES, AllBlocks.EXPOSED_COPPER_TILES);
        builder.put(AllBlocks.EXPOSED_COPPER_TILES, AllBlocks.WEATHERED_COPPER_TILES);
        builder.put(AllBlocks.WEATHERED_COPPER_TILES, AllBlocks.OXIDIZED_COPPER_TILES);
        builder.put(AllBlocks.COPPER_TILE_SLAB, AllBlocks.EXPOSED_COPPER_TILE_SLAB);
        builder.put(AllBlocks.EXPOSED_COPPER_TILE_SLAB, AllBlocks.WEATHERED_COPPER_TILE_SLAB);
        builder.put(AllBlocks.WEATHERED_COPPER_TILE_SLAB, AllBlocks.OXIDIZED_COPPER_TILE_SLAB);
        builder.put(AllBlocks.COPPER_TILE_STAIRS, AllBlocks.EXPOSED_COPPER_TILE_STAIRS);
        builder.put(AllBlocks.EXPOSED_COPPER_TILE_STAIRS, AllBlocks.WEATHERED_COPPER_TILE_STAIRS);
        builder.put(AllBlocks.WEATHERED_COPPER_TILE_STAIRS, AllBlocks.OXIDIZED_COPPER_TILE_STAIRS);
        cir.setReturnValue(builder.build());
    }
}
