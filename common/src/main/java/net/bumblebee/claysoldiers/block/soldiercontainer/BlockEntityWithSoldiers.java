package net.bumblebee.claysoldiers.block.soldiercontainer;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class BlockEntityWithSoldiers extends BaseBlockEntityWithSoldier {
    private static final String SOLDIER_DATA_TAG = "SoldierData";
    private static final Codec<Stream<OccupantSoldierData.Compressed>> SOLDIER_CODEC = OccupantSoldierData.CODEC.listOf().xmap(List::stream, Stream::toList);
    private final ResourceKey<PoiType> poiKey;


    protected BlockEntityWithSoldiers(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState, ResourceKey<PoiType> poiKey, PoiType poiType) {
        MultiOccupantQueue queue = new MultiOccupantQueue(poiType.maxTickets(), worldPosition, new WalkAnimationState());
        super(type, worldPosition, blockState, queue, poiType.maxTickets(), queue.walkAnimation());
        this.poiKey = poiKey;
    }

    protected void updateOccupants(@Nullable Level level, UpdateOperation operation) {
        if (level instanceof ServerLevel serverLevel) {
            PoiManager poiManager = serverLevel.getPoiManager();
            if (poiManager.getType(worldPosition).isEmpty()) {
                return;
            }

            switch (operation) {
                case ADD -> poiManager.take(s -> s.is(poiKey), (_, p) -> p.equals(worldPosition), worldPosition, 1);
                case REMOVE -> serverLevel.getPoiManager().release(worldPosition);
                case UPDATE -> {
                    boolean run;
                    do {
                        run = poiManager.release(worldPosition);
                    } while (run);

                    for (int i = 0; i < queue.size(); i++) {
                        poiManager.take(s -> s.is(poiKey), (_, p) -> p.equals(worldPosition), worldPosition, 1);
                    }
                }
            }

        }
    }

    private record MultiOccupantQueue(Queue<OccupantSoldierData> queue, BlockPos worldPosition, WalkAnimationState walkAnimation) implements OccupantQueue {
        private MultiOccupantQueue(int maxSize, BlockPos worldPosition, WalkAnimationState state) {
            this(new ArrayDeque<>(maxSize), worldPosition, state);
        }

        @Override
        public void add(OccupantSoldierData data) {
            queue.add(data);
        }

        @Override
        public OccupantSoldierData removeSoldier() {
            return queue.remove();
        }

        @Override
        public int size() {
            return queue.size();
        }

        @Override
        public void clear() {
            queue.clear();
        }

        @Override
        public void saveAdditional(@NotNull ValueOutput tag, boolean client) {
            tag.store(SOLDIER_DATA_TAG, SOLDIER_CODEC, queue.stream().map(s -> s.compress(client)));
        }

        @Override
        public void loadAdditional(@NotNull ValueInput tag) {
            tag.read(SOLDIER_DATA_TAG, SOLDIER_CODEC).orElse(Stream.of()).map(s -> s.build(worldPosition, walkAnimation, tag.lookup()).orElse(null)).filter(Objects::nonNull).sorted().forEach(queue::add);
        }

        @Override
        public void forEach(Consumer<OccupantSoldierData> action) {
            queue.forEach(action);
        }

        @Override
        public Collection<OccupantSoldierData> killSoldiers(ServerLevel level, ServerPlayer player, Supplier<Vec3> exitPos) {
            var it = queue.iterator();
            List<OccupantSoldierData> killed = new ArrayList<>();

            while (it.hasNext()) {
                OccupantSoldierData soldier = it.next();

                if (soldier.canBeKilledBy(level, player)) {
                    killed.add(soldier);
                    it.remove();

                }
            }

            return killed;
        }
    }
}
