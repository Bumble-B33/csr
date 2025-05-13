package net.bumblebee.claysoldiers;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.block.blueprint.EaselBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.BatteryProperty;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.blueprint.BlueprintManger;
import net.bumblebee.claysoldiers.capability.AssignablePoiCapability;
import net.bumblebee.claysoldiers.capability.BlueprintRequestHandler;
import net.bumblebee.claysoldiers.commands.ClaySoldierCommands;
import net.bumblebee.claysoldiers.commands.ColorHelperArgumentType;
import net.bumblebee.claysoldiers.commands.DefaultedResourceLocationArgument;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.integration.ExternalMods;
import net.bumblebee.claysoldiers.integration.accessories.ModAccessories;
import net.bumblebee.claysoldiers.networking.ConfigSyncPayload;
import net.bumblebee.claysoldiers.networking.DataMapPayloadBuilder;
import net.bumblebee.claysoldiers.platform.FabricCapabilityManger;
import net.bumblebee.claysoldiers.platform.FabricCommonHooks;
import net.bumblebee.claysoldiers.platform.FabricDataMapGetter;
import net.bumblebee.claysoldiers.platform.FabricNetworkManger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public class ClaySoldierFabric implements ModInitializer {
    private static final ResourceLocation BLUEPRINT_ID = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "csr_blueprint");
    public static final ResourceLocation BLUEPRINT_PACK_ID = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, ClaySoldiersCommon.BLUEPRINT_PACK_PATH);
    public static final ResourceLocation CSR_DEFAULT_PACK_ID = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, ClaySoldiersCommon.CSR_DEFAULT_DATA_PACK_PATH);


    public static final BlockApiLookup<BlueprintRequestHandler, Void> BLUEPRINT_REQUEST_HANDLER_LOOKUP =
            BlockApiLookup.get(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "blueprint_request_handler"), BlueprintRequestHandler.class, Void.class);
    public static final BlockApiLookup<AssignablePoiCapability, Void> ASSIGNABLE_POI_LOOKUP =
            BlockApiLookup.get(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "assignable_poi_capability"), AssignablePoiCapability.class, Void.class);

    public static long hamsterWheelCapacity = 3000;
    public static long hamsterWheelSpeed = 3;

    private final BlueprintTagLoad blueprintTagLoad = new BlueprintTagLoad();

    @Override
    public void onInitialize() {
        ClaySoldiersCommon.init();

        FabricNetworkManger.forEachClient(data -> {
            PayloadTypeRegistry.playS2C().register(data.id(), data.codec());
        });

        DataMapPayloadBuilder.registerAll();
        PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.ID, ConfigSyncPayload.STREAM_CODEC);


        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(FabricDataMapGetter.FABRIC_ITEM_ID, FabricDataMapGetter::new);
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new FabricCapabilityManger());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(BLUEPRINT_ID, provider ->
                new IdentifiableResourceListenerWrapper(BLUEPRINT_ID, new BlueprintManger(
                        provider.lookupOrThrow(Registries.BLOCK),
                        provider.lookupOrThrow(ModRegistries.BLUEPRINTS),
                        blueprintTagLoad
                ))
        );

        boolean blueprintPack = ResourceManagerHelperImpl.registerBuiltinResourcePack(
                BLUEPRINT_PACK_ID,
                ClaySoldiersCommon.CSR_DEFAULT_PACK_LOCATION + "/" + ClaySoldiersCommon.BLUEPRINT_PACK_PATH,
                FabricLoader.getInstance().getModContainer(ClaySoldiersCommon.MOD_ID).orElseThrow(),
                Component.translatable(ClaySoldiersCommon.BLUEPRINT_DATA_PACK_LANG),
                ResourcePackActivationType.NORMAL
        );
        if (!blueprintPack) {
            ClaySoldiersCommon.LOGGER.error("Blueprint Pack count not be loaded");
        }

        if (!ResourceManagerHelperImpl.registerBuiltinResourcePack(
                CSR_DEFAULT_PACK_ID,
                ClaySoldiersCommon.CSR_DEFAULT_PACK_LOCATION + "/" + ClaySoldiersCommon.CSR_DEFAULT_DATA_PACK_PATH,
                FabricLoader.getInstance().getModContainer(ClaySoldiersCommon.MOD_ID).orElseThrow(),
                Component.translatable(ClaySoldiersCommon.CSR_DEFAULT_DATA_PACK_LANG),
                ResourcePackActivationType.DEFAULT_ENABLED
        )) {
            ClaySoldiersCommon.LOGGER.error("CSR Default Pack count not be loaded");
        }

        ClaySoldiersCommon.entityAttributes(FabricDefaultAttributeRegistry::register);

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            ClaySoldiersCommon.playerJoinedServer(player, !joined);

            if (!player.connection.connection.isMemoryConnection()) {
                if (joined) {
                    ServerPlayNetworking.send(player, new ConfigSyncPayload(FabricCommonHooks.isBlueprintEnabled(), hamsterWheelSpeed));
                }
                FabricDataMapGetter.sentPayloadsToClient(player);
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(m -> {
            ClaySoldiersCommon.serverStartedEvent(m);
            FabricCommonHooks.setBlueprintEnabled(m.getPackRepository().getSelectedIds().contains(BLUEPRINT_PACK_ID.toString()));
            ClaySoldiersCommon.LOGGER.info(FabricCommonHooks.isBlueprintEnabled() ? "Loaded BlueprintDataPack" : "Not loaded BlueprintDataPack");

            ClaySoldiersCommon.LOGGER.info("Loaded Datamaps: {}", FabricDataMapGetter.getLoadedDataMapsWithSize());
        });

        ClaySoldiersCommon.registerDynamicRegistry(new ClaySoldiersCommon.DynamicRegistryEvent() {
            @Override
            public <T> void register(ResourceKey<Registry<T>> registry, Codec<T> codec, @Nullable Codec<T> synced, @Nullable ClaySoldiersCommon.RegistryRegisteredCallBack<T> callback) {
                if (synced == null) {
                    DynamicRegistries.register(registry, codec);
                } else {
                    DynamicRegistries.registerSynced(registry, codec);
                }
                if (callback != null) {
                    DynamicRegistrySetupCallback.EVENT.register(registryView -> registryView.registerEntryAdded(registry, (callback::onRegister)));
                }
            }
        });

        CommonLifecycleEvents.TAGS_LOADED.register((r, client) -> {
            ClaySoldiersCommon.onTagLoad(r, client);
            if (!client) {
                FabricDataMapGetter.onTagLoad(r);
                blueprintTagLoad.onTagLoad(r);
            }
        });

        ArgumentTypeRegistry.registerArgumentType(
                ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "color_helper"),
                ColorHelperArgumentType.class,
                SingletonArgumentInfo.contextFree(ColorHelperArgumentType::colorArgumentType)
        );

        ArgumentTypeRegistry.registerArgumentType(
                ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "all_clay_mob_teams"),
                DefaultedResourceLocationArgument.AllClayMobTeam.class,
                SingletonArgumentInfo.contextAware(DefaultedResourceLocationArgument::all)
        );

        ArgumentTypeRegistry.registerArgumentType(
                ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "soldier_item_types"),
                DefaultedResourceLocationArgument.SoldierItemType.class,
                SingletonArgumentInfo.contextAware(DefaultedResourceLocationArgument::itemType)
        );

        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> {
            ClaySoldierCommands.register(commandDispatcher, commandBuildContext);
        });

        ExternalMods.ACCESSORIES.ifLoaded(() -> ModAccessories::init);

        EnergyStorage.SIDED.registerForBlockEntities(new BlockApiLookup.BlockEntityApiProvider<>() {
            @Override
            public @Nullable EnergyStorage find(BlockEntity blockEntity, Direction context) {
                var en = HamsterWheelBlockEntity.getEnergyStorage(blockEntity, context);
                return en == null ? null : (EnergyStorage) en;
            }
        }, ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get());

        BLUEPRINT_REQUEST_HANDLER_LOOKUP.registerForBlockEntities(new BlockApiLookup.BlockEntityApiProvider<>() {
            @Override
            public @Nullable BlueprintRequestHandler find(BlockEntity blockEntity, Void context) {
                return blockEntity instanceof EaselBlockEntity easelBlockEntity ? easelBlockEntity.getBlueprintRequestHandler() : null;
            }
        }, ModBlockEntities.EASEL_BLOCK_ENTITY.get());
        ASSIGNABLE_POI_LOOKUP.registerForBlockEntities(new BlockApiLookup.BlockEntityApiProvider<>() {
            @Override
            public @Nullable AssignablePoiCapability find(BlockEntity blockEntity, Void context) {
                return blockEntity instanceof HamsterWheelBlockEntity wheel ? wheel.getPoiCap() : null;
            }
        }, ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get());


        SimpleConfigFabric config = SimpleConfigFabric.of(ClaySoldiersCommon.MOD_ID).provider(namespace ->
                """
                        # Whether the Inventory of a clay soldier can be edited via the menu. May cause loss of items.
                        claySoldierMenuModify=false
                        # Hamster Wheel Capacity
                        hamsterWheelCapacity=3000
                        # Hamster Wheel Generation Speed
                        hamsterWheelSpeed=1
                        """).request();

        if (config.isBroken()) {
            ClaySoldiersCommon.LOGGER.error("CSR Config: An Error occurred loading the {} Config File {}", ClaySoldiersCommon.MOD_ID, config);
        } else {
            ClaySoldiersCommon.LOGGER.info("CSR Config: Successfully loaded on Server: {}", config.configValues());
        }
        ClaySoldiersCommon.claySolderMenuModify = config.getBoolean("claySoldierMenuModify", false);
        hamsterWheelCapacity = config.getPositiveLong("hamsterWheelCapacity", 3000, BatteryProperty.getMaxSupportedEnergy(i -> Long.MAX_VALUE / i));
        hamsterWheelSpeed = config.getPositiveLong("hamsterWheelSpeed", 3, Long.MAX_VALUE);
    }

    record IdentifiableResourceListenerWrapper(ResourceLocation location, SimpleJsonResourceReloadListener resourceReloadListener) implements IdentifiableResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return location;
        }

        @Override
        public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return resourceReloadListener.reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
        }
    }

    private static class BlueprintTagLoad implements BiConsumer<BlueprintManger, ResourceManager> {
        private BlueprintManger manger;
        private ResourceManager resourceManager;

        @Override
        public void accept(BlueprintManger manger, ResourceManager resourceManager) {
            this.manger = manger;
            this.resourceManager = resourceManager;
        }

        public void onTagLoad(RegistryAccess registries) {
            manger.onTagLoad(resourceManager, registries.registryOrThrow(Registries.BLOCK).getTag(ModTags.Blocks.BLUEPRINT_BLACK_LISTED).orElseThrow().stream().toList());
            manger = null;
            resourceManager = null;
        }
    }
}
