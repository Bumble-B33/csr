package net.bumblebee.claysoldiers.item;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class BrickedClaySoldierItem extends BrickedItem {
    public static final String ORIGINAL_DISPLAY_NAME = "bricked_item." + ClaySoldiersCommon.MOD_ID + ".original";

    public BrickedClaySoldierItem(Properties pProperties) {
        super(pProperties.component(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), ClayMobTeamManger.DEFAULT_TYPE));
    }

    @Override
    public ItemStack getOriginal(ItemStack bricked) {
        return bricked.transmuteCopy(ModItems.CLAY_SOLDIER.get());
    }

    @Override
    public Component getOriginalDisplayName(ItemStack bricked, HolderLookup.Provider registries) {
        var id = bricked.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get());
        if (id != null) {
            return Component.translatable(ORIGINAL_DISPLAY_NAME, ClayMobTeamManger.getFromKeyOrError(id, registries).getDisplayName());
        }
        return Component.empty();
    }
}
