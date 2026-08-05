package net.bumblebee.claysoldiers.menu.horse;

import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.common.horse.AbstractClayHorse;
import net.bumblebee.claysoldiers.menu.AbstractClayMobMenuSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public abstract class ClayHorseMenuSlot extends AbstractClayMobMenuSlot {
    public static final String ARMOR_SLOT_NAME = SoldierEquipmentSlot.SOLDIER_SLOT_PREFIX + "horse.armor";
    public static final String HORN_SLOT_NAME = SoldierEquipmentSlot.SOLDIER_SLOT_PREFIX + "horse.horn";


    private ClayHorseMenuSlot(int pSlot, int pX, int pY) {
        super(pSlot, pX, pY);
    }

    public static ClayHorseMenuSlot createArmorSlot(AbstractClayHorse horse, int pX, int pY) {
        return new ClayHorseMenuSlot(1, pX, pY) {
            @Override
            public ItemStack getItem() {
                return horse.getArmor().stack();
            }

            @Override
            public void set(ItemStack pStack) {
                horse.setArmor(pStack);
            }

            @Override
            public @NonNull Component getDisplayName() {
                return Component.translatable(ARMOR_SLOT_NAME);
            }
        };
    }

    public static ClayHorseMenuSlot createHornSlot(AbstractClayHorse horse, int pX, int pY) {
        return new ClayHorseMenuSlot(1, pX, pY) {
            @Override
            public ItemStack getItem() {
                return horse.getHorn().stack();
            }

            @Override
            public void set(ItemStack pStack) {
                horse.setHorn(pStack);
            }

            @Override
            public @NonNull Component getDisplayName() {
                return Component.translatable(HORN_SLOT_NAME);
            }
        };
    }

    @Override
    public boolean mayPlace(ItemStack pStack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player pPlayer) {
        return false;
    }

    @Override
    public ItemStack remove(int pAmount) {
        return ItemStack.EMPTY;
    }

    @Override
    public abstract ItemStack getItem();

    @Override
    public abstract void set(ItemStack pStack);

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
