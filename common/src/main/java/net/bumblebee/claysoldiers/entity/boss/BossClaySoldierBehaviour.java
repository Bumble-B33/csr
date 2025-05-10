package net.bumblebee.claysoldiers.entity.boss;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.EnumSet;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class BossClaySoldierBehaviour {
    public static final Codec<BossClaySoldierBehaviour> CODEC = ModRegistries.BOSS_CLAY_SOLDIER_BEHAVIOURS_REGISTRY.byNameCodec();
    private final ResourceKey<LootTable> deathLoot;
    private final BossClaySoldierEntity.BossTypes initialType;
    private final EnumSet<BossClaySoldierEntity.BossTypes> allowedTypes;
    private final BiConsumer<BossClaySoldierEntity, DamageSource> onDeath;
    private final Predicate<BossClaySoldierEntity> shouldDie;

    protected BossClaySoldierBehaviour(BossClaySoldierEntity.BossTypes initial, ResourceKey<LootTable> deathLoot, BossClaySoldierEntity.BossTypes[] allowedOther, BiConsumer<BossClaySoldierEntity, DamageSource> onDeath, Predicate<BossClaySoldierEntity> shouldDie) {
        this.deathLoot = deathLoot;
        this.onDeath = onDeath;
        this.shouldDie = shouldDie;
        this.allowedTypes = EnumSet.of(initial, allowedOther);
        this.initialType = initial;
    }

    public static Builder of(BossClaySoldierEntity.BossTypes type, ResourceKey<LootTable> deathLoot) {
        return new Builder(type, deathLoot);
    }

    /**
     * Called initially when the Boss ist setup with this AI.
     */
    public void setUpBoss(BossClaySoldierEntity boss) {
        boss.setBossType(initialType);
    }

    public void onDeath(BossClaySoldierEntity bossEntity, DamageSource damageSource) {
       onDeath.accept(bossEntity, damageSource);
    }
    /**
     * Called when the Boss is about to die.
     *
     * @return whether the boss should die.
     */
    public boolean shouldDie(BossClaySoldierEntity bossEntity) {
        return shouldDie.test(bossEntity);
    }

    public ResourceKey<LootTable> getLootTable() {
        return deathLoot;
    }


    public boolean isAllowed(BossClaySoldierEntity.BossTypes type) {
        return allowedTypes.contains(type);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public static class Builder {
        private final ResourceKey<LootTable> deathLoot;
        private final BossClaySoldierEntity.BossTypes initialType;
        private BiConsumer<BossClaySoldierEntity, DamageSource> onDeath = (b, d) -> {};
        private Predicate<BossClaySoldierEntity> shouldDie = (b) -> true;
        private BossClaySoldierEntity.BossTypes[] allowedBossTypes = new BossClaySoldierEntity.BossTypes[0];

        public Builder(BossClaySoldierEntity.BossTypes initialType, ResourceKey<LootTable> deathLoot) {
            this.deathLoot = deathLoot;
            this.initialType = initialType;
        }

        public Builder setOnDeath(BiConsumer<BossClaySoldierEntity, DamageSource> onDeath) {
            this.onDeath = onDeath;
            return this;
        }

        public Builder setShouldDie(Predicate<BossClaySoldierEntity> shouldDie) {
            this.shouldDie = shouldDie;

            return this;
        }

        public Builder setAllowedBossTypes(BossClaySoldierEntity.BossTypes... allowdBossTypes) {
            this.allowedBossTypes = allowdBossTypes;
            return this;
        }

        public BossClaySoldierBehaviour build() {
            return new BossClaySoldierBehaviour(initialType, deathLoot, allowedBossTypes, onDeath, shouldDie);
        }
    }
}