package net.bumblebee.claysoldiers.item.claymobspawn;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.variant.NameableVariant;
import net.bumblebee.claysoldiers.entity.common.variant.VariantHolder;
import net.bumblebee.claysoldiers.init.ModCritirions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

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
    private static final DispenseBehavior INSTANCE = new DispenseBehavior();

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
            public @NonNull EntityType<T> getType() {
                return entityType.get();
            }

            @Override
            public Consumer<T> modifyBeforeSpawn(ItemStack stack, ServerLevel level, @Nullable Player player) {
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
     * @param count the amountRequired to spawn
     * @return the amountRequired spawned
     */
    public int spawnWithCount(ItemStack doll, UseOnContext context, int count) {
        return spawnWithCount(doll, context.getLevel(), context.getPlayer(), context.getClickedPos(), context.getClickedFace(), count);
    }

    private int spawnWithCount(ItemStack doll,
                               Level level,
                               @Nullable Player player,
                               BlockPos blockpos,
                                Direction direction,
                               int count) {
        if (!isValid(doll, level, player) || count <= 0) {
            return -1;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        } else {
            BlockState blockstate = level.getBlockState(blockpos);
            BlockEntity blockEntity = level.getBlockEntity(blockpos);
            if (blockEntity instanceof Spawner spawner) {
                spawner.setEntityId(this.getType(), level.getRandom());
                level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockpos);
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
                            serverLevel,
                            modifyBeforeSpawn(doll.copyWithCount(1), serverLevel, player),
                            updatedBlockPos,
                            EntitySpawnReason.SPAWN_ITEM_USE,
                            true,
                            !Objects.equals(blockpos, updatedBlockPos) && direction == Direction.UP) != null) {
                        level.gameEvent(player, GameEvent.ENTITY_PLACE, blockpos);
                    }
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    ModCritirions.MULTI_SPAWN_ITEM_USE_TRIGGER.get().trigger(serverPlayer, getType(), count);
                }
            }
            return count;
        }
    }



    /**
     * Called before the {@link T Entity} is spawned.
     * @return whether the {@link T Entity} can be spawned
     */
    protected boolean isValid(ItemStack stack, Level level, @Nullable Player player) {
        return true;
    }

    /**
     * @return the {@link EntityType<T> EntityType} to spawn
     */
    public abstract @NotNull EntityType<T> getType();

    /**
     * The {@link T Entity} is modified with the returned consumer
     * @param stack the stack that is spawning the entity
     * @return the consumer to modify the entity with
     */
    public abstract Consumer<T> modifyBeforeSpawn(ItemStack stack, ServerLevel level, Player player);

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

    public static <T extends Entity> void registerDispenseBehavior(MultiSpawnItem<T> item) {
        DispenserBlock.registerBehavior(item, INSTANCE);
    }

    private static class DispenseBehavior extends DefaultDispenseItemBehavior {
        public ItemStack execute(BlockSource source, ItemStack dispensed) {
            Direction direction = source.state().getValue(DispenserBlock.FACING);

            if (dispensed.getItem() instanceof MultiSpawnItem<?> multiSpawnItem) {
                try {
                    multiSpawnItem.spawnWithCount(dispensed, source.level(), null, source.pos(), direction, 1);
                } catch (Exception e) {
                    ClaySoldiersCommon.ERROR_HANDLER.warn("Error while dispensing Multi Spawn Item from dispenser at %s. Error: %s".formatted(source.pos(), e.getMessage()));
                    return ItemStack.EMPTY;
                }

                dispensed.shrink(1);
                source.level().gameEvent(null, GameEvent.ENTITY_PLACE, source.pos());
            }

            return dispensed;
        }
    }
}
