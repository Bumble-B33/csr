package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.ClaySoldierFabric;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.soldiercontainer.ClayMobContainer;
import net.bumblebee.claysoldiers.capability.*;
import net.bumblebee.claysoldiers.platform.services.AbstractCapabilityManger;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import team.reborn.energy.api.EnergyStorage;

import java.util.function.*;

public class FabricCapabilityManger extends AbstractCapabilityManger implements PreparableReloadListener {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "csr_capabilities");

    @Override
    public IBlockCache<IBlockStorageAccess> createStorageCache(ServerLevel level, BlockPos pos) {
        return new FabricCache<>(BlockApiCache.create(ItemStorage.SIDED, level, pos), FabricBlockStorageAccess::new, null);
    }

    @Override
    public IBlockCache<BlueprintRequestHandler> createBlueprintCache(ServerLevel level, BlockPos pos) {
        return FabricCache.of(BlockApiCache.create(ClaySoldierFabric.BLUEPRINT_REQUEST_HANDLER_LOOKUP, level, pos));
    }

    @Override
    public IBlockCache<AssignableWorksiteCapability> createPoiCache(ServerLevel level, BlockPos pos) {
        return FabricCache.of(BlockApiCache.create(ClaySoldierFabric.ASSIGNABLE_POI_LOOKUP, level, pos));
    }

    @Override
    public IBlockCache<EnergyCapability> createEnergyCache(ServerLevel level, BlockPos pos, Direction side) {
        return new FabricCache<>(BlockApiCache.create(EnergyStorage.SIDED, level, pos), s -> new EnergyCapability() {
            @Override
            public int insert(int amount) {
                int inserted;
                try (Transaction tx = Transaction.openOuter()) {
                    inserted = Math.toIntExact(s.insert(amount, tx));
                    tx.commit();
                }

                return inserted;
            }

            @Override
            public int extract(int wanted) {
                int extracted;
                try (Transaction tx = Transaction.openOuter()) {
                    extracted = Math.toIntExact(s.extract(wanted, tx));
                    tx.commit();
                }

                return extracted;
            }
        }, side);
    }

    @Override
    public ClayMobContainer getClayMobContainer(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        return ClaySoldierFabric.CLAY_MOB_CONTAINER_LOOKUP.find(level, pos, state, blockEntity, null);
    }

    private record FabricCache<T, C, D>(BlockApiCache<C, D> cache, Function<@NonNull C, T> mapper, D context) implements IBlockCache<T> {
        public static <T> FabricCache<T, T, Void> of(BlockApiCache<T, Void> cache) {
            return new FabricCache<>(cache, Function.identity(), null);
        }

        @Override
        public BlockPos pos() {
            return cache.getPos();
        }

        @Override
        public @Nullable T getCapability() {
            C res = cache.find(context);
            if (res == null) {
                return null;
            }
            return mapper.apply(res);
        }
    }

    private record FabricBlockStorageAccess(@NotNull Storage<ItemVariant> storage) implements IBlockStorageAccess {

        @Override
        public ItemStack tryInserting(ItemStack stack) {
            ItemVariant itemVariant = ItemVariant.of(stack);
            int maxCount = stack.getCount();
            try (Transaction transaction = Transaction.openOuter()) {
                long inserted = storage.insert(itemVariant, maxCount, transaction);
                transaction.commit();
                int remaining = (int) (maxCount - inserted);
                if (remaining <= 0) {
                    return ItemStack.EMPTY;
                }
                return stack.copyWithCount(remaining);
            }


        }

        @Override
        public ItemStack tryExtracting(Predicate<ItemStack> stackPredicate, int amountToExtract) {
            ItemVariant itemVariant = null;
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                itemVariant = view.getResource();
                if (stackPredicate.test(itemVariant.toStack())) {
                    break;
                } else {
                    itemVariant = null;
                }
            }
            if (itemVariant == null) {
                return ItemStack.EMPTY;
            }

            try (Transaction transaction = Transaction.openOuter()) {
                int amount = (int) storage.extract(itemVariant, amountToExtract, transaction);
                if (amount > 0) {
                    transaction.commit();
                    return itemVariant.toStack(amount);
                }
            }

            return ItemStack.EMPTY;
        }

        @Override
        public void forEach(ToIntFunction<ItemStack> test, Consumer<ItemStack> thenDo, BooleanSupplier finished) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                if (finished.getAsBoolean()) {
                    break;
                }
                var itemVariant = view.getResource();
                int amountWanted = test.applyAsInt(itemVariant.toStack());
                if (amountWanted <= 0) {
                    continue;
                }
                try (Transaction transaction = Transaction.openOuter()) {
                    int amount = (int) storage.extract(itemVariant, amountWanted, transaction);
                    if (amount > 0) {
                        transaction.commit();
                        thenDo.accept(itemVariant.toStack(amount));
                    }
                }
            }
        }
    }
}
