package net.bumblebee.claysoldiers.item.blueprint;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;

public class BlueprintPageItem extends Item {
    public BlueprintPageItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return ClaySoldiersCommon.COMMON_HOOKS.isBlueprintEnabled(enabledFeatures);
    }
}
