package net.bumblebee.claysoldiers.mobeffects;

import net.bumblebee.claysoldiers.entity.soldier.VampireSubjugate;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ClayVampireConversion extends MobEffect {
    public ClayVampireConversion() {
        super(MobEffectCategory.NEUTRAL, 0x660707);
    }

    @Override
    public void onMobRemoved(LivingEntity pLivingEntity, int pAmplifier, Entity.RemovalReason pReason) {
        if (pReason == Entity.RemovalReason.KILLED) {
            if (pLivingEntity instanceof VampireSubjugate vampireSubjugate) {
                vampireSubjugate.convertToVampire();
            }
        }
    }
}
