package net.bumblebee.claysoldiers.capability;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class FabricBlockStorageCache implements IBlockCache<IBlockStorageAccess> {
    private final BlockApiCache<Storage<ItemVariant>, @Nullable Direction> cache;

    public FabricBlockStorageCache(ServerLevel level, BlockPos pos) {
        this.cache = BlockApiCache.create(ItemStorage.SIDED, level, pos);
    }

    @Override
    public BlockPos pos() {
        return cache.getPos();
    }

    @Override
    public @Nullable IBlockStorageAccess getCapability() {
        Storage<ItemVariant> views = cache.find(null);
        if (views == null) {
            return null;
        }
        return new FabricBlockStorageAccess(views);
    }

    public record FabricBlockStorageAccess(@NotNull Storage<ItemVariant> storage) implements IBlockStorageAccess {

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
