package net.bumblebee.claysoldiers.menu.soldier;

import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModMenuTypes;
import net.bumblebee.claysoldiers.menu.AbstractClayMobMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ClaySoldierMenu extends AbstractClayMobMenu<AbstractClaySoldierEntity> {
    @Nullable
    private final AbstractClaySoldierEntity soldier;

    public ClaySoldierMenu(int pContainerId, Inventory inv, int extraData) {
        this(pContainerId, inv, inv.player.level().getEntity(extraData) instanceof AbstractClaySoldierEntity claySoldier ? claySoldier : null);
    }
    public ClaySoldierMenu(int pContainerId, Inventory inv, @Nullable AbstractClaySoldierEntity claySoldier) {
        super(ModMenuTypes.CLAY_SOLDIER_MENU.get(), pContainerId, inv);
        this.soldier = claySoldier;
        this.inventoryYOffset = 28;
        initPlayerInventory(inv);
        if (claySoldier != null) {
            addSlot(new ClaySoldierSlot(SoldierEquipmentSlot.MAINHAND, claySoldier, 26, 90));
            addSlot(new ClaySoldierSlot(SoldierEquipmentSlot.OFFHAND, claySoldier, 59, 90));
            addSlot(new ClaySoldierSlot(SoldierEquipmentSlot.HEAD, claySoldier, 8, 18));
            addSlot(new ClaySoldierSlot(SoldierEquipmentSlot.CHEST, claySoldier, 8, 36));
            addSlot(new ClaySoldierSlot(SoldierEquipmentSlot.LEGS, claySoldier, 8, 54));
            addSlot(new ClaySoldierSlot(SoldierEquipmentSlot.FEET, claySoldier, 8, 72));
            addSlot(new ClaySoldierSlot(SoldierEquipmentSlot.CAPE, claySoldier, 77, 36));
            addSlot(new ClaySoldierSlot(SoldierEquipmentSlot.BACKPACK, claySoldier, 77, 54));
            addSlot(new ClaySoldierSlot(SoldierEquipmentSlot.BACKPACK_PASSIVE, claySoldier, 77, 72));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    protected Optional<AbstractClaySoldierEntity> getSource() {
        return Optional.ofNullable(soldier);
    }
}
