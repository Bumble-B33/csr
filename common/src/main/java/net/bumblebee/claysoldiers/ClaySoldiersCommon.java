package net.bumblebee.claysoldiers;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.blueprint.BlueprintData;
import net.bumblebee.claysoldiers.blueprint.BlueprintManger;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunctions;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicates;
import net.bumblebee.claysoldiers.entity.ClayWraithEntity;
import net.bumblebee.claysoldiers.entity.boss.BossBatEntity;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.horse.ClayHorseEntity;
import net.bumblebee.claysoldiers.entity.horse.ClayPegasusEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.networking.*;
import net.bumblebee.claysoldiers.networking.spawnpayloads.ClayBossSpawnPayload;
import net.bumblebee.claysoldiers.networking.spawnpayloads.ClayMobSpawnPayload;
import net.bumblebee.claysoldiers.networking.spawnpayloads.ClaySoldierSpawnPayload;
import net.bumblebee.claysoldiers.platform.services.*;
import net.bumblebee.claysoldiers.soldieritemtypes.SoldierItemType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttacks;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.team.TeamLoyaltyManger;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ClaySoldiersCommon {
    public static final String MOD_ID = "csr";
    public static final String MOD_NAME = "Clay Soldiers Remake";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final String CLAY_SOLDIER_PROPERTY = "clay_soldier_property";
    public static final String CLAY_SOLDIER_PROPERTY_LANG = CLAY_SOLDIER_PROPERTY + "." + MOD_ID + ".";

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IDataMapGetter DATA_MAP = load(IDataMapGetter.class);
    public static final INetworkManger NETWORK_MANGER = load(INetworkManger.class);
    public static final ICommonHooks COMMON_HOOKS = load(ICommonHooks.class);
    public static final AbstractCapabilityManger CAPABILITY_MANGER = load(AbstractCapabilityManger.class);

    public static final GameRules.Key<GameRules.IntegerValue> CLAY_SOLDIER_DROP_RULE = PLATFORM.createIntRule("soldierDropThemSelf", GameRules.Category.DROPS, 50);
    public static final GameRules.Key<GameRules.BooleanValue> CLAY_SOLDIER_INVENTORY_DROP_RULE = PLATFORM.createBoolRule("soldierDropInventory", GameRules.Category.DROPS, true);

    public static final String BLUEPRINT_PACK_PATH = "blueprint_pack";
    public static final String BLUEPRINT_DATA_PACK_LANG = "resourcePack.%s.blueprint.name".formatted(ClaySoldiersCommon.MOD_ID);
    public static final String BLUEPRINT_PACK_DESCRIPTION = "datapack.%s.%s.description".formatted(MOD_ID, BLUEPRINT_PACK_PATH);
    public static final String BLUEPRINT_PACK_SOURCE = "pack.source.%s.%s".formatted(MOD_ID, BLUEPRINT_PACK_PATH);

    public static Predicate<Player> IS_WEARING_GOGGLES = p -> p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.CLAY_GOGGLES.get());
    @Nullable
    public static Supplier<@Nullable Player> clientPlayer;

    public static boolean claySolderMenuModify = false;

    public static void init() {
        ModRegistries.init();
        ModItems.init();
        ModBlocks.init();
        ModBlockEntities.init();

        ModArmorMaterials.init();
        ModDataComponents.init();
        ModEffects.init();
        ModParticles.init();

        ModRecipes.init();
        ModEntityTypes.init();
        ModMenuTypes.init();
        ModCreativeTab.init();
        SoldierPropertyTypes.init();
        SpecialAttacks.init();
        ClayPoiFunctions.init();
        ClayPredicates.init();
        ModItemGenerators.init();
        ModBossBehaviours.init();
        ModPoiTypes.init();

        NETWORK_MANGER.registerS2CPayload(ClayMobItemBreakParticles.ID, ClayMobItemBreakParticles.STREAM_CODEC, ClayMobItemBreakParticles::handleClient);
        NETWORK_MANGER.registerS2CPayload(SoldierItemChangePayload.ID, SoldierItemChangePayload.STREAM_CODEC, SoldierItemChangePayload::handleClient);
        NETWORK_MANGER.registerS2CPayload(ClaySoldierReviveCooldownPayload.ID, ClaySoldierReviveCooldownPayload.STREAM_CODEC, ClaySoldierReviveCooldownPayload::handleClient);
        NETWORK_MANGER.registerS2CPayload(SoldierCarriedChangePayload.ID, SoldierCarriedChangePayload.STREAM_CODEC, SoldierCarriedChangePayload::handleClient);

        NETWORK_MANGER.registerS2CPayload(ClayTeamPlayerDataPayload.Single.ID, ClayTeamPlayerDataPayload.STREAM_CODEC_SINGLE, ClayTeamPlayerDataPayload.Single::handleClient);
        NETWORK_MANGER.registerS2CPayload(ClayTeamPlayerDataPayload.Remove.ID, ClayTeamPlayerDataPayload.STREAM_CODEC_REMOVE, ClayTeamPlayerDataPayload.Remove::handleClient);
        NETWORK_MANGER.registerS2CPayload(ClayTeamPlayerDataPayload.Creation.ID, ClayTeamPlayerDataPayload.STREAM_CODEC_CREATION, ClayTeamPlayerDataPayload.Creation::handleClient);

        NETWORK_MANGER.registerS2CPayload(BlueprintClientPayload.ID, BlueprintClientPayload.STREAM_CODEC, BlueprintClientPayload::handleClient);
        NETWORK_MANGER.registerS2CPayload(CapabilityStatusPayload.ID, CapabilityStatusPayload.STREAM_CODEC, CapabilityStatusPayload::handleClient);

        NETWORK_MANGER.registerS2CPayload(HamsterWheelEnergyPayload.ID, HamsterWheelEnergyPayload.STREAM_CODEC, HamsterWheelEnergyPayload::handleClient);

        NETWORK_MANGER.registerS2CPayload(ClayMobSpawnPayload.ID, ClayMobSpawnPayload.STREAM_CODEC, ClayMobSpawnPayload::handleClient);
        NETWORK_MANGER.registerS2CPayload(ClaySoldierSpawnPayload.ID, ClaySoldierSpawnPayload.STREAM_CODEC, ClaySoldierSpawnPayload::handleClient);
        NETWORK_MANGER.registerS2CPayload(ClayBossSpawnPayload.ID, ClayBossSpawnPayload.STREAM_CODEC, ClayBossSpawnPayload::handleClient);


        NETWORK_MANGER.registerS2CPayload(BlueprintPlacePayload.ID, BlueprintPlacePayload.STREAM_CODEC, BlueprintPlacePayload::handleClient);
    }

    public static void playerJoinsServerEvent(@Nullable ServerPlayer player, Stream<ServerPlayer> relevantPlayers) {
        if (player != null) {
            playerJoinedServer(player, false);
        } else {
            relevantPlayers.forEach(serverPlayer -> playerJoinedServer(serverPlayer, true));
        }
    }

    /**
     * @param player the {@code Player} who should {@code TeamPlayerData} the
     * @param reload whether this was caused by a reload
     */
    public static void playerJoinedServer(ServerPlayer player, boolean reload) {
        sendWhenChannel(player, new ClayTeamPlayerDataPayload.Creation(TeamLoyaltyManger.getTeamData(player.serverLevel()), reload), reload);

        if (!reload) {
            sendWhenChannel(player, new BlueprintClientPayload(BlueprintManger.getBlueprintShapeData(player.registryAccess())), false);
        }

        if (NETWORK_MANGER.isMemoryConnection(player)) {
            LOGGER.info("No data PackSync Payloads will be send as the Connection is Memory for {}", player.getScoreboardName());
            return;
        }

        sendWhenChannel(player, new CapabilityStatusPayload(AbstractCapabilityManger.getEnabledMap()), reload);
    }

    private static void sendWhenChannel(ServerPlayer serverPlayer, CustomPacketPayload payload, boolean reload) {
        if (NETWORK_MANGER.hasChannel(serverPlayer, payload.type())) {
            ClaySoldiersCommon.NETWORK_MANGER.sendToPlayer(serverPlayer, payload);
            LOGGER.info("Sending {} to {} was {}", payload.type().id(), serverPlayer.getScoreboardName(), reload ? "reload" : "join");
        } else {
            LOGGER.error("Could not send {} Packet to {} because there was no Channel", payload.type().id(), serverPlayer.getScoreboardName());
        }
    }

    public static void serverStartedEvent(MinecraftServer server) {
        LOGGER.info("TeamPlayerData on server: {}", TeamLoyaltyManger.getTeamPlayerData(server.overworld()));
        LOGGER.info("Teams loaded: {}", ClayMobTeamManger.getAllKeys(server.registryAccess()).toList());
    }

    public static void entityAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> event) {
        event.accept(ModEntityTypes.CLAY_SOLDIER_ENTITY.get(), AbstractClaySoldierEntity.setSoldierAttributes());
        event.accept(ModEntityTypes.CLAY_WRAITH.get(), ClayWraithEntity.setWraithAttributes());
        event.accept(ModEntityTypes.ZOMBIE_CLAY_SOLDIER_ENTITY.get(), AbstractClaySoldierEntity.setSoldierAttributes());
        event.accept(ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY.get(), AbstractClaySoldierEntity.setSoldierAttributes());
        event.accept(ModEntityTypes.CLAY_HORSE_ENTITY.get(), ClayHorseEntity.createBaseHorseAttributes());
        event.accept(ModEntityTypes.CLAY_PEGASUS_ENTITY.get(), ClayPegasusEntity.createPegasusAttributes());

        event.accept(ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get(), BossClaySoldierEntity.bossAttributes());
        event.accept(ModEntityTypes.VAMPIRE_BAT.get(), BossBatEntity.createBatAttributes());

    }

    public static void registerDynamicRegistry(DynamicRegistryEvent event) {
        event.register(ModRegistries.BLUEPRINTS, BlueprintData.JSON_CODEC, BlueprintData.JSON_CODEC);
        event.register(ModRegistries.SOLDIER_ITEM_TYPES, SoldierItemType.CODEC, null, ((id, location, value) -> value.onRegister(location)));
        event.register(ModRegistries.CLAY_MOB_TEAMS, ClayMobTeam.CODEC_JSON, ClayMobTeam.CODEC_JSON, (id, key, value) -> ClayMobTeamManger.appendFromItemMap(value.getGetFrom(), key));
    }

    public static void onTagLoad(RegistryAccess registryAccess, boolean client) {
        if (!client) {
            var reg = registryAccess.registryOrThrow(ModRegistries.SOLDIER_ITEM_TYPES);
            reg.forEach(type -> type.onTagLoad(tag -> registryAccess.registryOrThrow(Registries.ITEM).getTag(tag)));
            SoldierItemType.postTagLoad(reg.stream());
        }
    }

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

    public interface DynamicRegistryEvent {
        default <T> void register(ResourceKey<Registry<T>> registry, Codec<T> codec, @Nullable Codec<T> synced) {
            register(registry, codec, synced, null);
        }

        <T> void register(ResourceKey<Registry<T>> registry, Codec<T> codec, @Nullable Codec<T> synced, RegistryRegisteredCallBack<T> callback);
    }

    public interface RegistryRegisteredCallBack<T> {
        void onRegister(int id, ResourceLocation location, T value);
    }
}