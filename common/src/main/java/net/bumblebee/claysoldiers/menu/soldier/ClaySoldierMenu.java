package net.bumblebee.claysoldiers.menu.soldier;

import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModMenuTypes;
import net.bumblebee.claysoldiers.menu.AbstractClayMobMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class ClaySoldierMenu extends AbstractClayMobMenu<AbstractClaySoldierEntity> {
    @Nullable
    private final AbstractClaySoldierEntity soldier;

    protected ClaySoldierMenu(MenuType<? extends AbstractClayMobMenu<AbstractClaySoldierEntity>> type, int pContainerId, Inventory inv, @Nullable AbstractClaySoldierEntity claySoldier) {
        super(type, pContainerId, inv);
        this.soldier = claySoldier;
        this.inventoryYOffset = 28;
        initPlayerInventory(inv);
        if (claySoldier != null) {
            addSlot(new ClaySoldierMenuSlot(SoldierEquipmentSlot.MAINHAND, claySoldier, 26, 90));
            addSlot(new ClaySoldierMenuSlot(SoldierEquipmentSlot.OFFHAND, claySoldier, 59, 90));
            addSlot(new ClaySoldierMenuSlot(SoldierEquipmentSlot.HEAD, claySoldier, 8, 18));
            addSlot(new ClaySoldierMenuSlot(SoldierEquipmentSlot.CHEST, claySoldier, 8, 36));
            addSlot(new ClaySoldierMenuSlot(SoldierEquipmentSlot.LEGS, claySoldier, 8, 54));
            addSlot(new ClaySoldierMenuSlot(SoldierEquipmentSlot.FEET, claySoldier, 8, 72));
            addSlot(new ClaySoldierMenuSlot(SoldierEquipmentSlot.CAPE, claySoldier, 77, 36));
            addSlot(new ClaySoldierMenuSlot(SoldierEquipmentSlot.BACKPACK, claySoldier, 77, 54));
            addSlot(new ClaySoldierMenuSlot(SoldierEquipmentSlot.BACKPACK_PASSIVE, claySoldier, 77, 72));
        }
    }

    public static ClaySoldierMenu forClaySoldier(int pContainerId, Inventory inv, @NonNull AbstractClaySoldierEntity claySoldier) {
        return new ClaySoldierMenu(ModMenuTypes.CLAY_SOLDIER_MENU.get(), pContainerId, inv, claySoldier);
    }

    public static ClaySoldierMenu forClaySoldier(int pContainerId, Inventory inv, int extraData) {
        return new ClaySoldierMenu(ModMenuTypes.CLAY_SOLDIER_MENU.get(), pContainerId, inv, getSoldierFromData(inv, extraData));
    }

    private static @Nullable AbstractClaySoldierEntity getSoldierFromData(Inventory inventory, int extraData) {
        return inventory.player.level().getEntity(extraData) instanceof AbstractClaySoldierEntity claySoldier ? claySoldier : null;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    protected Optional<AbstractClaySoldierEntity> getSource() {
        return Optional.ofNullable(soldier);
    }
}
