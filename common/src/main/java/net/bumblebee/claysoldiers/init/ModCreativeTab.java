package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintManger;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class ModCreativeTab {
    public static final String CLAY_SOLDIERS_TAB_TITLE = "itemGroup." + ClaySoldiersCommon.MOD_ID + ".clay_soldiers_tab";
    public static final String CLAY_SOLDIER_ITEMS_TAB_TITLE = "itemGroup." + ClaySoldiersCommon.MOD_ID + ".clay_soldier_items_tab";


    public static final Supplier<CreativeModeTab> CLAY_SOLDIERS_TAB = ClaySoldiersCommon.PLATFORM.registerCreativeModeTab("clay_soldiers", builder -> builder
            .title(Component.translatable(CLAY_SOLDIERS_TAB_TITLE))
            .icon(() -> ModItems.CLAY_SOLDIER.get().getDefaultInstance())
            .displayItems(addAllItems())
            .build());

    public static final Supplier<CreativeModeTab> CLAY_SOLDIER_ITEMS_TAB = ClaySoldiersCommon.PLATFORM.registerCreativeModeTabSoldierItems();



    public static CreativeModeTab.DisplayItemsGenerator addAllItems() {
        return ((itemDisplayParameters, output) -> {
            for (Item item : ClaySoldiersCommon.PLATFORM.getAllItems()) {
                if (item == ModItems.BLUEPRINT.get()) {
                    modifyBlueprint(output, itemDisplayParameters.holders());
                } else {
                    output.accept(item);
                }
            }
            modifySoldierItems(output, itemDisplayParameters.holders());
            output.accept(ModItems.createEnchantedBook(itemDisplayParameters.holders(), ModEnchantments.SOLDIER_PROJECTILE, 1));
        });
    }

    private static void modifySoldierItems(CreativeModeTab.Output out, HolderLookup.Provider registries) {
        ClaySoldiersCommon.LOGGER.debug("Adding {} Soldier Puppets in Creative Tab", ClayMobTeamManger.getAllKeys(registries).count() - 1);
        ClayMobTeamManger.getAllKeys(registries).forEach(key -> {
            if (!key.equals(ClayMobTeamManger.DEFAULT_TYPE)) {
                ItemStack stack = ClayMobTeamManger.createStackForTeam(key, registries);
                out.accept(stack);
            }
        });
    }

    private static void modifyBlueprint(CreativeModeTab.Output out, HolderLookup.Provider holders) {
        for (var items : BlueprintManger.getBlueprintItems(holders)) {
            out.accept(items);
        }
    }

    public static void init() {
    }
}
