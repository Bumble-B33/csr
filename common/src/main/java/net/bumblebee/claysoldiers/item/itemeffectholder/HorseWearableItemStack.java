package net.bumblebee.claysoldiers.item.itemeffectholder;

import net.bumblebee.claysoldiers.datamap.horse.ClayHorseItemMap;
import net.bumblebee.claysoldiers.datamap.horse.ClayHorseWearableProperties;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class HorseWearableItemStack extends ItemStackEffectHolder<ClayHorseWearableProperties> {
    public static final HorseWearableItemStack EMPTY = new HorseWearableItemStack();

    public HorseWearableItemStack(ItemStack stack) {
        super(stack);
    }

    private HorseWearableItemStack() {
        super(ItemStack.EMPTY, null);
    }

    public float protection() {
        return effect != null ? effect.protection() : 0;
    }

    @Override
    protected ClayHorseWearableProperties createEffectOnInitialisation(ItemStack stack) {
        return ClayHorseItemMap.get(stack);
    }

    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }
}
