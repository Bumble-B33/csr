package net.bumblebee.claysoldiers.entity.throwables;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClaySoldierThrownPotion extends ThrownPotion {
    public ClaySoldierThrownPotion(Level pLevel, LivingEntity shooter) {
        this(ModEntityTypes.CLAY_SOLDIER_POTION.get(), pLevel);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1F, shooter.getZ());
        this.setOwner(shooter);
    }

    public ClaySoldierThrownPotion(EntityType<? extends ThrownPotion> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void onHit(HitResult pResult) {
        if (!this.level().isClientSide) {
            ItemStack itemstack = this.getItem();
            var potionContents = itemstack.get(DataComponents.POTION_CONTENTS);
            if (potionContents == null) {
                return;
            }
            if (potionContents.hasEffects()) {
                this.applySplash(potionContents.getAllEffects(), pResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) pResult).getEntity() : null);
            }
            this.discard();
        }
    }

    private void applySplash(Iterable<MobEffectInstance> pEffectInstances, @Nullable Entity pTarget) {
        AABB aabb = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<ClayMobEntity> list = this.level().getEntitiesOfClass(ClayMobEntity.class, aabb).stream().filter((s) -> s.sameTeamAs(this.getOwner())).toList();
        if (!list.isEmpty()) {
            Entity entity = this.getEffectSource();

            for (ClayMobEntity clayMobEntity : list) {
                if (clayMobEntity.isAffectedByPotions()) {
                    double distanceToTarget = this.distanceToSqr(clayMobEntity);
                    if (distanceToTarget < 16.0) {
                        double selfHitDistance;
                        if (clayMobEntity == pTarget) {
                            selfHitDistance = 1.0;
                        } else {
                            selfHitDistance = 1.0 - Math.sqrt(distanceToTarget) / 4.0;
                        }

                        for (MobEffectInstance mobeffectinstance : pEffectInstances) {
                            Holder<MobEffect> mobeffect = mobeffectinstance.getEffect();
                            if (mobeffect.value().isInstantenous()) {
                                mobeffect.value().applyInstantenousEffect(this, this.getOwner(), clayMobEntity, mobeffectinstance.getAmplifier(), selfHitDistance);
                            } else {
                                int durationNew = mobeffectinstance.mapDuration(duration -> (int) (selfHitDistance * (double) duration + 0.5));
                                MobEffectInstance newMobEffectInstance = new MobEffectInstance(
                                        mobeffect, durationNew, mobeffectinstance.getAmplifier(), true, false
                                );
                                if (!newMobEffectInstance.endsWithin(20)) {
                                    clayMobEntity.addEffect(newMobEffectInstance, entity);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}