package net.bumblebee.claysoldiers.item.blueprint;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class BlueprintDependendBlockItem extends BlockItem {
    public BlueprintDependendBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return ClaySoldiersCommon.COMMON_HOOKS.isBlueprintEnabled(enabledFeatures);
    }
}
