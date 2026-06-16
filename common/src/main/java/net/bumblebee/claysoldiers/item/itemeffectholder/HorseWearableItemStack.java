package net.bumblebee.claysoldiers.item.itemeffectholder;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.horse.ClayHorseWearableProperties;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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
        return ClaySoldiersCommon.DATA_MAP.getHorseArmor(stack);
    }

    @Nullable
    public EquipmentSlot getEquipmentSlot() {
        return effect != null ? effect.getSlot().asEquipmentSlot() : null;
    }
}
