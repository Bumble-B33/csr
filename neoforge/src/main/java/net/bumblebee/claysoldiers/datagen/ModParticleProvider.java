package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.init.ModParticles;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;

public class ModParticleProvider extends ParticleDescriptionProvider {
    public ModParticleProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, fileHelper);
    }

    @Override
    protected void addDescriptions() {
        sprite(ModParticles.SMALL_HEART_PARTICLE.get(), ResourceLocation.withDefaultNamespace("heart"));
        sprite(ModParticles.SMALL_ANGRY_PARTICLE.get(), ResourceLocation.withDefaultNamespace("angry"));
        sprite(ModParticles.SMALL_HAPPY_PARTICLE.get(), ResourceLocation.withDefaultNamespace("glint"));
        sprite(ModParticles.SMALL_WAXED_PARTICLE.get(), ResourceLocation.withDefaultNamespace("glow"));
    }
}
