package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.function.Supplier;

public final class ModParticles {
    public static final Supplier<SimpleParticleType> SMALL_HEART_PARTICLE = ClaySoldiersCommon.PLATFORM.registerParticle("small_heart",
            () -> new ExtSimpleParticleType(false));
    public static final Supplier<SimpleParticleType> SMALL_ANGRY_PARTICLE = ClaySoldiersCommon.PLATFORM.registerParticle("small_angry",
            () -> new ExtSimpleParticleType(false));
    public static final Supplier<SimpleParticleType> SMALL_HAPPY_PARTICLE = ClaySoldiersCommon.PLATFORM.registerParticle("small_happy",
            () -> new ExtSimpleParticleType(false));
    public static final Supplier<SimpleParticleType> SMALL_WAXED_PARTICLE = ClaySoldiersCommon.PLATFORM.registerParticle("small_waxed",
            () -> new ExtSimpleParticleType(false));


    public static void init() {
    }

    private static class ExtSimpleParticleType extends SimpleParticleType {
        protected ExtSimpleParticleType(boolean overrideLimiter) {
            super(overrideLimiter);
        }
    }
}
