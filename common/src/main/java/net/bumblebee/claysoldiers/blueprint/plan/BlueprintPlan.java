package net.bumblebee.claysoldiers.blueprint.plan;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.blueprint.BlueprintRequestResult;
import net.bumblebee.claysoldiers.blueprint.BlueprintTemplateSettings;
import net.bumblebee.claysoldiers.blueprint.BlueprintUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.BiConsumer;

public abstract class BlueprintPlan {
    public static final String ITEMS_TAG = "ItemMap";
    public static final String HAS_STARTED_TAG = "Started";
    public static final Codec<Either<BlueprintItemCountMap.Mutable, BlueprintBlockInfoList>> REQUIREMENTS_CODEC = Codec.either(
            BlueprintItemCountMap.CODEC,
            BlueprintBlockInfoList.CODEC
    );

    public static final Codec<BlueprintPlan.Builder> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.BOOL.fieldOf(HAS_STARTED_TAG).forGetter(s -> s.hasStarted),
            REQUIREMENTS_CODEC.fieldOf(ITEMS_TAG).forGetter(s -> s.requirements),
            BlueprintUtil.SIZE_MAP_CODEC.forGetter(s -> s.size),
            Codec.BOOL.fieldOf("client").forGetter(s -> s.client)
    ).apply(in, BlueprintPlan.Builder::new));

    protected boolean hasStarted;
    protected final BlueprintItemCountMap.Mutable itemCountMap;
    private final Vec3i size;

    public BlueprintPlan(BlueprintItemCountMap itemCountMap, Vec3i size) {
        this.itemCountMap = itemCountMap.mutable();
        this.size = size;
        this.hasStarted = false;
    }

    /**
     * Tries to place the give {@code Item} anywhere in this structure.
     *
     * @param item the item to place
     * @param base base position of this template
     * @return whether the {@code Item} could be placed.
     */
    public abstract BlueprintRequestResult tryPlacing(Level level, ItemStack item, BlockPos base, BlueprintTemplateSettings settings);

    /**
     * Returns if this item is needed in this Template, if so reduces it count.
     *
     * @return whether this Item was needed
     */
    public boolean hasItemAndShrink(Item item) {
        return itemCountMap.hasItemAndShrink(item);
    }

    protected int getNumberOfItems() {
        return itemCountMap.getNumberOfItems();
    }

    /**
     * @return a list of all Items needed to build this Template.
     */
    public List<ItemStack> getNeededItems() {
        return itemCountMap.asList();
    }


    public void forEachItemRequired(BiConsumer<Item, Integer> action) {
        itemCountMap.forEach(action);
    }

    public boolean isFinished() {
        return itemCountMap.isEmpty();
    }

    public Vec3i getSize() {
        return size;
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public void start() {
        hasStarted = true;
    }


    public abstract Builder asBuilder(boolean client);

    public static class Builder {
        private final boolean hasStarted;
        private final Either<BlueprintItemCountMap.Mutable, BlueprintBlockInfoList> requirements;
        private final Vec3i size;
        private final boolean client;

        protected Builder(boolean hasStarted, Either<BlueprintItemCountMap.Mutable, BlueprintBlockInfoList> requirements, Vec3i size, boolean client) {
            this.hasStarted = hasStarted;
            this.requirements = requirements;
            this.size = size;
            this.client = client;
        }

        public BlueprintPlan build(HolderLookup.Provider registries) {
            BlueprintPlan plan;
            if (client) {
                if (requirements.left().isPresent()) {
                    plan = new ClientBlueprintPlan(requirements.left().orElseThrow(), size);
                } else {
                    plan = new ClientBlueprintPlan(requirements.right().map(BlueprintUtil::getNeededItemsFromInfo).orElseThrow(), size);
                }
            } else {
                plan = new ServerBlueprintPlan(size, requirements.right().orElseThrow(), registries);//ServerBlueprintPlan.load(size, tag, registries);
            }
            if (hasStarted) {
                plan.start();
            }
            return plan;
        }
    }
}
