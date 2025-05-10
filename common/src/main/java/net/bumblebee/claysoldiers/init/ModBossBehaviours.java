package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.boss.BossBatEntity;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierBehaviour;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ModBossBehaviours {
    public static final ResourceKey<LootTable> DEFAULT_LOOT_TABLE = createBossLootTable("default");
    public static final ResourceKey<LootTable> VAMPIRE_LOOT_TABLE = createBossLootTable("vampire");


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

    private static ResourceKey<LootTable> createBossLootTable(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "boss_clay_soldier/%s".formatted(name)));
    }

    public static void init() {
    }
}
