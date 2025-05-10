package net.bumblebee.claysoldiers.capability;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface IBlockCache<T> {
    BlockPos pos();

    @Nullable
    T getCapability();
}
