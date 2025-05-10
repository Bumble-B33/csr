package net.bumblebee.claysoldiers.platform;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface ItemLikeSupplier<T extends ItemLike> extends Supplier<T>, ItemLike {
    ItemLikeSupplier<Item> EMPTY = new ItemLikeSupplier<>() {
        @Override
        public Item get() {
            return null;
        }

        @Override
        public boolean is(ItemStack stack) {
            return false;
        }
    };

    @Override
    T get();

    /**
     * @return the stored object as an {@code Item}.
     */
    @Override
    default @NotNull Item asItem() {
        return get().asItem();
    }

    /**
     * Returns whether the given stack is for the {@link #asItem() item}.
     * @param stack the stack to test
     * @return whether the given stack is for the {@link #asItem() item}.
     */
    default boolean is(ItemStack stack) {
        return stack.is(asItem());
    }

    /**
     * Creates a new {@code ItemLikeSupplier} from the give {@code Supplier}
     */
    static <T extends ItemLike> ItemLikeSupplier<T> create(Supplier<T> supplier) {
        return supplier::get;
    }
}
