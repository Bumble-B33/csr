package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.datamap.FabricDataMapLoader;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.datamap.armor.SoldierMultiWearable;
import net.bumblebee.claysoldiers.platform.services.IDataMapGetter;
import net.bumblebee.claysoldiers.soldierpoi.SoldierPoi;
import net.bumblebee.claysoldiers.soldierproperties.SoldierVehicleProperties;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FabricDataMapGetter implements IDataMapGetter {
    private static final Map<Holder<Item>, SoldierHoldableEffect> SOLDIER_HOLDABLE_MAP = FabricDataMapLoader.SOLDIER_HOLDABLE_MAP;
    private static final Map<Holder<Item>, SoldierMultiWearable> SOLDIER_WEARABLE_MAP = FabricDataMapLoader.SOLDIER_WEARABLE_MAP;
    private static final Map<Holder<Item>, SoldierPoi> SOLDIER_ITEM_POI_MAP = FabricDataMapLoader.SOLDIER_ITEM_POI_MAP;
    private static final Map<Holder<Block>, SoldierPoi> SOLDIER_BLOCK_POI_MAP = FabricDataMapLoader.SOLDIER_BLOCK_POI_MAP;
    private static final Map<Holder<EntityType<?>>, SoldierVehicleProperties> SOLDIER_VEHICLE_PROPERTIES_MAP = FabricDataMapLoader.SOLDIER_VEHICLE_PROPERTIES_MAP;

    private final EnumMap<SoldierEquipmentSlot, List<Item>> bySlot;

    public FabricDataMapGetter() {
        this.bySlot = new EnumMap<>(SoldierEquipmentSlot.class);
        for (SoldierEquipmentSlot slot : SoldierEquipmentSlot.values()) {
            bySlot.put(slot, new ArrayList<>());
        }
    }

    @Override
    public @Nullable SoldierHoldableEffect getEffect(ItemStack stack) {
        return SOLDIER_HOLDABLE_MAP.get(stack.typeHolder());
    }

    @Override
    public @Nullable SoldierHoldableEffect getEffect(Item item) {
        return SOLDIER_HOLDABLE_MAP.get(item.builtInRegistryHolder());
    }

    @Override
    public @Nullable SoldierMultiWearable getArmor(ItemStack stack) {
        return SOLDIER_WEARABLE_MAP.get(stack.typeHolder());
    }

    @Override
    public @Nullable SoldierPoi getItemPoi(ItemStack stack) {
        return SOLDIER_ITEM_POI_MAP.get(stack.typeHolder());
    }

    @Override
    public @Nullable SoldierPoi getItemPoi(Item item) {
        return SOLDIER_ITEM_POI_MAP.get(item.builtInRegistryHolder());
    }

    @Override
    public @Nullable SoldierPoi getBlockPoi(Block block) {
        return SOLDIER_BLOCK_POI_MAP.get(block.builtInRegistryHolder());
    }

    @Override
    public @Nullable SoldierVehicleProperties getVehicleProperties(EntityType<?> type) {
        return SOLDIER_VEHICLE_PROPERTIES_MAP.get(type.builtInRegistryHolder());
    }

    @Override
    public @NotNull List<Item> getHoldableEffectForSlot(SoldierEquipmentSlot slot) {
        return Objects.requireNonNull(bySlot.get(slot));
    }
}