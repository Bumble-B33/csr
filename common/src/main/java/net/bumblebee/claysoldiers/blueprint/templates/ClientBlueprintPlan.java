package net.bumblebee.claysoldiers.blueprint.templates;

import net.bumblebee.claysoldiers.blueprint.BlueprintTemplateSettings;
import net.bumblebee.claysoldiers.util.ErrorHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ClientBlueprintPlan extends BlueprintPlan {
    public ClientBlueprintPlan(Map<Item, Integer> itemCountMap, Vec3i size) {
        super(itemCountMap, size);
    }

    public ClientBlueprintPlan(CompoundTag tag) {
        super(new HashMap<>(), BlueprintUtil.getSizeFromTag(tag));
        loadItemCount(tag);
        loadHasStarted(tag);
    }

    @Override
    public PlaceResult tryPlacing(Level level, ItemStack item, BlockPos base, BlueprintTemplateSettings settings) {
        if (hasItemAndShrink(item.getItem())) {
            return PlaceResult.SUCCESS;
        }
        ErrorHandler.INSTANCE.error("Tried to Place an Item for a Blueprint but it is not needed");
        return PlaceResult.NOT_NEEDED;
    }

    @Override
    public String toString() {
        return "ClientBlueprintTemplate{%s Items(%s): %s}".formatted(
                getSize(),
                getNumberOfItems(),
                getNeededItems()
        );
    }


}
