package net.bumblebee.claysoldiers.entity.common.programmable;

import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface ProgrammableClayMobAccess {
    ProgrammableClayMobAccess EMPTY = () -> null;

    @Nullable
    ClaySoldierChip<?> getInstalledChip();
}
