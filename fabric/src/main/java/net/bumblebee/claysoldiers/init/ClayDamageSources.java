package net.bumblebee.claysoldiers.init;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.LivingEntity;

public class ClayDamageSources extends DamageSources {
    public ClayDamageSources(RegistryAccess registry) {
        super(registry);
    }

    @Override
    public DamageSource mobAttack(LivingEntity pMob) {
        return this.source(ModDamageTypes.CLAY_HURT, pMob);
    }

    @Override
    public DamageSource onFire() {
        return this.source(ModDamageTypes.CLAY_ON_FIRE);
    }

}
