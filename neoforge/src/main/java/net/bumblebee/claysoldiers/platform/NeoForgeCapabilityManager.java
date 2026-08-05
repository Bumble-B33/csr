package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.block.soldiercontainer.ClayMobContainer;
import net.bumblebee.claysoldiers.capability.*;
import net.bumblebee.claysoldiers.init.ModNeoForgeCapabilities;
import net.bumblebee.claysoldiers.platform.services.AbstractCapabilityManger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.*;

public class NeoForgeCapabilityManager extends AbstractCapabilityManger {
    @ApiStatus.Internal
    public NeoForgeCapabilityManager() {
    }

    @Override
    public IBlockCache<IBlockStorageAccess> createStorageCache(ServerLevel level, BlockPos pos) {
        return new NeoForgeCache<>(BlockCapabilityCache.create(Capabilities.Item.BLOCK, level, pos, null), NeoForgeBlockStorageAccess::new);
    }

    @Override
    public IBlockCache<BlueprintRequestHandler> createBlueprintCache(ServerLevel level, BlockPos pos) {
        return NeoForgeCache.of(BlockCapabilityCache.create(ModNeoForgeCapabilities.BLUEPRINT_REQUEST_CAP, level, pos, null));
    }

    @Override
    public IBlockCache<AssignableWorksiteCapability> createPoiCache(ServerLevel level, BlockPos pos) {
        return NeoForgeCache.of(BlockCapabilityCache.create(ModNeoForgeCapabilities.ASSIGNABLE_POI_CAP, level, pos, null));
    }


    @Override
    public IBlockCache<EnergyCapability> createEnergyCache(ServerLevel level, BlockPos pos, Direction side) {
        return new NeoForgeCache<>(BlockCapabilityCache.create(Capabilities.Energy.BLOCK, level, pos, side), EnergyWrapper::new);
    }

    @Override
    public @Nullable ClayMobContainer getClayMobContainer(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        return level.getCapability(ModNeoForgeCapabilities.CLAY_MOB_CONTAINER, pos, state, blockEntity, null);
    }

    private record NeoForgeCache<C, T>(BlockCapabilityCache<C, ?> cache, Function<@NonNull C, T> mapper) implements IBlockCache<T> {
        public static <T> NeoForgeCache<T, T> of(BlockCapabilityCache<T, ?> cache) {
            return new NeoForgeCache<>(cache, Function.identity());
        }

        @Override
        public BlockPos pos() {
            return cache.pos();
        }

        @Override
        public @Nullable T getCapability() {
            var cap = cache.getCapability();
            if (cap == null) {
                return null;
            }

            return mapper.apply(cache.getCapability());
        }
    }

    private record NeoForgeBlockStorageAccess(@NonNull ResourceHandler<ItemResource> itemHandler) implements IBlockStorageAccess {

            @Override
            public ItemStack tryInserting(ItemStack stack) {
                var stackToInsert = stack.copy();
                for (int i = 0; i < itemHandler.size(); i++) {
                    stackToInsert = ItemUtil.insertItemReturnRemaining(itemHandler, i, stackToInsert, false, null);
                    if (stackToInsert.isEmpty()) {
                        break;
                    }
                }
                return stackToInsert;
            }

            @Override
            public ItemStack tryExtracting(Predicate<ItemStack> stackPredicate, int amount) {
                ItemStack stack = ItemStack.EMPTY;
                for (int i = 0; i < itemHandler.size(); i++) {
                    var resource = itemHandler.getResource(i);
                    if (stackPredicate.test(resource.toStack())) {
                        try (var tx = Transaction.openRoot()) {
                            int extracted = itemHandler.extract(i, resource, amount, tx);
                            tx.commit();
                            return resource.toStack(extracted);
                        }
                    }
                }
                return stack;
            }

            @Override
            public void forEach(ToIntFunction<ItemStack> test, Consumer<ItemStack> thenDo, BooleanSupplier finished) {
                for (int i = 0; i < itemHandler.size(); i++) {
                    if (finished.getAsBoolean()) {
                        break;
                    }
                    var resource = itemHandler.getResource(i);
                    int amountWanted = test.applyAsInt(resource.toStack());
                    if (amountWanted <= 0) {
                        continue;
                    }
                    try (var tx = Transaction.openRoot()) {
                        int extracted = itemHandler.extract(i, resource, amountWanted, tx);
                        tx.commit();
                        thenDo.accept(resource.toStack(extracted));
                    }
                }
            }

            @Override
            public String toString() {
                return "SA{" + itemHandler + '}';
            }
        }

    public record EnergyWrapper(@NonNull EnergyHandler energyHandler) implements EnergyCapability {
        @Override
        public int insert(int amount) {
            int inserted;
            try (Transaction tx = Transaction.openRoot()) {
                inserted = energyHandler.insert(amount, tx);
                tx.commit();
            }
            return inserted;
        }

        @Override
        public int extract(int wanted) {
            int extracted;
            try (Transaction tx = Transaction.openRoot()) {
                extracted = energyHandler.extract(wanted, tx);
                tx.commit();
            }
            return extracted;
        }
    }
}
