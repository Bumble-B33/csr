package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.mobeffects.ClaySoldierSlimeRootEffect;
import net.bumblebee.claysoldiers.mobeffects.ClayVampireConversion;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

public final class ModEffects {
    public static final Holder<MobEffect> SLIME_ROOT = ClaySoldiersCommon.PLATFORM.registerMobEffect("slime_root",
            ClaySoldierSlimeRootEffect::new);
    public static final Holder<MobEffect> VAMPIRE_CONVERSION = ClaySoldiersCommon.PLATFORM.registerMobEffect("clay_vampire_conversion",
            ClayVampireConversion::new);

    public static void init(){
    }

    private ModEffects() {
    }
}
