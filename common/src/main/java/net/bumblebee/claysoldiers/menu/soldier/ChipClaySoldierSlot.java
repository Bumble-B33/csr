package net.bumblebee.claysoldiers.menu.soldier;

import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClayMobAccess;
import net.bumblebee.claysoldiers.item.chip.ClaySoldierChipItem;
import net.bumblebee.claysoldiers.menu.AbstractClayMobMenuSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class ChipClaySoldierSlot extends AbstractClayMobMenuSlot {
    public static final String CHIP_NAME = SoldierEquipmentSlot.SOLDIER_SLOT_PREFIX + "chip";
    private final ProgrammableClayMobAccess soldier;

    public ChipClaySoldierSlot(ProgrammableClayMobAccess soldier, int pX, int pY) {
        super(1, pX, pY);
        this.soldier = soldier;
    }

    @Override
    public ItemStack getItem() {
        return ClaySoldierChipItem.create(soldier.getInstalledChip());
    }

    @Override
    public void set(ItemStack pStack) {

    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable(CHIP_NAME);
    }
}
