package net.bumblebee.claysoldiers.soldieritemtypes;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierPickUpPriority;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class WeightedItem {
    private final Item item;
    private FloatSupplier weightGetter;
    private float weight = -1;

    public WeightedItem(Item item) {
        this.item = item;
        this.weightGetter = () -> getWeight(item);;
    }

    public float getWeight() {
        return weight;
    }

    public Item getItem() {
        return item;
    }

    public ItemStack asStack() {
        return item.getDefaultInstance();
    }

    @Override
    public String toString() {
        return "%s(%.2f)".formatted(item, weight);
    }


    private static float getWeight(Item item) {
        var eff = ClaySoldiersCommon.DATA_MAP.getEffect(item);
        return eff == null ? 0 : 1f / (eff.pickUpPriority() - SoldierPickUpPriority.MIN + 1);
    }

    public float finalizeWeight() {
        weight = weightGetter.getAsFloat();
        weightGetter = null;
        return weight;
    }

    @FunctionalInterface
    private interface FloatSupplier {
        float getAsFloat();
    }
}
