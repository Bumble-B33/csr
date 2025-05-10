package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.datamap.armor.SoldierMultiWearable;
import net.bumblebee.claysoldiers.init.ModDataMaps;
import net.bumblebee.claysoldiers.platform.services.IDataMapGetter;
import net.bumblebee.claysoldiers.soldierpoi.SoldierPoi;
import net.bumblebee.claysoldiers.soldierproperties.SoldierVehicleProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NeoForgeDataMapGetter implements IDataMapGetter {
    private static Map<SoldierEquipmentSlot, List<Item>> bySlot = new EnumMap<>(SoldierEquipmentSlot.class);

    @Override
    public @Nullable SoldierHoldableEffect getEffect(ItemStack stack) {
        return stack.getItemHolder().getData(ModDataMaps.SOLDIER_HOLDABLE);
    }

    @Override
    public @Nullable SoldierHoldableEffect getEffect(Item item) {
        return item.builtInRegistryHolder().getData(ModDataMaps.SOLDIER_HOLDABLE);
    }

    @Override
    public @Nullable SoldierMultiWearable getArmor(ItemStack stack) {
        return stack.getItemHolder().getData(ModDataMaps.SOLDIER_ARMOR);
    }

    @Override
    public @Nullable SoldierPoi getItemPoi(ItemStack stack) {
        return stack.getItemHolder().getData(ModDataMaps.SOLDIER_ITEM_POI);
    }

    @Override
    public @Nullable SoldierPoi getItemPoi(Item item) {
        return item.builtInRegistryHolder().getData(ModDataMaps.SOLDIER_ITEM_POI);
    }

    @Override
    public @Nullable SoldierPoi getBlockPoi(Block block) {
        var key = BuiltInRegistries.BLOCK.getResourceKey(block).orElse(null);
        if (key == null) {
            return null;
        }
        return BuiltInRegistries.BLOCK.getData(ModDataMaps.SOLDIER_BLOCK_POI, key);
    }

    @Override
    public @Nullable SoldierVehicleProperties getVehicleProperties(EntityType<?> type) {
        return type.builtInRegistryHolder().getData(ModDataMaps.SOLDIER_VEHICLE_PROPERTIES);
    }

    @Override
    public @NotNull List<Item> getHoldableEffectForSlot(SoldierEquipmentSlot slot) {
        return Objects.requireNonNull(bySlot.get(slot));
    }

    public static void setBySlot(Map<SoldierEquipmentSlot, List<Item>> map) {
        bySlot = map;
    }


    public static String getBySlotMap() {
        return bySlot.toString();
    }
}
