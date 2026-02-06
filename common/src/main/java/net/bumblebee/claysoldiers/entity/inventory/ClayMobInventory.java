package net.bumblebee.claysoldiers.entity.inventory;

import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class ClayMobInventory<S, T> extends EntityEquipment {
    private final Map<EquipmentSlot, T> effectMap;
    private final Function<S, EquipmentSlot> mapper;
    private final Function<ItemStack, T> factory;
    private final T empty;

    public ClayMobInventory(Function<S, EquipmentSlot> mapper, Function<ItemStack, T> factory, T empty) {
        this.mapper = mapper;
        this.factory = factory;
        this.empty = empty;
        this.effectMap = new EnumMap<>(EquipmentSlot.class);
    }

    public T getWithEffect(S slot) {
        return effectMap.getOrDefault(mapper.apply(slot), empty);
    }

    @Override
    public ItemStack set(EquipmentSlot slot, ItemStack stack) {
        this.effectMap.put(slot, factory.apply(stack));
        return super.set(slot, stack);
    }

    @Override
    public void clear() {
        super.clear();
        this.effectMap.replaceAll((p_401733_, p_401734_) -> empty);
    }

    @Override
    public void setAll(EntityEquipment equipment) {
        super.setAll(equipment);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            set(slot, get(slot));
        }
    }
}
