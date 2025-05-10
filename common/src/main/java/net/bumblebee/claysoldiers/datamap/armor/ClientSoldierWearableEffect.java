package net.bumblebee.claysoldiers.datamap.armor;

import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.*;

public class ClientSoldierWearableEffect extends SoldierWearableEffect implements ClientWearableRenderer {
    private static final Map<SoldierEquipmentSlot, ArmorItem> DEFAULT_MAP = new EnumMap<>(SoldierEquipmentSlot.class);
    static {
        DEFAULT_MAP.put(SoldierEquipmentSlot.HEAD, (ArmorItem) Items.IRON_HELMET);
        DEFAULT_MAP.put(SoldierEquipmentSlot.CHEST, (ArmorItem) Items.IRON_CHESTPLATE);
        DEFAULT_MAP.put(SoldierEquipmentSlot.LEGS, (ArmorItem) Items.IRON_LEGGINGS);
        DEFAULT_MAP.put(SoldierEquipmentSlot.FEET, (ArmorItem) Items.IRON_BOOTS);
    }
    private ItemStack armorStack;
    private List<TrimHolder> finishedArmorTrims;


    public ClientSoldierWearableEffect(ArmorItem item, ColorHelper color, Set<SoldierArmorTrim> trims, boolean offsetColor) {
        super(item, color, trims, offsetColor);
    }

    public static SoldierWearableEffect create(ArmorItem item, ColorHelper color, Set<SoldierArmorTrim> trims, boolean offsetColor) {
        return new ClientSoldierWearableEffect(item, color, trims, offsetColor);
    }

    public ArmorItem defaultModel(SoldierEquipmentSlot slot) {
        return DEFAULT_MAP.get(slot);
    }

    @Override
    public ItemStack getArmorCopyStack() {
        if (armorStack != null) {
            return armorStack;
        }
        ArmorItem copyModel = copyModel();
        if (copyModel == null) {
            return ItemStack.EMPTY;
        }

        armorStack = copyModel.getDefaultInstance();

        if (armorStack.is(ItemTags.DYEABLE) && !getColorHelper().equals(ColorHelper.EMPTY)) {
            armorStack.set(DataComponents.DYED_COLOR, new DyedItemColor(getColorHelper().getColorStatic(), false));
        }

        return armorStack;
    }

    @Override
    public Iterable<TrimHolder> getArmorTrims(RegistryAccess access) {
        if (finishedArmorTrims != null) {
            return finishedArmorTrims;
        }
        finishedArmorTrims = new ArrayList<>();

        for (SoldierArmorTrim trim : trims) {
            ArmorTrim armorTrim = trim.createTrim(access);
            if (armorTrim != null) {
                finishedArmorTrims.add(new TrimHolder(armorTrim, trim.getColor()));

            }
        }
        return finishedArmorTrims;
    }
}
