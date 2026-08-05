package net.bumblebee.claysoldiers.entity;

import net.bumblebee.claysoldiers.init.ModDamageTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

public class ClayDamageSources extends DamageSources {
    private final Registry<DamageType> damageTypes;

    public ClayDamageSources(RegistryAccess registries) {
        super(registries);
        this.damageTypes = registries.lookupOrThrow(Registries.DAMAGE_TYPE);
    }

    @Override
    public DamageSource mobAttack(LivingEntity mob) {
        return new DamageSource(this.damageTypes.getOrThrow(ModDamageTypes.CLAY_HURT), mob);

    }

    @Override
    public DamageSource onFire() {
        return new DamageSource(this.damageTypes.getOrThrow(ModDamageTypes.CLAY_ON_FIRE));
    }

}
