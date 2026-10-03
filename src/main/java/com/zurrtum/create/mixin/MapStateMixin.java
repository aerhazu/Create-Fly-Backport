package com.zurrtum.create.mixin;

import com.google.common.collect.Maps;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.station.StationMapData;
import com.zurrtum.create.content.trains.station.StationMarker;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import java.util.*;

@Mixin(MapState.class)
public abstract class MapStateMixin implements StationMapData {
    @Shadow
    @Final
    public int centerX;

    @Shadow
    @Final
    public int centerZ;

    @Shadow
    @Final
    public byte scale;

    @Shadow
    @Final
    Map<String, MapDecoration> decorations;

    @Shadow
    private int decorationCount;

    @Unique
    private final Map<String, StationMarker> create$stationMarkers = Maps.newHashMap();

    // 1.21.1's MapState persists via classic writeNbt/fromNbt rather than a Codec<MapState> field,
    // so station markers are spliced into the NBT directly instead of via a wrapper Codec.
    @Inject(method = "writeNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/nbt/NbtCompound;", at = @At("RETURN"))
    private void create$writeStationMarkers(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfoReturnable<NbtCompound> cir) {
        if (create$stationMarkers.isEmpty()) {
            return;
        }
        NbtElement encoded = StationMarker.LIST_CODEC.encodeStart(NbtOps.INSTANCE, List.copyOf(create$stationMarkers.values())).getOrThrow();
        cir.getReturnValue().put(StationMarker.STATION_MARKERS_KEY, encoded);
    }

    @Inject(method = "fromNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/map/MapState;", at = @At("RETURN"))
    private static void create$readStationMarkers(
        NbtCompound nbt,
        RegistryWrapper.WrapperLookup registryLookup,
        CallbackInfoReturnable<MapState> cir
    ) {
        MapState state = cir.getReturnValue();
        if (state == null || !nbt.contains(StationMarker.STATION_MARKERS_KEY)) {
            return;
        }
        StationMarker.LIST_CODEC.parse(NbtOps.INSTANCE, nbt.get(StationMarker.STATION_MARKERS_KEY))
            .result()
            .ifPresent(list -> list.forEach(((StationMapData) state)::create$addStationMarker));
    }

    @Override
    public Map<String, StationMarker> create$getStationMarkers() {
        return create$stationMarkers;
    }

    @Override
    public void create$addStationMarker(StationMarker marker) {
        create$stationMarkers.put(marker.getId(), marker);

        int scaleMultiplier = 1 << scale;
        float localX = (marker.getTarget().getX() - centerX) / (float) scaleMultiplier;
        float localZ = (marker.getTarget().getZ() - centerZ) / (float) scaleMultiplier;

        if (localX < -63.0F || localX > 63.0F || localZ < -63.0F || localZ > 63.0F) {
            this.removeDecoration(marker.getId());
            return;
        }

        byte localXByte = (byte) (int) (localX * 2.0F + 0.5F);
        byte localZByte = (byte) (int) (localZ * 2.0F + 0.5F);

        MapDecoration decoration = StationMarker.createStationDecoration(localXByte, localZByte, Optional.of(marker.getName()));
        MapDecoration oldDecoration = decorations.put(marker.getId(), decoration);
        if (!decoration.equals(oldDecoration)) {
            if (oldDecoration != null && oldDecoration.type().value().trackCount()) {
                --decorationCount;
            }

            if (decoration.type().value().trackCount()) {
                ++decorationCount;
            }

            markDecorationsDirty();
        }
    }

    @Shadow
    protected abstract void removeDecoration(String id);

    @Shadow
    protected abstract void markDecorationsDirty();

    @Shadow
    public abstract boolean decorationCountNotLessThan(int trackedCount);

    @Override
    public boolean create$toggleStation(WorldAccess level, BlockPos pos, StationBlockEntity stationBlockEntity) {
        double xCenter = pos.getX() + 0.5D;
        double zCenter = pos.getZ() + 0.5D;
        int scaleMultiplier = 1 << scale;

        double localX = (xCenter - (double) centerX) / (double) scaleMultiplier;
        double localZ = (zCenter - (double) centerZ) / (double) scaleMultiplier;

        if (localX < -63.0D || localX > 63.0D || localZ < -63.0D || localZ > 63.0D)
            return false;

        StationMarker marker = StationMarker.fromWorld(level, pos);
        if (marker == null)
            return false;

        if (create$stationMarkers.remove(marker.getId(), marker)) {
            removeDecoration(marker.getId());
            return true;
        }

        if (!decorationCountNotLessThan(256)) {
            create$addStationMarker(marker);
            return true;
        }

        return false;
    }

    @Inject(method = "removeBanner(Lnet/minecraft/world/BlockView;II)V", at = @At("RETURN"))
    public void create$onCheckBanners(BlockView blockGetter, int x, int z, CallbackInfo ci) {
        create$checkStations(blockGetter, x, z);
    }

    @Unique
    private void create$checkStations(BlockView blockGetter, int x, int z) {
        Iterator<StationMarker> iterator = create$stationMarkers.values().iterator();
        List<StationMarker> newMarkers = new ArrayList<>();

        while (iterator.hasNext()) {
            StationMarker marker = iterator.next();
            if (marker.getTarget().getX() == x && marker.getTarget().getZ() == z) {
                StationMarker other = StationMarker.fromWorld(blockGetter, marker.getSource());
                if (!marker.equals(other)) {
                    iterator.remove();
                    removeDecoration(marker.getId());

                    if (other != null && marker.getTarget().equals(other.getTarget())) {
                        newMarkers.add(other);
                    }
                }
            }
        }

        for (StationMarker marker : newMarkers) {
            create$addStationMarker(marker);
        }
    }
}
