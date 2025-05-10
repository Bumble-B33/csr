package net.bumblebee.claysoldiers.integration.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface CommonBlockProvider {
    void appendTooltip(BlockData data, CommonTooltipHelper tooltip, boolean detail);
    ResourceLocation getUniqueId();

    Class<? extends Block> getTargetClass();

    record BlockData(BlockState state, @Nullable BlockEntity entity, CompoundTag serverData) {}
}
