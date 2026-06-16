package net.bumblebee.claysoldiers.blueprint.plan;

import com.mojang.datafixers.util.Either;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintRequestResult;
import net.bumblebee.claysoldiers.blueprint.BlueprintTemplateSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClientBlueprintPlan extends BlueprintPlan {
    public ClientBlueprintPlan(BlueprintItemCountMap itemCountMap, Vec3i size) {
        super(itemCountMap, size);
    }

    @Override
    public BlueprintRequestResult tryPlacing(Level level, ItemStack item, BlockPos base, BlueprintTemplateSettings settings) {
        if (hasItemAndShrink(item.getItem())) {
            return BlueprintRequestResult.success();
        }
        ClaySoldiersCommon.ERROR_HANDLER.error("Tried to Place an Item for a Blueprint but it is not needed");
        return BlueprintRequestResult.fail();
    }

    @Override
    public Builder asBuilder(boolean client) {
        if (!client) {
            throw new IllegalStateException("Cannot create ServerBlueprintPlan Builder from ClientBlueprintPlan");
        }
        return new Builder(hasStarted, Either.left(itemCountMap), getSize(), true);
    }

    @Override
    public String toString() {
        return "ClientBlueprintTemplate{%s Items(%s): %s%s}".formatted(
                getSize(),
                getNumberOfItems(),
                getNeededItems(),
                hasStarted ? " started" : ""
        );
    }


}
