package net.bumblebee.claysoldiers.menu.horse;

import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.horse.AbstractClayHorse;
import net.bumblebee.claysoldiers.menu.AbstractClayMenuSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class ClayHorseSlot extends AbstractClayMenuSlot {
    public static final String ARMOR_SLOT_NAME = SoldierEquipmentSlot.SOLDIER_SLOT_PREFIX + "armor";

    private ClayHorseSlot(int pSlot, int pX, int pY) {
        super(pSlot, pX, pY);
    }

    public static ClayHorseSlot createArmorSlot(AbstractClayHorse horse, int pX, int pY) {
        return new ClayHorseSlot(1, pX, pY) {
            @Override
            public ItemStack getItem() {
                return horse.getArmor().stack();
            }

            @Override
            public void set(ItemStack pStack) {
                horse.setArmor(pStack);
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable(ARMOR_SLOT_NAME);
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
