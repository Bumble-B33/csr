package net.bumblebee.claysoldiers.claypoifunction;

import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * Represent a Source the is passed in by the {@link ClayPoiFunction} on executing.
 */
public interface ClayPoiSource {
    /**
     * Returns the {@code ItemStack} associated with this Source if there is one.
     */
    @Nullable
    ItemStack getItemStack();

    /**
     * Returns the owner of this poi if the is one.
     */
    @Nullable
    default Entity getOwner() {
        return null;
    }

    /**
     * Returns the {@code Item} associated with this Source if there is one.
     */
    @Nullable
    default Item getItem() {
        return getItemStack() != null ? getItemStack().getItem() : null;
    }

    default ColorHelper getBlockItemMapColor() {
        if (getItem() instanceof BlockItem blockItemMapColor) {
            return ColorHelper.color(blockItemMapColor.getBlock().defaultMapColor().col);
        }
        return ColorHelper.EMPTY;
    }

    default ColorHelper getDyeItemColor() {
        var stack = getItemStack();
        if (stack == null) {
            return ColorHelper.EMPTY;
        }

        var dyeColor = getItemStack().get(DataComponents.DYE);
        if (dyeColor != null) {
            return ColorHelper.color(dyeColor.getFireworkColor());
        }
        return ColorHelper.EMPTY;
    }

    /**
     * Creates a new source from a give {@code ItemStack}.
     */
    static ClayPoiSource createSource(ItemStack stack) {
        return () -> stack;
    }
    /**
     * Creates a new source from a give {@code ItemEntity}.
     */
    static ClayPoiSource createSource(ItemEntity itemEntity) {
        return new ClayPoiSource() {
            @Override
            public ItemStack getItemStack() {
                return itemEntity.getItem();
            }

            @Override
            public @Nullable Entity getOwner() {
                return itemEntity.getOwner() instanceof Player player ? player : null;
            }
        };
    }



    static ClayPoiSource createSource(Block block) {
        return new ClayPoiSource() {
            @Override
            public @Nullable ItemStack getItemStack() {
                return block.asItem() == Items.AIR ? null : block.asItem().getDefaultInstance();
            }
        };
    }
}
