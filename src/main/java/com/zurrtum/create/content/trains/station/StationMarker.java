package com.zurrtum.create.content.trains.station;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllMapDecorationTypes;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class StationMarker {
    public static final Codec<StationMarker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("source").forGetter(StationMarker::getSource),
        BlockPos.CODEC.fieldOf("target").forGetter(StationMarker::getTarget),
        TextCodecs.CODEC.fieldOf("name").forGetter(StationMarker::getName)
    ).apply(instance, StationMarker::new));
    public static final Codec<List<StationMarker>> LIST_CODEC = CODEC.listOf();

    private final BlockPos source;
    private final BlockPos target;
    private final Text name;
    private final String id;

    public StationMarker(BlockPos source, BlockPos target, Text name) {
        this.source = source;
        this.target = target;
        this.name = name;
        id = "create:station-" + target.getX() + "," + target.getY() + "," + target.getZ();
    }

    public static StationMarker fromWorld(BlockView level, BlockPos pos) {
        Optional<StationBlockEntity> stationOption = level.getBlockEntity(pos, AllBlockEntityTypes.TRACK_STATION);

        if (stationOption.isEmpty() || stationOption.get().getStation() == null)
            return null;

        String name = stationOption.get().getStation().name;
        return new StationMarker(
            pos,
            BlockEntityBehaviour.get(stationOption.get(), TrackTargetingBehaviour.TYPE).getPositionForMapMarker(),
            Text.literal(name)
        );
    }

    public BlockPos getSource() {
        return source;
    }

    public BlockPos getTarget() {
        return target;
    }

    public Text getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StationMarker that = (StationMarker) o;

        if (!target.equals(that.target))
            return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, name);
    }

    public static MapDecoration createStationDecoration(byte x, byte y, Optional<Text> name) {
        return new MapDecoration(AllMapDecorationTypes.STATION_MAP_DECORATION, x, y, (byte) 0, name);
    }

    // 1.21.1's MapState persists via classic writeNbt/fromNbt, not a Codec<MapState> field, so
    // station markers are spliced into the NBT directly by MapStateMixin instead of via a wrapper Codec.
    public static final String STATION_MARKERS_KEY = "create:stations";
}
