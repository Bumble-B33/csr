package net.bumblebee.claysoldiers.soldieritemtypes;

import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ItemGenerator {
    NonNullList<ItemStack> EMPTY_LIST = NonNullList.withSize(0, ItemStack.EMPTY);
    ItemGenerator EMPTY = new ItemGenerator() {
        @Override
        public Limit limitedBy() {
            return Limit.ZERO;
        }

        @Override
        public NonNullList<ItemStack> generateForTag(List<WeightedItem> available, int count, RandomSource random) {
            return EMPTY_LIST;
        }
    };


    default NonNullList<ItemStack> generate(List<WeightedItem> available, int count, RandomSource random, List<Generator> all) {
        return generateForTag(available, count, random);
    }

    /**
     * @return the generation limit for {@link #generateForTag}.
     */
    Limit limitedBy();

    NonNullList<ItemStack> generateForTag(List<WeightedItem> available, int count, RandomSource random);
    
    enum Limit {
        /**
         * The result is limited by the size of the available items.
         */
        SIZE,
        /**
         * The result is limited by the amountRequired requested
         */
        COUNT,
        /**
         * The result is always 0.
         */
        ZERO
    }
}
