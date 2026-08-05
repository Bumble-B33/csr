package net.bumblebee.claysoldiers.block.soldiercontainer;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BlockEntityWithSingleSoldier extends BaseBlockEntityWithSoldier {
    private static final String SOLDIER_DATA_TAG = "SoldierData";
    private final ResourceKey<PoiType> poiKey;
    private final SingleClayMobHolder single;

    protected BlockEntityWithSingleSoldier(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState, ResourceKey<PoiType> poiKey, PoiType poiType) {
        var single = new SingleClayMobHolder(worldPosition, new WalkAnimationState());
        super(type, worldPosition, blockState, single, 1, single.walkAnimationState);
        this.poiKey = poiKey;
        this.single = single;
    }

    public @Nullable OccupantSoldierData getSoldierData() {
        return single.getSoldier();
    }

    @Override
    protected void updateOccupants(@Nullable Level level, UpdateOperation operation) {
        updateOccupants(level, single.soldierData);
    }

    private void updateOccupants(@Nullable Level level, @Nullable OccupantSoldierData soldierData) {
        if (level instanceof ServerLevel serverLevel) {
            if (soldierData != null) {
                serverLevel.getPoiManager().take(s -> s.is(poiKey), (h, p) -> p.equals(worldPosition), worldPosition, 1);
            } else {
                serverLevel.getPoiManager().getType(worldPosition).ifPresent(t -> {
                    serverLevel.getPoiManager().release(worldPosition);
                });
            }
        }
    }

    private static class SingleClayMobHolder implements OccupantQueue {
        private final BlockPos worldPos;
        private final WalkAnimationState walkAnimationState;
        @Nullable
        private OccupantSoldierData soldierData;

        public SingleClayMobHolder(BlockPos worldPos, WalkAnimationState walkAnimationState) {
            this.worldPos = worldPos;
            this.walkAnimationState = walkAnimationState;
        }

        @Nullable
        public OccupantSoldierData getSoldier() {
            return soldierData;
        }

        @Override
        public void add(OccupantSoldierData data) {
            this.soldierData = data;
        }

        @Override
        public OccupantSoldierData removeSoldier() {
            OccupantSoldierData data = this.soldierData;
            this.soldierData = null;
            return data;
        }

        @Override
        public int size() {
            return soldierData == null ? 0 : 1;
        }

        @Override
        public void clear() {
            soldierData = null;
        }

        @Override
        public void saveAdditional(@NotNull ValueOutput tag, boolean client) {
            if (soldierData != null) {
                tag.store(SOLDIER_DATA_TAG, OccupantSoldierData.CODEC, soldierData.compress(client));
            }
        }

        @Override
        public void loadAdditional(@NotNull ValueInput tag) {
            soldierData = tag.read(SOLDIER_DATA_TAG, OccupantSoldierData.CODEC).flatMap(s -> s.build(worldPos, walkAnimationState, tag.lookup())).orElse(null);
        }

        @Override
        public void forEach(Consumer<OccupantSoldierData> action) {
            if (soldierData != null) {
                action.accept(soldierData);
            }
        }

        @Override
        public Collection<OccupantSoldierData> killSoldiers(ServerLevel level, ServerPlayer player, Supplier<Vec3> exitPos) {
            if (soldierData == null || !soldierData.canBeKilledBy(level, player)) {
                return List.of();
            }
            var list = Collections.singleton(soldierData);
            soldierData = null;
            return list;
        }
    }
}
