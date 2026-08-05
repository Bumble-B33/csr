package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.init.ModParticles;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;

public class ModParticleProvider extends ParticleDescriptionProvider {
    public ModParticleProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addDescriptions() {
        spriteSet(ModParticles.SMALL_HEART_PARTICLE.get(), Identifier.withDefaultNamespace("heart"));
        spriteSet(ModParticles.SMALL_ANGRY_PARTICLE.get(), Identifier.withDefaultNamespace("angry"));
        spriteSet(ModParticles.SMALL_HAPPY_PARTICLE.get(), Identifier.withDefaultNamespace("glint"));
        spriteSet(ModParticles.SMALL_WAXED_PARTICLE.get(), Identifier.withDefaultNamespace("glow"));
        spriteSet(ModParticles.CHARGING_PARTICLE.get(), Identifier.withDefaultNamespace("shriek"));
    }
}
