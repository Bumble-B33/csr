package net.bumblebee.claysoldiers.menu.soldier;

import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.menu.AbstractClayMobMenuSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class CarriedClaySoldierMenuSlot extends AbstractClayMobMenuSlot {
    public static final String CARRIED_SLOT_NAME = SoldierEquipmentSlot.SOLDIER_SLOT_PREFIX + "carried";
    private final ProgrammableClaySoldierEntity soldier;

    protected CarriedClaySoldierMenuSlot(ProgrammableClaySoldierEntity soldier, int pX, int pY) {
        super(1, pX, pY);
        this.soldier = soldier;
    }

    @Override
    public ItemStack getItem() {
        return soldier.getCarriedStack();
    }

    @Override
    public void set(ItemStack stack) {
        soldier.setCarriedStack(stack);
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack got = soldier.getCarriedStack().split(amount);
        soldier.updateCarriedStack();
        return got;
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable(CARRIED_SLOT_NAME);
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
    public int getMaxStackSize() {
        return 64;
    }
}
