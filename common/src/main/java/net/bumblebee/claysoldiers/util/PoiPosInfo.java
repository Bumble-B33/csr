package net.bumblebee.claysoldiers.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class PoiPosInfo {
    public static final String EMPTY_LANG = "poi_info.empty";
    public static final String POS_LANG = "poi_info.with_pos";
    public static final String POS_AND_SIDE_LANG = "poi_info.with_pos_side";
    public static final String TOP_LANG = "poi_info.direction.up";
    public static final String BOTTOM_LANG = "poi_info.direction.down";
    public static final String NORTH_LANG = "poi_info.direction.north";
    public static final String EAST_LANG = "poi_info.direction.east";
    public static final String SOUTH_LANG = "poi_info.direction.south";
    public static final String WEST_LANG = "poi_info.direction.west";

    public static final PoiPosInfo EMPTY = new PoiPosInfo(null, null);
    public static final Codec<PoiPosInfo> CODEC = RecordCodecBuilder.create(in -> in.group(
            BlockPos.CODEC.optionalFieldOf("pos").forGetter(PoiPosInfo::pos),
            Direction.CODEC.optionalFieldOf("side").forGetter(PoiPosInfo::direction)
    ).apply(in, (p, d) -> PoiPosInfo.create(p.orElse(null), d.orElse(null))));
    public static final StreamCodec<ByteBuf, PoiPosInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), PoiPosInfo::pos,
            ByteBufCodecs.optional(Direction.STREAM_CODEC), PoiPosInfo::direction,
            (p, d) -> PoiPosInfo.create(p.orElse(null), d.orElse(null))
    );


    private final @Nullable BlockPos pos;
    private final @Nullable Direction direction;

    private PoiPosInfo(@Nullable BlockPos pos, @Nullable Direction direction) {
        this.pos = pos;
        this.direction = direction;
    }

    public static PoiPosInfo create(@Nullable BlockPos pos, @Nullable Direction direction) {
        if (pos == null) {
            return EMPTY;
        }
        return new PoiPosInfo(pos, direction);
    }

    public boolean isEmpty() {
        return pos == null;
    }

    public @Nullable BlockPos getPos() {
        return pos;
    }

    public @Nullable Direction side() {
        return direction;
    }

    private Optional<BlockPos> pos() {
        return Optional.ofNullable(pos);
    }

    private Optional<Direction> direction() {
        return Optional.ofNullable(direction);
    }

    public Component shortDisplayName() {
        if (pos == null) {
            return Component.translatable(EMPTY_LANG);
        }
        if (direction == null) {
            return Component.translatable(POS_LANG, pos.toShortString());
        }
        return Component.translatable(POS_AND_SIDE_LANG, pos.toShortString(), getSideDisplayName(direction));
    }

    private static Component getSideDisplayName(Direction side) {
        return switch (side) {
            case UP -> Component.translatable(TOP_LANG);
            case DOWN -> Component.translatable(BOTTOM_LANG);
            case NORTH -> Component.translatable(NORTH_LANG);
            case SOUTH -> Component.translatable(SOUTH_LANG);
            case WEST -> Component.translatable(WEST_LANG);
            case EAST -> Component.translatable(EAST_LANG);
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PoiPosInfo) obj;
        return Objects.equals(this.pos, that.pos) &&
                Objects.equals(this.direction, that.direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, direction);
    }

    @Override
    public String toString() {
        if (pos == null) {
            return "PoiPos[Empty]";
        }

        return "PoiPos[%s, %s]".formatted(pos.toShortString(), direction);
    }

}
