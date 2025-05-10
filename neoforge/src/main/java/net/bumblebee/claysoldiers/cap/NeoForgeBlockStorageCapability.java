package net.bumblebee.claysoldiers.cap;

import net.bumblebee.claysoldiers.capability.IBlockCache;
import net.bumblebee.claysoldiers.capability.IBlockStorageAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class NeoForgeBlockStorageCapability implements IBlockCache<IBlockStorageAccess> {
    private final BlockCapabilityCache<IItemHandler, Direction> cache;

    public NeoForgeBlockStorageCapability(BlockCapabilityCache<IItemHandler, Direction> cache) {
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
        private final IItemHandler itemHandler;

        public NeoForgeBlockStorageAccess(IItemHandler iItemHandler) {
            this.itemHandler = iItemHandler;
        }

        @Override
        public ItemStack tryInserting(ItemStack stack) {
            var stackToInsert = stack.copy();
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                stackToInsert = itemHandler.insertItem(i, stackToInsert, false);
                if (stackToInsert.isEmpty()) {
                    break;
                }
            }
            return stackToInsert;
        }

        @Override
        public ItemStack tryExtracting(Predicate<ItemStack> stackPredicate, int amount) {
            ItemStack stack = ItemStack.EMPTY;
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                if (stackPredicate.test(itemHandler.getStackInSlot(i))) {
                    stack = itemHandler.extractItem(i, amount, false);
                    if (!stack.isEmpty()) {
                        return stack;
                    }

                }
            }

            return stack;
        }

        @Override
        public void forEach(ToIntFunction<ItemStack> test, Consumer<ItemStack> thenDo, BooleanSupplier finished) {
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                if (finished.getAsBoolean()) {
                    break;
                }
                int amountWanted = test.applyAsInt(itemHandler.getStackInSlot(i));
                if (amountWanted <= 0) {
                    continue;
                }
                ItemStack stack = itemHandler.extractItem(i, amountWanted, false);
                if (!stack.isEmpty()) {
                    thenDo.accept(stack);
                }
            }
        }
    }
}
