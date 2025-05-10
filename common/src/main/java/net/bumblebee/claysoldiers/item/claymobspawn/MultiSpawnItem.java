package net.bumblebee.claysoldiers.item.claymobspawn;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.variant.NameableVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An item class similar to the {@link net.minecraft.world.item.SpawnEggItem},
 * whoever instead of spawning 1 {@link T Entity}, it will spawn multiple,
 * depending on the size of the stack
 * @param <T> the Entity to spawn
 */
public abstract class MultiSpawnItem<T extends Entity> extends Item {
    public MultiSpawnItem(Properties pProperties) {
        super(pProperties);
    }

    /**
     * Creates a new {@link MultiSpawnItem} with the given entity type and variant.
     *
     * @param entityType the entity type of the entity to spawn
     * @param variant the variant of the entity
     */
    public static <V extends NameableVariant, T extends ClayMobEntity & VariantHolder<V>> MultiSpawnItem<T> createClayMob(Supplier<EntityType<T>> entityType, V variant, Properties properties) {
        return new MultiSpawnItem<>(properties) {

            @Override
            public EntityType<T> getType() {
                return entityType.get();
            }

            @Override
            public Consumer<T> modifyBeforeSpawn(ItemStack stack) {
                return clayMob -> {
                    clayMob.setSpawnedFrom(stack, true);
                    clayMob.setVariant(variant);
                };
            }

            @Override
            public int getPouchColor(DataComponentMap data, LivingEntity viewing) {
                return variant.getPouchColor();
            }
        };
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        Player player = pContext.getPlayer();
        int stackCount = pContext.getItemInHand().getCount();
        if (player !=  null && player.isShiftKeyDown()) {
            stackCount = 1;
        }
        int res = spawnWithCount(pContext.getItemInHand(), pContext, stackCount);
        if (res > 0) {
            pContext.getItemInHand().shrink(res);
            return InteractionResult.CONSUME;
        }
        return res == -1 ? InteractionResult.FAIL : InteractionResult.SUCCESS;
    }

    /**
     * Spawns multiple {@link T Entities}.
     * @param doll the {@code ItemStack} from which the entities should be spawned.
     * @param count the amount to spawn
     * @return the amount spawned
     */
    public int spawnWithCount(ItemStack doll, UseOnContext pContext, int count) {
        if (!isValid(doll, pContext.getLevel(), pContext.getPlayer()) || count <= 0) {
            return -1;
        }
        Level level = pContext.getLevel();
        if (!(level instanceof ServerLevel)) {
            return 0;
        } else {
            BlockPos blockpos = pContext.getClickedPos();
            Direction direction = pContext.getClickedFace();
            BlockState blockstate = level.getBlockState(blockpos);
            BlockEntity blockEntity = level.getBlockEntity(blockpos);
            if (blockEntity instanceof Spawner spawner) {
                spawner.setEntityId(this.getType(), level.getRandom());
                level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
                level.gameEvent(pContext.getPlayer(), GameEvent.BLOCK_CHANGE, blockpos);
                return 1;

            } else {
                BlockPos updatedBlockPos;
                if (blockstate.getCollisionShape(level, blockpos).isEmpty()) {
                    updatedBlockPos = blockpos;
                } else {
                    updatedBlockPos = blockpos.relative(direction);
                }

                EntityType<T> entitytype = this.getType();

                for (int i = 0; i < count;i++) {
                    if (entitytype.spawn(
                            (ServerLevel) level,
                            modifyBeforeSpawn(doll.copyWithCount(1)),
                            updatedBlockPos,
                            MobSpawnType.SPAWN_EGG,
                            true,
                            !Objects.equals(blockpos, updatedBlockPos) && direction == Direction.UP) != null) {
                        level.gameEvent(pContext.getPlayer(), GameEvent.ENTITY_PLACE, blockpos);
                    }
                }

            }
            return count;
        }
    }

    protected boolean isValid(ItemStack stack, Level level, @Nullable Player player) {
        return true;
    }

    /**
     * @return the {@link EntityType<T> EntityType} to spawn
     */
    public abstract EntityType<T> getType();

    /**
     * The {@link T Entity} is modified with the returned consumer
     * @param stack the stack that is spawning the entity
     * @return the consumer to modify the entity with
     */
    public abstract Consumer<T> modifyBeforeSpawn(ItemStack stack);

    /**
     * Retrieves the DataComponents needed for spawning this {@link T Entity}
     * and recreating an {@code ItemStack} when extracted from the Clay Pouch
     */
    public DataComponentMap requiredForPouch(ItemStack stack) {
        return DataComponentMap.EMPTY;
    }

    /**
     * Returns the Color the Clay Pouch should have with this {@link T Entity}.
     * @param data DataComponents retrieved by {@link #requiredForPouch}
     */
    public abstract int getPouchColor(DataComponentMap data, LivingEntity viewing);

    /**
     * Recreates this as an {@code ItemStack} after being extracted from a Clay Pouch.
     * @param data  DataComponents retrieved by {@link #requiredForPouch}
     */
    public ItemStack recreateStackFromPouch(DataComponentMap data, HolderLookup.Provider registries) {
        return getDefaultInstance();
    }
}
