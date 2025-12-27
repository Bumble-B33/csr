package net.bumblebee.claysoldiers.claypoifunction;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
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

    default int getBlockItemMapColor() {
        if (getItem() instanceof BlockItem blockItemMapColor) {
            return 0xFF000000 | blockItemMapColor.getBlock().defaultMapColor().col;
        }
        return -1;
    }

    default int getDyeItemColor() {
        if (getItem() instanceof DyeItem dyeItem) {
            return dyeItem.getDyeColor().getTextColor();
        }
        return -1;
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
