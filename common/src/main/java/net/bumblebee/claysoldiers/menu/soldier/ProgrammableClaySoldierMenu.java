package net.bumblebee.claysoldiers.menu.soldier;

import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

public class ProgrammableClaySoldierMenu extends ClaySoldierMenu {
    public ProgrammableClaySoldierMenu(int pContainerId, Inventory inv, int extraData) {
        this(pContainerId, inv, getSoldierFromData(inv, extraData));
    }

    public ProgrammableClaySoldierMenu(int pContainerId, Inventory inv, @Nullable ProgrammableClaySoldierEntity claySoldier) {
        super(ModMenuTypes.PROGRAMMABLE_CLAY_SOLDIER_MENU.get(), pContainerId, inv, claySoldier);
        if (claySoldier != null) {
            addSlot(new ChipClaySoldierSlot(claySoldier, 77, 18));
            addSlot(new CarriedClaySoldierMenuSlot(claySoldier, 77, 90));

        }
    }

    private static @Nullable ProgrammableClaySoldierEntity getSoldierFromData(Inventory inventory, int extraData) {
        return inventory.player.level().getEntity(extraData) instanceof ProgrammableClaySoldierEntity claySoldier ? claySoldier : null;
    }
}
