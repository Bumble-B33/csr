package net.bumblebee.claysoldiers.integration;

import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.boss.SmartBossClaySoldierEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public interface SBLEntityConstructor {
    static BossClaySoldierEntity create(EntityType<? extends BossClaySoldierEntity> pEntityType, Level pLevel) {
        return new SmartBossClaySoldierEntity(pEntityType, pLevel);
    }
}
