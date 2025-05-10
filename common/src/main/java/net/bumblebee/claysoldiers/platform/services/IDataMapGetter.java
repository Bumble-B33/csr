package net.bumblebee.claysoldiers.platform.services;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.datamap.armor.SoldierMultiWearable;
import net.bumblebee.claysoldiers.datamap.horse.ClayHorseItemMap;
import net.bumblebee.claysoldiers.datamap.horse.ClayHorseWearableProperties;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.soldierpoi.SoldierPoi;
import net.bumblebee.claysoldiers.soldierproperties.SoldierVehicleProperties;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

public interface IDataMapGetter {
    @Nullable
    SoldierHoldableEffect getEffect(ItemStack stack);

    @Nullable
    SoldierHoldableEffect getEffect(Item item);

    @Nullable
    SoldierMultiWearable getArmor(ItemStack stack);

    @Nullable
    SoldierPoi getItemPoi(ItemStack stack);

    @Nullable
    SoldierPoi getItemPoi(Item item);

    @Nullable
    SoldierPoi getBlockPoi(Block block);

    default @Nullable SoldierPoi getBlockPoi(ItemStack stack) {
        return getBlockPoi(stack.getItem());
    }

    default @Nullable SoldierPoi getBlockPoi(Item item) {
        return item instanceof BlockItem blockItem ? getBlockPoi(blockItem.getBlock()) : null;
    }

    default ClayHorseWearableProperties getHorseArmor(Item item) {
        return ClayHorseItemMap.get(item);
    }

    @Nullable
    SoldierVehicleProperties getVehicleProperties(EntityType<?> type);

    @NotNull
    List<Item> getHoldableEffectForSlot(SoldierEquipmentSlot slot);

    static <T> void warnHoldable(Map<T, SoldierHoldableEffect> dataMap, BiPredicate<T, TagKey<Item>> isTagged) {
        dataMap.forEach((k, effect) -> {
            effect.getRemovalConditions().forEach(r -> {
                if (r.getChance() <= 0) {
                    ClaySoldiersCommon.LOGGER.warn("DataMap: {} has a RemovalCondition({}) with chance 0", k, r.getDisplayName().getString());
                }
            });
            if (!isTagged.test(k, ModTags.Items.SOLDIER_HOLDABLE)) {
                ClaySoldiersCommon.LOGGER.warn("DataMap: {} is not tagged with {}", k, ModTags.Items.SOLDIER_HOLDABLE);
            }
        });
    }

    static <T> Map<SoldierEquipmentSlot, List<Item>> createBySlotMap(Map<T, SoldierHoldableEffect> dataMap, Function<T, Item> keyToItem) {
        Map<SoldierEquipmentSlot, List<Item>> map = new EnumMap<>(SoldierEquipmentSlot.class);
        for (SoldierEquipmentSlot slot : SoldierEquipmentSlot.values()) {
            map.put(slot, new ArrayList<>());
        }
        dataMap.forEach((k, effect) -> effect.slots().forEach(slot -> map.get(slot).add(keyToItem.apply(k))));
        return map;
    }
}
