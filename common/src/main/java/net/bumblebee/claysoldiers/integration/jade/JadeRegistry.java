package net.bumblebee.claysoldiers.integration.jade;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.integration.jade.providers.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class JadeRegistry {
    private static final List<CommonBlockProvider> JADE_BLOCKS = new ArrayList<>();
    private static final List<CommonEntityProvider<?>> JADE_ENTITY = new ArrayList<>();
    private static final List<CommonEntityServerAppender<?>> JADE_ENTITY_SERVER = new ArrayList<>();

    public static final String BASE_LANG = "jade.plugin." + ClaySoldiersCommon.MOD_ID + ".%s.%s";


    public static final ResourceLocation CLAY_MOB = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_mob");
    public static final ResourceLocation CLAY_SOLDIER = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier");
    public static final ResourceLocation ZOMBIE_CLAY_SOLDIER = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "zombie_clay_soldier");
    public static final ResourceLocation CLAY_WRAITH = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_wraith");
    public static final ResourceLocation VAMPIRE_SOLDIER = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "vampire_clay_soldier");

    public static final ResourceLocation EASEL_BLOCK = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "easel_block_storage");
    public static final ResourceLocation HAMSTER_WHEEL_BLOCK = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "hamster_wheel");

    public static final ResourceLocation BOSS_CLAY_SOLDIER = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "boss_clay_soldier");


    static {
        registerEntityProvider(ClayMobProvider.INSTANCE);
        registerEntityProvider(ClaySoldierProvider.INSTANCE);
        registerEntityProvider(VampireClaySoldierProvider.INSTANCE);
        registerEntityProvider(ZombieClaySoldierProvider.INSTANCE);
        registerEntityProvider(ClayWraithProvider.INSTANCE);

        registerEntityProvider(BossClaySoldierProvider.INSTANCE);

        registerEntityServerAppender(ClayWraithProvider.INSTANCE);
        registerEntityServerAppender(ZombieClaySoldierProvider.INSTANCE);



        registerBlockProvider(EaselBlockProvider.INSTANCE);
        registerBlockProvider(HamsterWheelBlockProvider.INSTANCE);
    }

    public static String getLangKey(CommonBlockProvider provider, String suffix) {
        return BASE_LANG.formatted(provider.getUniqueId().getPath(), suffix);
    }
    public static String getLangKey(CommonEntityProvider<?> provider, String suffix) {
        return BASE_LANG.formatted(provider.getUniqueId().getPath(), suffix);
    }

    public static void registerBlockProvider(CommonBlockProvider blockProvider) {
        JADE_BLOCKS.add(blockProvider);
    }

    public static <T extends Entity> void registerEntityProvider(CommonEntityProvider<T> entityProvider) {
        JADE_ENTITY.add(entityProvider);
    }

    public static <T extends Entity> void registerEntityServerAppender(CommonEntityServerAppender<T> serverAppender) {
        JADE_ENTITY_SERVER.add(serverAppender);
    }

    public static List<CommonBlockProvider> getBlocks() {
        return JADE_BLOCKS;
    }

    public static List<CommonEntityProvider<?>> getEntities() {
        return JADE_ENTITY;
    }
    public static List<CommonEntityServerAppender<?>> getServerEntities() {
        return JADE_ENTITY_SERVER;
    }
}
