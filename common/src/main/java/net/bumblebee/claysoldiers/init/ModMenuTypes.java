package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.menu.escritoire.EscritoireMenu;
import net.bumblebee.claysoldiers.menu.horse.ClayHorseMenu;
import net.bumblebee.claysoldiers.menu.soldier.ClaySoldierMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public final class ModMenuTypes {
    public static final Supplier<MenuType<ClaySoldierMenu>> CLAY_SOLDIER_MENU = ClaySoldiersCommon.PLATFORM.registerMenuType("clay_soldier_menu",
            ClaySoldierMenu::new);
    public static final Supplier<MenuType<ClayHorseMenu>> CLAY_HORSE_MENU = ClaySoldiersCommon.PLATFORM.registerMenuType("clay_horse_menu",
            ClayHorseMenu::new);
    public static final Supplier<MenuType<EscritoireMenu>> ESCRITOIRE_MENU = ClaySoldiersCommon.PLATFORM.registerMenuType("escritoire",
            EscritoireMenu::new);

    private ModMenuTypes() {
    }

    public static void init() {
    }
}
