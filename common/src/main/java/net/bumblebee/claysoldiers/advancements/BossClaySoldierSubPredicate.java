package net.bumblebee.claysoldiers.advancements;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.entity.common.boss.BossClaySoldierBehaviour;
import net.bumblebee.claysoldiers.entity.common.boss.BossClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record BossClaySoldierSubPredicate(Optional<BossClaySoldierBehaviour> type) implements EntitySubPredicate {
    public static final MapCodec<BossClaySoldierSubPredicate> CODEC = ModRegistries.BOSS_CLAY_SOLDIER_BEHAVIOURS_REGISTRY.byNameCodec().optionalFieldOf("boss_behaviour").xmap(BossClaySoldierSubPredicate::new, BossClaySoldierSubPredicate::type);

    public static BossClaySoldierSubPredicate of(BossClaySoldierBehaviour type) {
        return new BossClaySoldierSubPredicate(Optional.of(type));
    }

    @Override
    public MapCodec<? extends EntitySubPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
        if (entity instanceof BossClaySoldierEntity boss) {
            return type.map(t -> boss.getBossAI() == t).orElse(true);
        }
        return false;
    }
}
