package net.bumblebee.claysoldiers.entity.boss;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.init.ModBossBehaviours;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.soldierproperties.SoldierProperty;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public enum ClaySoldierBossEquipment {
    NORMAL(ModBossBehaviours.DEFAULT, Map.of(), () -> SoldierPropertyMap.of(
            new SoldierProperty<>(SoldierPropertyTypes.SIZE.get(), 5f),
            SoldierPropertyTypes.ATTACK_RANGE.get().createProperty(1f)
    )),
    VAMPIRE(ModBossBehaviours.VAMPIRE, Map.of(), () -> SoldierPropertyMap.of(
            new SoldierProperty<>(SoldierPropertyTypes.SIZE.get(), 4f),
            new SoldierProperty<>(SoldierPropertyTypes.ATTACK_RANGE.get(), 1f)
    )),
    ZOMBIE(ModBossBehaviours.ZOMBIE, Map.of(), () -> SoldierPropertyMap.of(
            new SoldierProperty<>(SoldierPropertyTypes.SIZE.get(), 4f),
            new SoldierProperty<>(SoldierPropertyTypes.ATTACK_RANGE.get(), 1f)
    )),
    RANDOM((boss, weight, team, waxed) -> setUpRandom(boss, weight));

    private final SetupFunction generator;

    ClaySoldierBossEquipment(Supplier<BossClaySoldierBehaviour> bossAI, Map<SoldierEquipmentSlot, Supplier<ItemStack>> equipment, Supplier<SoldierPropertyMap> baseProperties) {
        this((boss, weight, team, waxed) -> {
            boss.setBaseProperties(baseProperties.get());
            if (team == null) {
                team = Util.getRandomSafe(
                        boss.registryAccess().registryOrThrow(ModRegistries.CLAY_MOB_TEAMS).keySet().stream().filter(t -> !t.equals(ClayMobTeamManger.NO_TEAM_TYPE)).toList(), boss.getRandom()
                ).orElse(ClayMobTeamManger.DEFAULT_TYPE);
            }
            boss.setClayTeamType(team);
            equipment.forEach((slot, item) -> boss.setItemSlot(slot, item.get()));
            boss.setWaxed(waxed);
            boss.setBossAI(bossAI.get());
        });
    }

    ClaySoldierBossEquipment(SetupFunction generator) {
        this.generator = generator;
    }

    public void setUp(BossClaySoldierEntity boss, int weight, @Nullable ResourceLocation team, boolean waxed) {
        generator.setUp(boss, weight, team, waxed);
    }

    public static void setUpRandom(BossClaySoldierEntity boss, int weight) {
        RandomSource random = boss.getRandom();


        ResourceLocation team = Util.getRandomSafe(
                boss.registryAccess().registryOrThrow(ModRegistries.CLAY_MOB_TEAMS).keySet().stream().filter(t -> !t.equals(ClayMobTeamManger.NO_TEAM_TYPE)).toList(), random
        ).orElse(ClayMobTeamManger.DEFAULT_TYPE);


        boss.setBossAI(Util.getRandom(List.of(ModBossBehaviours.VAMPIRE, ModBossBehaviours.DEFAULT), random).get());
        boss.setClayTeamType(team);
        if (weight <= 0) {
            return;
        }
        if (random.nextBoolean()) {
            weight--;
            boss.setWaxed(true);
        }

        for (SoldierEquipmentSlot slot : SoldierEquipmentSlot.values()) {
            if (weight <= 0) {
                break;
            }
            if (random.nextBoolean()) {
                var opt = Util.getRandomSafe(ClaySoldiersCommon.DATA_MAP.getHoldableEffectForSlot(slot), random);
                if (opt.isPresent() && opt.get().getDefaultInstance().is(ModTags.Items.SOLDIER_BOSS_EQUIPABLE)) {
                    boss.setItemSlot(slot, opt.orElseThrow().getDefaultInstance());
                    weight--;
                    continue;
                }

                var opt2 = Util.getRandomSafe(ClaySoldiersCommon.DATA_MAP.getHoldableEffectForSlot(slot), random);
                if (opt2.isPresent() && opt2.get().getDefaultInstance().is(ModTags.Items.SOLDIER_BOSS_EQUIPABLE)) {
                    boss.setItemSlot(slot, opt2.orElseThrow().getDefaultInstance());
                    weight--;
                }


            }
        }

        var map = RandomSoldierPropertyGenerator.generateRandom(random, weight + 3, RandomSoldierPropertyGenerator.BOSS);

        RandomSoldierPropertyGenerator.generateRandom(random, RandomSoldierPropertyGenerator.BOSS_WEIGHTLESS.size(), RandomSoldierPropertyGenerator.BOSS_WEIGHTLESS).forEach(map::addPropertyForce);

        boss.setBaseProperties(map);
    }

    private interface SetupFunction {
        void setUp(BossClaySoldierEntity boss, int weight, @Nullable ResourceLocation team, boolean waxed);
    }
}
