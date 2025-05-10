package net.bumblebee.claysoldiers.integration.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public interface CommonEntityServerAppender<T extends Entity> {
    void appendServerData(CompoundTag tag, T entity);
    ResourceLocation getUniqueId();

    Class<T> getTargetClass();
}
