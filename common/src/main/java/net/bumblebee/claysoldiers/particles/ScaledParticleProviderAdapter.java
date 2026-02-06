package net.bumblebee.claysoldiers.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;


public record ScaledParticleProviderAdapter(ParticleProvider<SimpleParticleType> provider, float scale) implements ParticleProvider<SimpleParticleType> {
    @Override
    public @Nullable Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, RandomSource randomSource) {
        Particle particle = provider.createParticle(pType, pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, randomSource);
        if (particle != null) {
            particle.scale(scale);
        }
        return particle;
    }
}
