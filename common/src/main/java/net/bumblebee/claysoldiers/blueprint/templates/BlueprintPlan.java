package net.bumblebee.claysoldiers.blueprint.templates;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintTemplateSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;
import java.util.Map;

public abstract class BlueprintPlan {
    public static final String ITEMS_TAG = "ItemMap";
    public static final String HAS_STARTED_TAG = "Started";

    protected static final Codec<Map<Item, Integer>> ITEM_COUNT_MAP_CODEC = Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), Codec.INT);

    private boolean hasStarted;
    private final Map<Item, Integer> itemCountMap;
    private final Vec3i size;

    public BlueprintPlan(Map<Item, Integer> itemCountMap, Vec3i size) {
        this.itemCountMap = itemCountMap;
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
    public abstract PlaceResult tryPlacing(Level level, ItemStack item, BlockPos base, BlueprintTemplateSettings settings);

    /**
     * Returns if this item is needed in this Template, if so reduces it count.
     *
     * @return whether this Item was needed
     */
    public boolean hasItemAndShrink(Item item) {
        if (!itemCountMap.containsKey(item)) {
            return false;
        }
        Integer count = itemCountMap.get(item);
        if (count == null || count <= 0) {
            return false;
        }
        count--;
        if (count <= 0) {
            itemCountMap.remove(item);
        } else {
            itemCountMap.put(item, count);
        }

        return true;
    }

    protected int getNumberOfItems() {
        return itemCountMap.values().stream().reduce(0, Integer::sum);
    }

    /**
     * @return a list of all Items needed to build this Template.
     */
    public List<ItemStack> getNeededItems() {
        return BlueprintUtil.itemMapToList(itemCountMap);
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

    public void saveHasStarted(ValueOutput tag) {
        if (hasStarted()) {
            tag.putBoolean(HAS_STARTED_TAG, true);
        }
    }

    public void saveSize(ValueOutput tag) {
        tag.store(StructureTemplate.SIZE_TAG, Vec3i.CODEC, getSize());
    }

    public void saveItems(ValueOutput tag) {
        if (itemCountMap.isEmpty()) {
            return;
        }
        tag.store(ITEMS_TAG, ITEM_COUNT_MAP_CODEC, itemCountMap);
    }

    protected void loadItemCount(ValueInput tag) {
        tag.read(ITEMS_TAG, ITEM_COUNT_MAP_CODEC).ifPresent(itemCountMap::putAll);
    }

    protected void loadHasStarted(ValueInput tag) {
        hasStarted = tag.getBooleanOr(HAS_STARTED_TAG, false);
    }

    protected ListTag newIntegerList(int... pValues) {
        ListTag listtag = new ListTag();

        for (int i : pValues) {
            listtag.add(IntTag.valueOf(i));
        }

        return listtag;
    }

    public enum PlaceResult {
        SUCCESS,
        NOT_NEEDED,
        CANT_PLACE;

        public boolean isSuccess() {
            return this == SUCCESS;
        }

        public boolean isFail() {
            return this != SUCCESS;
        }
    }
}
