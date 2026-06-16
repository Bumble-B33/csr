package net.bumblebee.claysoldiers.integration.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public interface CommonEntityProvider<T extends Entity> {
    void appendTooltip(T entity, CommonTooltipHelper tooltip, boolean detail, CompoundTag serverData);

    Identifier getUniqueId();

    Class<T> getTargetClass();

    default boolean requiresServerData() {
        return false;
    }
}
