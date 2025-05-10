package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.soldieritemtypes.Generator;
import net.bumblebee.claysoldiers.soldieritemtypes.ItemGenerator;
import net.bumblebee.claysoldiers.soldieritemtypes.WeightedItem;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class ModItemGenerators {
    private static final NonNullList<ItemStack> EMPTY = NonNullList.withSize(0, ItemStack.EMPTY);
    private static final ItemGenerator DEFAULT_GENERATOR = new ItemGenerator() {
        @Override
        public Limit limitedBy() {
            return Limit.COUNT;
        }

        @Override
        public String toString() {
            return "Default";
        }

        @Override
        public NonNullList<ItemStack> generateForTag(List<WeightedItem> available, int count, RandomSource random) {
            if (available.isEmpty()) {
                return NonNullList.create();
            }

            if (available.size() == 1) {
                NonNullList<ItemStack> selectedItems = NonNullList.create();

                addItemStackToList(selectedItems, available.getFirst().getItem(), count);
                return selectedItems;
            }

            int i = 0;
            int pickedItems = 0;
            int size = available.size();

            Map<Item, Integer> amountMap = new HashMap<>(Math.min(count, size));

            while (pickedItems < count) {
                i = (i + 1) % size;
                if (appendToMap(amountMap, available.get(i), random)) {
                    pickedItems++;
                }
            }

            NonNullList<ItemStack> itemAmountAdjusted = NonNullList.createWithCapacity(amountMap.size());
            amountMap.forEach((item, amount) -> addItemStackToList(itemAmountAdjusted, item, amount));
            return itemAmountAdjusted;
        }
    };
    private static final ItemGenerator ONE_OF_EACH_GENERATOR = new ItemGenerator() {
        @Override
        public Limit limitedBy() {
            return Limit.SIZE;
        }

        @Override
        public NonNullList<ItemStack> generateForTag(List<WeightedItem> available, int count, RandomSource random) {
            int availableSize = available.size();
            int adjustedCount = Math.min(availableSize, count);
            NonNullList<ItemStack> selected = NonNullList.createWithCapacity(adjustedCount);
            int randomOffset = random.nextInt(availableSize);
            for (int i = 0; i < adjustedCount; i++) {
                selected.add(available.get((i + randomOffset) % availableSize).asStack());
            }

            return selected;
        }

        @Override
        public String toString() {
            return "One of Each";
        }
    };

    public static final Supplier<ItemGenerator> DEFAULT = ClaySoldiersCommon.PLATFORM.registerItemGenerator("default", () -> DEFAULT_GENERATOR);

    public static final Supplier<ItemGenerator> ONE_OF_EACH = ClaySoldiersCommon.PLATFORM.registerItemGenerator("one_of_each", () -> ONE_OF_EACH_GENERATOR);

    public static final Supplier<ItemGenerator> ONE_OF_EACH_NO_TAG = ClaySoldiersCommon.PLATFORM.registerItemGenerator("one_of_each_no_tag", () -> createdNoTagGenerator(ONE_OF_EACH_GENERATOR));

    public static final Supplier<ItemGenerator> COMBINED_DEFAULTED = ClaySoldiersCommon.PLATFORM.registerItemGenerator("combined_defaulted", () -> createdCombinedGenerator(DEFAULT_GENERATOR));

    public static void init() {
    }

    private static ItemGenerator createdCombinedGenerator(ItemGenerator base) {
        return new ItemGenerator() {
            @Override
            public NonNullList<ItemStack> generate(List<WeightedItem> ignored, int count, RandomSource random, List<Generator> all) {
                NonNullList<ItemStack> selected = NonNullList.create();
                List<Generator> generatedExpected = new ArrayList<>(all);
                int initialIndividualCount = count / generatedExpected.size();
                int remaining = count % generatedExpected.size();

                var it = generatedExpected.iterator();
                while (it.hasNext()) {
                    var generator = it.next();
                    var generated = generator.generateForTag(initialIndividualCount, random);
                    selected.addAll(generated);

                    if (generator.limitedBy() == Limit.SIZE) {
                        remaining += initialIndividualCount - generated.size();
                        it.remove();
                    }
                }

                initialIndividualCount = remaining / generatedExpected.size();
                remaining = remaining % generatedExpected.size();

                for (int i = 0; i < generatedExpected.size(); i++) {
                    selected.addAll(generatedExpected.get(i).generateForTag(initialIndividualCount + (remaining < i ? 1 : 0), random));
                }

                return selected;
            }

            @Override
            public Limit limitedBy() {
                return base.limitedBy();
            }

            @Override
            public NonNullList<ItemStack> generateForTag(List<WeightedItem> available, int count, RandomSource random) {
                return base.generateForTag(available, count, random);
            }

            @Override
            public String toString() {
                return "Combined(%s)".formatted(base);
            }
        };
    };

    private static ItemGenerator createdNoTagGenerator(ItemGenerator base) {
        return new ItemGenerator() {
            @Override
            public Limit limitedBy() {
                return Limit.ZERO;
            }

            @Override
            public NonNullList<ItemStack> generateForTag(List<WeightedItem> available, int count, RandomSource random) {
                return EMPTY;
            }

            @Override
            public NonNullList<ItemStack> generate(List<WeightedItem> available, int count, RandomSource random, List<Generator> all) {
                return base.generate(available, count, random, all);
            }

            @Override
            public String toString() {
                return base.toString() + "(No Tag)";
            }
        };
    };


    private static boolean appendToMap(Map<Item, Integer> items, WeightedItem weightItem, RandomSource random) {
        if (random.nextFloat() < weightItem.getWeight()) {
            Integer count = items.get(weightItem.getItem());
            if (count == null) {
                items.put(weightItem.getItem(), 1);
            } else {
                items.put(weightItem.getItem(), ++count);
            }
            return true;
        } else {
            return false;
        }
    }

    private static void addItemStackToList(List<ItemStack> itemsCounted, Item item, int count) {
        int maxStackSize = item.getDefaultMaxStackSize();
        if (count <= maxStackSize) {
            itemsCounted.add(new ItemStack(item, count));
        } else {
            itemsCounted.add(new ItemStack(item, maxStackSize));
            addItemStackToList(itemsCounted, item, count - maxStackSize);
        }
    }
}