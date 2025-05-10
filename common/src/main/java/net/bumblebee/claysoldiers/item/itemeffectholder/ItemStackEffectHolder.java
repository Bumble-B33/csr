package net.bumblebee.claysoldiers.item.itemeffectholder;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A container class of an ItemStack with an effect
 * @param <P> the effect
 */
public abstract class ItemStackEffectHolder<P> {
    protected final ItemStack stack;
    @Nullable
    protected final P effect;

    public ItemStackEffectHolder(ItemStack stack, @Nullable P effect) {
        this.stack = stack;
        this.effect = effect;
    }
    public ItemStackEffectHolder(ItemStack stack) {
        this.stack = stack;
        this.effect = createEffectOnInitialisation(stack);
    }

    /**
     * Gets the effect of the {@code ItemStack} for initialisation.
     * @return the effect of the {@code ItemStack}
     */
    protected abstract P createEffectOnInitialisation(ItemStack stack);

    /**
     * Returns the effect of the {@code ItemStack}.
     * @return the effect of the {@code ItemStack}
     */
    public P effect() {
        return effect;
    }

    /**
     * Returns {@code ItemStack} this container holds.
     * @return {@code ItemStack} this container holds
     */
    public ItemStack stack() {
        return stack;
    }
    public boolean is(Item item) {
        return stack.is(item);
    }
    public boolean isEmpty() {
        return stack.isEmpty();
    }
    public int shrink(int count) {
        stack.shrink(count);
        return stack.getCount();
    }
    public int getCount() {
        return stack.getCount();
    }
    public void setCount(int count) {
        stack.setCount(count);
    }

    /**
     * Save this {@code ItemStackEffectHolder} to a {@code Tag}.
     * @return the {@code Tag} this is saved to
     */
    public Tag save(HolderLookup.Provider provider) {
        return stack.save(provider);
    }

    /**
     * Creates a new {@code ItemStack} from the given {@code CompoundTag}
     */
    public static<S extends ItemStackEffectHolder<?>> S parseOptional(Function<ItemStack, S> builder, HolderLookup.Provider provider, CompoundTag pCompoundTag) {
        return builder.apply(ItemStack.parseOptional(provider, pCompoundTag));
    }
    /**
     * Compares both {@code ItemStacks}, returns {@code true} if both {@code ItemStacks} are equal.
     * Does not check whether the effect ist the same
     */
    public static <T> boolean matches(ItemStackEffectHolder<T> stack, ItemStackEffectHolder<T> other) {
        return ItemStack.matches(stack.stack, other.stack);
    }
}
