package net.bumblebee.claysoldiers;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.block.hammock.SugarCaneHammockBlock;
import net.bumblebee.claysoldiers.blueprint.BlueprintManager;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunctions;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChips;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddons;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicates;
import net.bumblebee.claysoldiers.entity.common.ClayWraithEntity;
import net.bumblebee.claysoldiers.entity.common.boss.BossBatEntity;
import net.bumblebee.claysoldiers.entity.common.boss.BossClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.horse.ClayHorseEntity;
import net.bumblebee.claysoldiers.entity.common.horse.ClayPegasusEntity;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.item.claymobspawn.MultiSpawnItem;
import net.bumblebee.claysoldiers.networking.*;
import net.bumblebee.claysoldiers.networking.spawnpayloads.ClayBossSpawnPayload;
import net.bumblebee.claysoldiers.networking.spawnpayloads.ClaySoldierSpawnPayload;
import net.bumblebee.claysoldiers.networking.spawnpayloads.ProgrammableClaySoldierSpawnPayload;
import net.bumblebee.claysoldiers.platform.services.*;
import net.bumblebee.claysoldiers.recipe.ClientRecipeAccess;
import net.bumblebee.claysoldiers.soldieritemtypes.SoldierItemType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttacks;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.team.loyalty.TeamLoyaltyManger;
import net.bumblebee.claysoldiers.util.ErrorHandler;
import net.minecraft.core.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ClaySoldiersCommon {
    public static final String MOD_ID = "csr";
    public static final String MOD_NAME = "Clay Soldiers Remake";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final ProblemReporter PROBLEM_REPORTER = new ProblemReporter.ScopedCollector(LOGGER);
    public static final String CLAY_SOLDIER_PROPERTY = "clay_soldier_property";
    public static final String CLAY_SOLDIER_PROPERTY_LANG = CLAY_SOLDIER_PROPERTY + "." + MOD_ID + ".";

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IDataMapGetter DATA_MAP = load(IDataMapGetter.class);
    public static final NetworkManger NETWORK_MANGER = load(NetworkManger.class);
    public static final ICommonHooks COMMON_HOOKS = load(ICommonHooks.class);
    public static final AbstractCapabilityManger CAPABILITY_MANGER = load(AbstractCapabilityManger.class);
    public static final IConfig CONFIG = load(IConfig.class);
    public static final ErrorHandler ERROR_HANDLER = new ErrorHandler(LOGGER, PLATFORM.isDevEnv());

    public static final String CSR_DATA_PACK_LOCATION = "data/datapacks";
    public static final String CSR_DEFAULT_DATA_PACK_PATH = "csr_default_datapack";

    public static final String CSR_DEFAULT_DATA_PACK_LANG = "resourcePack.%s.csr_default.name".formatted(ClaySoldiersCommon.MOD_ID);
    public static final String CSR_DEFAULT_PACK_DESCRIPTION = "datapack.%s.%s.description".formatted(MOD_ID, CSR_DEFAULT_DATA_PACK_PATH);

    public final static List<Predicate<Player>> IS_WEARING_GOGGLES = new ArrayList<>(List.of(
            p -> p.getItemBySlot(EquipmentSlot.HEAD).is(ModTags.Items.CLAY_GOGGLES_ITEM)
    ));
    public final static List<Predicate<Player>> IS_WEARING_CLAY_SOLDIER = new ArrayList<>(List.of(
            p -> p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.CLAY_SOLDIER.get())
    ));
    public final static List<Predicate<Player>> IS_WEARING_STATOMETER = new ArrayList<>(List.of(
            p -> p.getOffhandItem().is(ModTags.Items.STAT_ITEM),
            p -> p.getMainHandItem().is(ModTags.Items.STAT_ITEM)
    ));

    @Nullable
    public static Supplier<@Nullable Player> clientPlayer;

    public static final ClientRecipeAccess CLIENT_RECIPE_ACCESS = ClientRecipeAccess.INSTANCE;


    public static void init() {
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
        ModCritirions.init();
        ClaySoldierChips.init();
        ClaySoldierChipAddons.init();

        ModLootTables.init();

        NETWORK_MANGER.registerS2CPayload(ClayMobItemBreakParticles.ID, ClayMobItemBreakParticles.STREAM_CODEC);
        NETWORK_MANGER.registerS2CPayload(SoldierItemChangePayload.ID, SoldierItemChangePayload.STREAM_CODEC);
        NETWORK_MANGER.registerS2CPayload(ClaySoldierReviveCooldownPayload.ID, ClaySoldierReviveCooldownPayload.STREAM_CODEC);
        NETWORK_MANGER.registerS2CPayload(SoldierCarriedChangePayload.ID, SoldierCarriedChangePayload.STREAM_CODEC);
        NETWORK_MANGER.registerS2CPayload(ClaySoldierChipUpdatePayload.ID, ClaySoldierChipUpdatePayload.STREAM_CODEC);

        NETWORK_MANGER.registerS2CPayload(ClayTeamPlayerDataPayload.Single.ID, ClayTeamPlayerDataPayload.STREAM_CODEC_SINGLE);
        NETWORK_MANGER.registerS2CPayload(ClayTeamPlayerDataPayload.Remove.ID, ClayTeamPlayerDataPayload.STREAM_CODEC_REMOVE);
        NETWORK_MANGER.registerS2CPayload(ClayTeamPlayerDataPayload.Creation.ID, ClayTeamPlayerDataPayload.STREAM_CODEC_CREATION);

        NETWORK_MANGER.registerS2CPayload(BlueprintClientPayload.ID, BlueprintClientPayload.STREAM_CODEC);
        NETWORK_MANGER.registerS2CPayload(CapabilityStatusPayload.ID, CapabilityStatusPayload.STREAM_CODEC);

        NETWORK_MANGER.registerS2CPayload(HamsterWheelEnergyPayload.ID, HamsterWheelEnergyPayload.STREAM_CODEC);

        NETWORK_MANGER.registerS2CPayload(ClaySoldierSpawnPayload.ID, ClaySoldierSpawnPayload.STREAM_CODEC);
        NETWORK_MANGER.registerS2CPayload(ClayBossSpawnPayload.ID, ClayBossSpawnPayload.STREAM_CODEC);
        NETWORK_MANGER.registerS2CPayload(ProgrammableClaySoldierSpawnPayload.ID, ProgrammableClaySoldierSpawnPayload.STREAM_CODEC);

        NETWORK_MANGER.registerS2CPayload(ChipAssemblyEnergyPayload.ID, ChipAssemblyEnergyPayload.STREAM_CODEC);

        NETWORK_MANGER.registerS2CPayload(BlueprintPlacePayload.ID, BlueprintPlacePayload.STREAM_CODEC);
    }

    public static void playerJoinsServerEvent(@Nullable ServerPlayer player, Stream<ServerPlayer> relevantPlayers) {
        if (player != null) {
            playerJoinedServer(player, false);
        } else {
            relevantPlayers.forEach(serverPlayer -> playerJoinedServer(serverPlayer, true));
        }
    }

    public static void registerDispenseBehavior() {
        MultiSpawnItem.registerDispenseBehavior(ModItems.CLAY_SOLDIER.get());
        MultiSpawnItem.registerDispenseBehavior(ModItems.CAKE_HORSE.get());
        MultiSpawnItem.registerDispenseBehavior(ModItems.GRASS_HORSE.get());
        MultiSpawnItem.registerDispenseBehavior(ModItems.SNOW_HORSE.get());
        MultiSpawnItem.registerDispenseBehavior(ModItems.MYCELIUM_HORSE.get());

        MultiSpawnItem.registerDispenseBehavior(ModItems.CAKE_PEGASUS.get());
        MultiSpawnItem.registerDispenseBehavior(ModItems.GRASS_PEGASUS.get());
        MultiSpawnItem.registerDispenseBehavior(ModItems.SNOW_PEGASUS.get());
        MultiSpawnItem.registerDispenseBehavior(ModItems.MYCELIUM_PEGASUS.get());
    }

    /**
     * @param player the {@code Player} who should {@code TeamPlayerData} the
     * @param reload whether this was caused by a reload
     */
    public static void playerJoinedServer(ServerPlayer player, boolean reload) {
        sendWhenChannel(player, new ClayTeamPlayerDataPayload.Creation(TeamLoyaltyManger.getTeamData(player.level()), reload), reload);

        if (!reload) {
            sendWhenChannel(player, new BlueprintClientPayload(BlueprintManager.getBlueprintShapeData(player.registryAccess())), false);
        }

        if (NETWORK_MANGER.isMemoryConnection(player)) {
            LOGGER.info("No data PackSync Payloads will be send as the Connection is Memory for {}", player.getScoreboardName());
            return;
        }

        sendWhenChannel(player, new CapabilityStatusPayload(AbstractCapabilityManger.getEnabledMap()), reload);
    }

    public static void sendWhenChannel(ServerPlayer serverPlayer, CustomPacketPayload payload, boolean reload) {
        if (NETWORK_MANGER.hasChannel(serverPlayer, payload.type())) {
            NETWORK_MANGER.sendToPlayer(serverPlayer, payload);
            LOGGER.info("Sending {} to {} was {}", payload.type().id(), serverPlayer.getScoreboardName(), reload ? "reload" : "join");
        } else {
            LOGGER.error("Could not send {} Packet to {} because there was no Channel", payload.type().id(), serverPlayer.getScoreboardName());
        }
    }

    public static void serverStartedEvent(MinecraftServer server) {
        LOGGER.info("TeamPlayerData on server: {}", TeamLoyaltyManger.getTeamPlayerData(server.overworld()));
        LOGGER.info("Teams loaded: {}", ClayMobTeamManger.getAll(server.registryAccess()).map(Holder::getRegisteredName).toList());
    }

    public static void entityAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> event) {
        event.accept(ModEntityTypes.CLAY_SOLDIER_ENTITY.get(), AbstractClaySoldierEntity.createSoldierAttributes().build());
        event.accept(ModEntityTypes.CLAY_WRAITH.get(), ClayWraithEntity.setWraithAttributes());
        event.accept(ModEntityTypes.ZOMBIE_CLAY_SOLDIER_ENTITY.get(), AbstractClaySoldierEntity.createSoldierAttributes().build());
        event.accept(ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY.get(), AbstractClaySoldierEntity.createSoldierAttributes().build());
        event.accept(ModEntityTypes.CLAY_HORSE_ENTITY.get(), ClayHorseEntity.createBaseHorseAttributes());
        event.accept(ModEntityTypes.CLAY_PEGASUS_ENTITY.get(), ClayPegasusEntity.createPegasusAttributes());

        event.accept(ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get(), BossClaySoldierEntity.bossAttributes());
        event.accept(ModEntityTypes.PROGRAMMABLE_CLAY_SOLDIER_ENTITY.get(), ProgrammableClaySoldierEntity.createSoldierAttributes().build());
        event.accept(ModEntityTypes.VAMPIRE_BAT.get(), BossBatEntity.createBatAttributes());

    }


    public static void onTagLoad(HolderLookup.Provider registryAccess, boolean client) {
        if (!client) {
            SoldierItemType.onTagLoad(registryAccess);
        }
    }

    public static void setClientRecipes(Collection<? extends RecipeHolder<?>> recipes) {
        ClaySoldiersCommon.CLIENT_RECIPE_ACCESS.fill(recipes);
    }

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

    public interface DynamicRegistryEvent {
        default <T> void register(ResourceKey<Registry<T>> registry, Codec<T> codec, @Nullable Codec<T> synced) {
            register(registry, codec, synced, null);
        }

        <T> void register(ResourceKey<Registry<T>> registry, Codec<T> codec, @Nullable Codec<T> synced, @Nullable RegistryRegisteredCallBack<T> callback);
    }

    public interface RegistryRegisteredCallBack<T> {
        void onRegister(int id, Identifier location, T value);
    }

    public static class BlueprintTagLoad {
        private BlueprintManager manger;
        private ResourceManager resourceManager;

        public void accept(BlueprintManager manger, ResourceManager resourceManager) {
            this.manger = manger;
            this.resourceManager = resourceManager;
        }

        public void onTagLoad(HolderLookup.Provider registries) {
            if (manger == null) {
                if (ClaySoldiersCommon.PLATFORM.isClient()) {
                    return;
                } else {
                    throw new IllegalStateException("Cannot load Blueprint Tags on the Client");
                }
            }
            manger.onTagLoad(resourceManager, registries);
            manger = null;
            resourceManager = null;
        }
    }


    public static Optional<InteractionResult> useItemOnBlockEvent(Player player, Level level, InteractionHand hand, BlockPos pos) {
        return SugarCaneHammockBlock.onSugarCaneUse(player, level, hand, pos);
    }
}