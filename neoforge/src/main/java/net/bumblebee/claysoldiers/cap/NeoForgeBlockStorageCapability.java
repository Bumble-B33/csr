package net.bumblebee.claysoldiers.cap;

import net.bumblebee.claysoldiers.capability.IBlockCache;
import net.bumblebee.claysoldiers.capability.IBlockStorageAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class NeoForgeBlockStorageCapability implements IBlockCache<IBlockStorageAccess> {
    private final BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> cache;

    public NeoForgeBlockStorageCapability(BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> cache) {
        this.cache = cache;
    }

    @Override
    public BlockPos pos() {
        return cache.pos();
    }

    @Override
    public IBlockStorageAccess getCapability() {
        var itemHandler = cache.getCapability();
        return itemHandler == null ? null : new NeoForgeBlockStorageAccess(itemHandler);
    }

    public static class NeoForgeBlockStorageAccess implements IBlockStorageAccess {
        private final ResourceHandler<ItemResource> itemHandler;

        public NeoForgeBlockStorageAccess(ResourceHandler<ItemResource> iItemHandler) {
            this.itemHandler = iItemHandler;
        }

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
    }
}
