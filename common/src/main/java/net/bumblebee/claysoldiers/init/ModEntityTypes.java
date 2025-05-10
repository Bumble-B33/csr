package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayWraithEntity;
import net.bumblebee.claysoldiers.entity.boss.BossBatEntity;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.boss.ClayBlockProjectileEntity;
import net.bumblebee.claysoldiers.entity.horse.ClayHorseEntity;
import net.bumblebee.claysoldiers.entity.horse.ClayPegasusEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.ClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.VampireClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.ZombieClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.throwables.ClaySoldierSnowball;
import net.bumblebee.claysoldiers.entity.throwables.ClaySoldierThrowableItemEntity;
import net.bumblebee.claysoldiers.entity.throwables.ClaySoldierThrownPotion;
import net.bumblebee.claysoldiers.integration.ExternalMods;
import net.bumblebee.claysoldiers.integration.SBLEntityConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public class ModEntityTypes {
    public static final Supplier<EntityType<ClaySoldierEntity>> CLAY_SOLDIER_ENTITY =
            ClaySoldiersCommon.PLATFORM.registerEntity("clay_soldier", () -> EntityType.Builder.of(ClaySoldierEntity::new, MobCategory.CREATURE)
                    .sized(0.6F * AbstractClaySoldierEntity.DEFAULT_SCALE, 1.95F * AbstractClaySoldierEntity.DEFAULT_SCALE)
                    .ridingOffset(-0.7F * AbstractClaySoldierEntity.DEFAULT_SCALE)
                    .build(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier").toString())
            );

    public static final Supplier<EntityType<ZombieClaySoldierEntity>> ZOMBIE_CLAY_SOLDIER_ENTITY =
            ClaySoldiersCommon.PLATFORM.registerEntity("zombie_clay_soldier", () -> EntityType.Builder.of(ZombieClaySoldierEntity::new, MobCategory.CREATURE)
                    .sized(0.6F * AbstractClaySoldierEntity.DEFAULT_SCALE, 1.95F * AbstractClaySoldierEntity.DEFAULT_SCALE)
                    .ridingOffset(-0.7F * AbstractClaySoldierEntity.DEFAULT_SCALE)
                    .build(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "zombie_clay_soldier").toString())
            );
    public static final Supplier<EntityType<VampireClaySoldierEntity>> VAMPIRE_CLAY_SOLDIER_ENTITY =
            ClaySoldiersCommon.PLATFORM.registerEntity("vampire_clay_soldier", () -> EntityType.Builder.of(VampireClaySoldierEntity::new, MobCategory.CREATURE)
                    .sized(0.6F * AbstractClaySoldierEntity.DEFAULT_SCALE, 1.95F * AbstractClaySoldierEntity.DEFAULT_SCALE)
                    .ridingOffset(-0.7F * AbstractClaySoldierEntity.DEFAULT_SCALE)
                    .build(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "vampire_clay_soldier").toString())
            );

    public static final Supplier<EntityType<BossClaySoldierEntity>> BOSS_CLAY_SOLDIER_ENTITY =
            ClaySoldiersCommon.PLATFORM.registerEntity("boss_clay_soldier", () -> EntityType.Builder.of(
                            (EntityType.EntityFactory<BossClaySoldierEntity>) (entityType, level) -> ExternalMods.SBL.ifLoadedOrElse(() -> SBLEntityConstructor.create(entityType, level), new BossClaySoldierEntity(entityType, level)), MobCategory.CREATURE)
                    .sized(0.6F * AbstractClaySoldierEntity.DEFAULT_SCALE, 1.95F * AbstractClaySoldierEntity.DEFAULT_SCALE)
                    .ridingOffset(-0.7F * AbstractClaySoldierEntity.DEFAULT_SCALE)
                    .build(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "boss_clay_soldier").toString())
            );

    public static final Supplier<EntityType<ClayHorseEntity>> CLAY_HORSE_ENTITY =
            ClaySoldiersCommon.PLATFORM.registerEntity("clay_horse", () -> EntityType.Builder.of(ClayHorseEntity::new, MobCategory.CREATURE)
                    .sized(1.4F * ClayHorseEntity.SCALE, 1.4F * ClayHorseEntity.SCALE)
                    .clientTrackingRange(10)
                    .eyeHeight(1.52F * ClayHorseEntity.SCALE)
                    .build(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_horse").toString())
            );

    public static final Supplier<EntityType<ClayPegasusEntity>> CLAY_PEGASUS_ENTITY =
            ClaySoldiersCommon.PLATFORM.registerEntity("clay_pegasus", () -> EntityType.Builder.of(ClayPegasusEntity::new, MobCategory.MONSTER)
                    .sized(1.4F * ClayHorseEntity.SCALE, 1.4F * ClayHorseEntity.SCALE)
                    .clientTrackingRange(10)
                    .eyeHeight(1.52F * ClayPegasusEntity.DEFAULT_SCALE)
                    .build(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_pegasus").toString())
            );

    public static final Supplier<EntityType<ClaySoldierThrowableItemEntity>> CLAY_SOLDIER_THROWABLE_ITEM =
            ClaySoldiersCommon.PLATFORM.registerEntity("clay_soldier_thrown", () -> EntityType.Builder.<ClaySoldierThrowableItemEntity>of(ClaySoldierThrowableItemEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10)
                    .build(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_thrown").toString())
            );
    public static final Supplier<EntityType<ClaySoldierThrownPotion>> CLAY_SOLDIER_POTION =
            ClaySoldiersCommon.PLATFORM.registerEntity("clay_soldier_potion", () -> EntityType.Builder.<ClaySoldierThrownPotion>of(ClaySoldierThrownPotion::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10)
                    .noSummon()
                    .build(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_potion").toString())
            );
    public static final Supplier<EntityType<ClaySoldierSnowball>> CLAY_SOLDIER_SNOWBALL =
            ClaySoldiersCommon.PLATFORM.registerEntity("clay_soldier_snowball", () -> EntityType.Builder.<ClaySoldierSnowball>of(ClaySoldierSnowball::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10)
                    .noSummon()
                    .build(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_snowball").toString())
            );

    public static final Supplier<EntityType<ClayBlockProjectileEntity>> CLAY_BLOCK_PROJECTILE = ClaySoldiersCommon.PLATFORM.registerEntity(
            "clay_block_projectile", () -> EntityType.Builder.<ClayBlockProjectileEntity>of(ClayBlockProjectileEntity::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F).eyeHeight(0.3125F / 2F).clientTrackingRange(4).updateInterval(10)
                    .build(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_block_projectile").toString())
    );


    public static final Supplier<EntityType<ClayWraithEntity>> CLAY_WRAITH =
            ClaySoldiersCommon.PLATFORM.registerEntity("clay_wraith", () -> EntityType.Builder.of(ClayWraithEntity::new, MobCategory.MISC)
                    .sized(0.6F * AbstractClaySoldierEntity.DEFAULT_SCALE, 0.35f).fireImmune()
                    .build(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_wraith").toString())
            );

    public static final Supplier<EntityType<BossBatEntity>> VAMPIRE_BAT =
            ClaySoldiersCommon.PLATFORM.registerEntity("vampire_bat", () -> EntityType.Builder.of(BossBatEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.9F).eyeHeight(0.45F).clientTrackingRange(5)
                    .build(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "vampire_bat").toString())
            );

    public static void init(){
    }

    private ModEntityTypes() {
    }
}
