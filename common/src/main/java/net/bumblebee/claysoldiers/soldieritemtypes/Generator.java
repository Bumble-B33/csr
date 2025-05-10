package net.bumblebee.claysoldiers.soldieritemtypes;

import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public interface Generator {
    NonNullList<ItemStack> generateForTag(int count, RandomSource random);

    ItemGenerator.Limit limitedBy();
}
