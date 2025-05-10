package net.bumblebee.claysoldiers.capability;

import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public interface IBlockStorageAccess {
    /**
     * <p>
     * Inserts an ItemStack into the Storage and returns the remainder.
     * The ItemStack <em>should not</em> be modified in this function!
     * </p>
     *
     * @param stack ItemStack to insert. This must not be modified by the item handler.
     * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return an empty ItemStack).
     * May be the same as the input ItemStack if unchanged, otherwise a new ItemStack.
     * The returned ItemStack can be safely modified after.
     */
    ItemStack tryInserting(ItemStack stack);

    /**
     * Extracts an ItemStack matching the predicate.
     * <p>
     * The returned value must be empty if nothing is extracted,
     * otherwise its stack size must be less than or equal to {@code amount} and {@link ItemStack#getMaxStackSize()}.
     * </p>
     * @param stackPredicate the ItemStack to extract
     * @param amount Amount to extract (may be greater than the current stack's max limit)
     * @return ItemStack extracted from the slot, must be empty if nothing can be extracted.
     * The returned ItemStack can be safely modified after, so item handlers should return a new or copied stack.
     */
    ItemStack tryExtracting(Predicate<ItemStack> stackPredicate, int amount);

    /**
     * Iterates over every {@code ItemStack} in this Storage and test for wanted quantity.
     * If the quantity is more than 0, the {@code ItemStack} is extracted and handled.
     *
     * @param test returns the amount that is wanted of the given {@code ItemStack}
     * @param thenDo handles the extracted {@code ItemStack}
     * @param finished indicates that all wanted {@code ItemStacks} got extracted, called after each extraction
     */
    void forEach(ToIntFunction<ItemStack> test, Consumer<ItemStack> thenDo, BooleanSupplier finished);
}
