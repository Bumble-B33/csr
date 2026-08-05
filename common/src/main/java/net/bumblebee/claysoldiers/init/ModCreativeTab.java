package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintManager;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModCreativeTab {
    public static final String CLAY_SOLDIERS_TAB_TITLE = "itemGroup." + ClaySoldiersCommon.MOD_ID + ".clay_soldiers_tab";
    public static final String CLAY_SOLDIER_ITEMS_TAB_TITLE = "itemGroup." + ClaySoldiersCommon.MOD_ID + ".clay_soldier_items_tab";

    public static final Supplier<CreativeModeTab> CLAY_SOLDIERS_TAB = ClaySoldiersCommon.PLATFORM.registerCreativeModeTab("clay_soldiers", builder -> builder
            .title(Component.translatable(CLAY_SOLDIERS_TAB_TITLE))
            .icon(() -> ModItems.CLAY_SOLDIER.get().getDefaultInstance())
            .displayItems(ClaySoldiersCommon.PLATFORM.createGeneratorForAll())
            .build());

    public static final Supplier<CreativeModeTab> CLAY_SOLDIER_ITEMS_TAB = ClaySoldiersCommon.PLATFORM.registerCreativeModeTabSoldierItems();

    public static void modifySoldierItems(Consumer<ItemStack> out, HolderLookup.Provider registries) {
        ClaySoldiersCommon.ERROR_HANDLER.debug("Adding %s Soldier Puppets in Creative Tab".formatted(ClayMobTeamManger.getAll(registries).count() - 1));
        ClayMobTeamManger.getAll(registries).forEach(team -> {
            if (!team.key().equals(ClayMobTeamManger.DEFAULT_KEY)) {
                out.accept(ClaySoldierSpawnItem.createStack(team));
            }
        });
    }

    public static void modifyBlueprint(Consumer<ItemStack> out, HolderLookup.Provider holders) {
        for (var items : BlueprintManager.getBlueprintItems(holders)) {
            out.accept(items);
        }
    }

    public static void addBattery(Item item, Consumer<ItemStack> out) {
        var empty = item.getDefaultInstance();
        var full = item.getDefaultInstance();

        var helper = ClaySoldiersCommon.ENERGY_HELPER;

        full.set(helper.getComponent(helper.getMaxEnergyStorage(full)));

        out.accept(empty);
        out.accept(full);
    }

    public static void init() {
    }
}
