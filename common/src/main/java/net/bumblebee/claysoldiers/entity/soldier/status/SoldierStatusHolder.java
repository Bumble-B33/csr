package net.bumblebee.claysoldiers.entity.soldier.status;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface SoldierStatusHolder {
    /**
     * Returns the display name of the Status of this StatusHolder.
     * May be {@code null} to indicate this StatusHolder has currently no Status.
     */
    @Nullable
    Component getStatusDisplayName();
}
