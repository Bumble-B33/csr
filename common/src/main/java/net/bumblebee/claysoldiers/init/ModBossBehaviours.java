package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.boss.BossBatEntity;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierBehaviour;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierEntity;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ModBossBehaviours {
    public static final ResourceKey<LootTable> DEFAULT_LOOT_TABLE = createBossLootTable("default");
    public static final ResourceKey<LootTable> VAMPIRE_LOOT_TABLE = createBossLootTable("vampire");
    public static final ResourceKey<LootTable> ZOMBIE_LOOT_TABLE = createBossLootTable("zombie");

    public static final Supplier<BossClaySoldierBehaviour> DEFAULT = ClaySoldiersCommon.PLATFORM.registerClayBossBehaviour("default",
            () -> BossClaySoldierBehaviour.of(BossClaySoldierEntity.BossTypes.NORMAL, DEFAULT_LOOT_TABLE)
                    .setAllowedBossTypes(BossClaySoldierEntity.BossTypes.ZOMBIE)
                    .setOnDeath((boss, damageSource) -> boss.getBossDeathLoot(damageSource).forEach(boss::spawnAtLocation))
                    .setShouldDie(boss -> {
                        if (boss.getBossType() == BossClaySoldierEntity.BossTypes.NORMAL) {
                            boss.setHealth(boss.getMaxHealth());
                            boss.setBossType(BossClaySoldierEntity.BossTypes.ZOMBIE);
                            return false;
                        }
                        return true;
                    })
                    .setBossEvent((boss, bar) -> bar.setColor(BossEvent.BossBarColor.BLUE))
                    .build());

    public static final Supplier<BossClaySoldierBehaviour> VAMPIRE = ClaySoldiersCommon.PLATFORM.registerClayBossBehaviour("vampire",
            () -> BossClaySoldierBehaviour.of(BossClaySoldierEntity.BossTypes.VAMPIRE, VAMPIRE_LOOT_TABLE)
                    .setOnDeath((boss, damageSource) -> {
                        if (boss.level() instanceof ServerLevel serverLevel) {
                            BlockPos pos = boss.blockPosition();
                            List<ItemStack> loot = boss.getBossDeathLoot(damageSource);
                            if (loot.isEmpty()) {
                                return;
                            }
                            createBat(List.of(loot.getFirst()), serverLevel, pos);

                            List<ItemStack>[] lists = new List[]{new ArrayList<ItemStack>(), new ArrayList<ItemStack>(), new ArrayList<ItemStack>()};
                            for (int i = 1; i < loot.size(); i++) {
                                lists[i % 3].add(loot.get(i));
                            }
                            for (List<ItemStack> list : lists) {
                                createBat(list, serverLevel, pos);
                            }


                        } else {
                            Vec3 vec3 = boss.position();

                            for (int i = 0; i < 30; i++) {
                                boss.level().addParticle(ParticleTypes.ASH,
                                        vec3.x,
                                        vec3.y + 0.5,
                                        vec3.z,
                                        0, 0.1, 0);
                            }
                        }

                    })
                    .setBossEvent((boss, bar) -> bar.setColor(BossEvent.BossBarColor.RED))
                    .build());

    public static final Supplier<BossClaySoldierBehaviour> ZOMBIE = ClaySoldiersCommon.PLATFORM.registerClayBossBehaviour("zombie",
            () -> BossClaySoldierBehaviour.of(BossClaySoldierEntity.BossTypes.ZOMBIE, ZOMBIE_LOOT_TABLE)
                    .setBossEvent((b, e) -> e.setColor(BossEvent.BossBarColor.GREEN))
                    .setOnHurt((boss, damage) -> {
                        if (damage.is(DamageTypes.GENERIC_KILL) || damage.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                            return true;
                        }

                        if (boss.getMinionCount() > 0) {
                            return false;
                        }
                        if (boss.getPhaseCompleted() <= 0 && boss.getHealth() < boss.getMaxHealth() * 0.5f && boss.level() instanceof ServerLevel serverLevel) {
                            spawnZombieMinions(boss, serverLevel);
                            boss.completePhase();
                        }
                        return true;
                    })
                    .setModifyBossEventProgress((boss, event) -> {
                        if (boss.getMinionCount() > 0) {
                            event.setOverlay(BossEvent.BossBarOverlay.NOTCHED_6);
                            event.setProgress(boss.getMinionCount() / 6f);
                        } else {
                            event.setOverlay(BossEvent.BossBarOverlay.PROGRESS);
                            event.setProgress(boss.getHealth() / boss.getMaxHealth());
                        }
                    })
                    .setOnDeath((boss, damageSource) -> boss.getBossDeathLoot(damageSource).forEach(boss::spawnAtLocation))
                    .build());

    public static final Supplier<BossClaySoldierBehaviour> ZOMBIE_MINION = ClaySoldiersCommon.PLATFORM.registerClayBossBehaviour("zombie_minion",
            () -> BossClaySoldierBehaviour.of(BossClaySoldierEntity.BossTypes.ZOMBIE, null)
                    .setOnDeath((boss, damageSource) -> boss.notifyMinionOwnerOfDeath())
                    .setBossEvent((boss, bar) -> bar.setVisible(false))
                    .build());


    private ModBossBehaviours() {
    }

    private static void createBat(List<ItemStack> loot, ServerLevel level, BlockPos pos) {
        if (loot.isEmpty()) {
            return;
        }
        BossBatEntity bat = ModEntityTypes.VAMPIRE_BAT.get().spawn(level, pos, MobSpawnType.MOB_SUMMONED);
        if (bat != null) {
            bat.setLoot(loot);
        }
    }

    private static void spawnZombieMinions(BossClaySoldierEntity boss, ServerLevel level) {
        int size = 6;
        List<BossClaySoldierEntity> minions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            BossClaySoldierEntity minion = ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get().create(level);
            if (minion == null) {
                return;
            }
            minion.setBossAI(ZOMBIE_MINION.get());
            minion.setBaseProperties(SoldierPropertyMap.of(
                    SoldierPropertyTypes.PROTECTION.get().createProperty(-3f))
            );
            minion.getAttributes().getInstance(Attributes.MAX_HEALTH).setBaseValue(10f);
            minion.getAttributes().getInstance(Attributes.ARMOR).setBaseValue(0);
            minion.setMinionOwner(boss);


            minion.moveTo(
                    boss.position().x + (level.random.nextFloat()),
                    boss.position().y,
                    boss.position().z + (level.random.nextFloat()),
                    Mth.wrapDegrees(level.random.nextFloat() * 360.0F),
                    0);
            minion.yHeadRot = minion.getYRot();
            minion.yBodyRot = minion.getYRot();
            minion.setClayTeamType(boss.getClayTeamType());
            minions.add(minion);

            level.addFreshEntity(minion);
        }
        boss.addAllMinions(minions);
    }

    private static ResourceKey<LootTable> createBossLootTable(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "boss_clay_soldier/%s".formatted(name)));
    }

    public static void init() {
    }
}
