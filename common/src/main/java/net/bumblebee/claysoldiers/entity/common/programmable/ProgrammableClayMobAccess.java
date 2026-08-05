package net.bumblebee.claysoldiers.entity.common.programmable;

import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import org.jspecify.annotations.Nullable;

public interface ProgrammableClayMobAccess {

    @Nullable ClaySoldierChip getInstalledChip();
}
