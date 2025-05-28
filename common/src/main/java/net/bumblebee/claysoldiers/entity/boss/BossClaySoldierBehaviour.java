package net.bumblebee.claysoldiers.entity.boss;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class BossClaySoldierBehaviour {
    public static final Codec<BossClaySoldierBehaviour> CODEC = ModRegistries.BOSS_CLAY_SOLDIER_BEHAVIOURS_REGISTRY.byNameCodec();
    @Nullable
    private final ResourceKey<LootTable> deathLoot;
    private final BossClaySoldierEntity.BossTypes initialType;
    private final EnumSet<BossClaySoldierEntity.BossTypes> allowedTypes;
    private final BiConsumer<BossClaySoldierEntity, DamageSource> onDeath;
    private final Predicate<BossClaySoldierEntity> shouldDie;
    private final BiConsumer<BossClaySoldierEntity, ServerBossEvent> setupBossEvent;
    private final BiConsumer<BossClaySoldierEntity, ServerBossEvent> modifyBossEventProgress;
    private final BiPredicate<BossClaySoldierEntity, DamageSource> onHurt;


    protected BossClaySoldierBehaviour(BossClaySoldierEntity.BossTypes initial, @Nullable ResourceKey<LootTable> deathLoot, BossClaySoldierEntity.BossTypes[] allowedOther, BiConsumer<BossClaySoldierEntity, DamageSource> onDeath, Predicate<BossClaySoldierEntity> shouldDie, BiConsumer<BossClaySoldierEntity, ServerBossEvent> setupBossEvent, BiConsumer<BossClaySoldierEntity, ServerBossEvent> modifyBossEventProgress, BiPredicate<BossClaySoldierEntity, DamageSource> onHurt) {
        this.deathLoot = deathLoot;
        this.onDeath = onDeath;
        this.shouldDie = shouldDie;
        this.setupBossEvent = setupBossEvent;
        this.modifyBossEventProgress = modifyBossEventProgress;
        this.onHurt = onHurt;
        this.allowedTypes = EnumSet.of(initial, allowedOther);
        this.initialType = initial;
    }

    public static Builder of(BossClaySoldierEntity.BossTypes type, @Nullable ResourceKey<LootTable> deathLoot) {
        return new Builder(type, deathLoot);
    }

    /**
     * Called initially when the Boss ist setup with this AI.
     */
    public void setUpBoss(BossClaySoldierEntity boss, ServerBossEvent bossEvent) {
        boss.setBossType(initialType);
        setupBossEvent.accept(boss, bossEvent);

    }

    public void onDeath(BossClaySoldierEntity bossEntity, DamageSource damageSource) {
       onDeath.accept(bossEntity, damageSource);
    }

    public boolean onHurt(BossClaySoldierEntity bossEntity, DamageSource damageSource) {
        return onHurt.test(bossEntity, damageSource);
    }

    /**
     * Called when the Boss is about to die.
     *
     * @return whether the boss should die.
     */
    public boolean shouldDie(BossClaySoldierEntity bossEntity) {
        return shouldDie.test(bossEntity);
    }

    @Nullable
    public ResourceKey<LootTable> getLootTable() {
        return deathLoot;
    }

    public void getBossEventProgress(BossClaySoldierEntity bossEntity, ServerBossEvent event) {
        modifyBossEventProgress.accept(bossEntity, event);
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
        private BiConsumer<BossClaySoldierEntity, ServerBossEvent> setupBossEvent = (b, e) -> {};
        private BiConsumer<BossClaySoldierEntity, ServerBossEvent> modifyBossEventProgress = (b, e) -> e.setProgress(b.getHealth() / b.getMaxHealth());
        private BiPredicate<BossClaySoldierEntity, DamageSource> onHurt = (b, d) -> true;

        public Builder(BossClaySoldierEntity.BossTypes initialType, ResourceKey<LootTable> deathLoot) {
            this.deathLoot = deathLoot;
            this.initialType = initialType;
        }

        public Builder setBossEvent(BiConsumer<BossClaySoldierEntity, ServerBossEvent> modifyBossEvent) {
            this.setupBossEvent = modifyBossEvent;
            return this;
        }

        public Builder setModifyBossEventProgress(BiConsumer<BossClaySoldierEntity, ServerBossEvent> modifyBossEventProgress) {
            this.modifyBossEventProgress = modifyBossEventProgress;
            return this;
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

        public Builder setOnHurt(BiPredicate<BossClaySoldierEntity, DamageSource> onHurt) {
            this.onHurt = onHurt;
            return this;
        }

        public BossClaySoldierBehaviour build() {
            return new BossClaySoldierBehaviour(initialType, deathLoot, allowedBossTypes, onDeath, shouldDie, setupBossEvent, modifyBossEventProgress, onHurt);
        }
    }
}