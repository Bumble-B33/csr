package net.bumblebee.claysoldiers.entity.common.programmable.chips;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;

import java.util.function.Supplier;

public final class ClaySoldierChips {
    public static final Supplier<ClaySoldierChip.Type<CombatChip.Data>> COMBAT_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("combat_module", () -> new ClaySoldierChip.Type<>(
            CombatChip::new,
            CombatChip.CODEC,
            CombatChip.STREAM_CODEC.cast()
    ));

    public static void init() {

    }
}
