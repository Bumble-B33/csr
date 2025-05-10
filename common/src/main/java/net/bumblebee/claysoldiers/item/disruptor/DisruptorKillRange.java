package net.bumblebee.claysoldiers.item.disruptor;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.ClayMobContainer;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.init.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class DisruptorKillRange {
    private static final int MAX_RANGE = 64;
    public static final Codec<DisruptorKillRange> CODEC = Codec.either(Codec.floatRange(1, MAX_RANGE), Codec.STRING).comapFlatMap(DisruptorKillRange::getFromEither, DisruptorKillRange::createEither);
    public static final StreamCodec<ByteBuf, DisruptorKillRange> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, d -> d.range,
            ByteBufCodecs.BOOL, d -> d.unlimited,
            DisruptorKillRange::new
    );

    private static final String UNLIMITED_RANGE = "unlimited";
    private final float range;
    private final boolean unlimited;

    private DisruptorKillRange(float range, boolean unlimited) {
        this.range = range;
        this.unlimited = unlimited;
    }

    public static DisruptorKillRange unlimited() {
        return new DisruptorKillRange(0, true);
    }
    public static DisruptorKillRange range(float range) {
        return new DisruptorKillRange(range, false);
    }

    public Component appendRangeToComponent(String rangedKey, String unlimitedKey) {
        return unlimited ? Component.translatable(rangedKey, Component.translatable(unlimitedKey)) : Component.translatable(rangedKey, range);
    }

    public List<? extends ClayMobEntity> getEntitiesInRange(ServerLevel level, BlockPos center) {
        List<? extends ClayMobEntity> clayMobs;
        if (unlimited) {
            clayMobs = level.getEntities(EntityTypeTest.forClass(ClayMobEntity.class), ClayMobEntity::canBeKilledByItem);
        } else if (range >= 1) {
            clayMobs = level.getEntitiesOfClass(ClayMobEntity.class, new AABB(center).inflate(range), ClayMobEntity::canBeKilledByItem);
        } else {
            clayMobs = List.of();
        }
        return clayMobs;
    }

    public List<ClayMobContainer> getClaySoldierContainers(ServerLevel level, BlockPos center) {
        int range = unlimited ? MAX_RANGE : Math.max(MAX_RANGE, (int) this.range);
        return level.getPoiManager().getInSquare(h -> h.is(ModTags.PoiTypes.SOLDIER_CONTAINER), center, range, PoiManager.Occupancy.IS_OCCUPIED)
                .map(p -> getClayMobContainer(level, p.getPos()))
                .filter(Objects::nonNull)
                .toList();
    }
    private static @Nullable ClayMobContainer getClayMobContainer(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ClayMobContainer clayMobContainer) {
            return clayMobContainer;
        }
        ClaySoldiersCommon.LOGGER.error("ClayMobContainerPoi({}) does not implement {}", pos.toShortString(), HamsterWheelBlockEntity.class.getSimpleName());
        return null;
    }

    private static DataResult<DisruptorKillRange> getFromEither(Either<Float, String> either) {
        if (either.left().isPresent()) {
            return DataResult.success(DisruptorKillRange.range(either.left().orElseThrow()));
        }
        String range = either.right().orElse("");
        if (range.equals(UNLIMITED_RANGE)) {
            return DataResult.success(DisruptorKillRange.unlimited());
        }
        return DataResult.error(() -> "Cannot parse range %s".formatted(range), new DisruptorKillRange(0, false));
    }

    private Either<Float, String> createEither() {
        if (unlimited) {
            return Either.right(UNLIMITED_RANGE);
        }
        return Either.left(range);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisruptorKillRange that = (DisruptorKillRange) o;
        return Float.compare(range, that.range) == 0 && unlimited == that.unlimited;
    }

    @Override
    public int hashCode() {
        return Objects.hash(range, unlimited);
    }
}
