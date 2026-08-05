package net.bumblebee.claysoldiers;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.block.soldiercontainer.ClayMobContainer;
import net.bumblebee.claysoldiers.blueprint.BlueprintManager;
import net.bumblebee.claysoldiers.capability.AssignableWorksiteCapability;
import net.bumblebee.claysoldiers.capability.BlueprintRequestHandler;
import net.bumblebee.claysoldiers.commands.ColorHelperArgumentType;
import net.bumblebee.claysoldiers.datamap.FabricDataMapLoader;
import net.bumblebee.claysoldiers.energy.BatteryProperties;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.integration.ExternalMods;
import net.bumblebee.claysoldiers.integration.accessories.ModAccessories;
import net.bumblebee.claysoldiers.networking.ConfigSyncPayload;
import net.bumblebee.claysoldiers.networking.DataMapPayloadBuilder;
import net.bumblebee.claysoldiers.platform.FabricCapabilityManger;
import net.bumblebee.claysoldiers.platform.FabricConfig;
import net.bumblebee.claysoldiers.platform.services.IConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.impl.resource.ResourceLoaderImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyItem;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public class ClaySoldierFabric implements ModInitializer {
    private static final Identifier BLUEPRINT_ID = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "csr_blueprint");
    public static final Identifier CSR_DEFAULT_PACK_ID = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, ClaySoldiersCommon.CSR_DEFAULT_DATA_PACK_PATH);

    public static final BlockApiLookup<BlueprintRequestHandler, Void> BLUEPRINT_REQUEST_HANDLER_LOOKUP =
            BlockApiLookup.get(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "blueprint_request_handler"), BlueprintRequestHandler.class, Void.class);
    public static final BlockApiLookup<AssignableWorksiteCapability, Void> ASSIGNABLE_POI_LOOKUP =
            BlockApiLookup.get(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "assignable_poi_capability"), AssignableWorksiteCapability.class, Void.class);

    public static final BlockApiLookup<ClayMobContainer, Void> CLAY_MOB_CONTAINER_LOOKUP =
            BlockApiLookup.get(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_mob_container"), ClayMobContainer.class, Void.class);


    private final ClaySoldiersCommon.BlueprintTagLoad blueprintTagLoader = new ClaySoldiersCommon.BlueprintTagLoad();

    @Override
    public void onInitialize() {
        ClaySoldiersCommon.init();

        ModRegistries.register(_ -> {
        });
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipes.CHIP_ASSEMBLY_SERIALIZER.get());
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipes.ADDON_CHIP_SERIALIZER.get());

        ClaySoldiersCommon.NETWORK_MANGER.forEach(data -> {
            PayloadTypeRegistry.clientboundPlay().register(data.id(), data.codec());
        });

        DataMapPayloadBuilder.registerAll();
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.ID, ConfigSyncPayload.STREAM_CODEC);

        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(FabricCapabilityManger.ID, new FabricCapabilityManger());
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(FabricDataMapLoader.ID, new ReloadListenerWithProvider<>(new FabricDataMapLoader(), FabricDataMapLoader::setProvider));
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(BLUEPRINT_ID, new BlueprintManager(blueprintTagLoader));

        if (!ResourceLoaderImpl.registerBuiltinPack(
                CSR_DEFAULT_PACK_ID,
                ClaySoldiersCommon.CSR_DATA_PACK_LOCATION + "/" + ClaySoldiersCommon.CSR_DEFAULT_DATA_PACK_PATH,
                FabricLoader.getInstance().getModContainer(ClaySoldiersCommon.MOD_ID).orElseThrow(),
                Component.translatable(ClaySoldiersCommon.CSR_DEFAULT_DATA_PACK_LANG),
                PackActivationType.DEFAULT_ENABLED
        )) {
            ClaySoldiersCommon.LOGGER.error("CSR Default Pack count not be loaded");
        }

        ClaySoldiersCommon.entityAttributes(FabricDefaultAttributeRegistry::register);

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            ClaySoldiersCommon.playerJoinedServer(player, !joined);

            if (!ClaySoldiersCommon.NETWORK_MANGER.isMemoryConnection(player)) {
                if (joined) {
                    ClaySoldiersCommon.sendWhenChannel(player, FabricConfig.createSyncPayload(), false);
                }
                FabricDataMapLoader.sentPayloadsToClient(player);
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(m -> {
            ClaySoldiersCommon.serverStartedEvent(m);
            FabricConfig.logConfig("Server Start", false);

            ClaySoldiersCommon.LOGGER.info("Loaded Datamaps: {}", FabricDataMapLoader.getLoadedDataMapsWithSize());
        });

        ModRegistries.registerDynamicRegistry(new ClaySoldiersCommon.DynamicRegistryEvent() {
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
                blueprintTagLoader.onTagLoad(r);
            }
        });

        UseBlockCallback.EVENT.register((player, level, hand, blockHitResult) -> ClaySoldiersCommon.useItemOnBlockEvent(player, level, hand, blockHitResult.getBlockPos()).orElse(InteractionResult.PASS));

        ArgumentTypeRegistry.registerArgumentType(
                Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "color_helper"),
                ColorHelperArgumentType.class,
                SingletonArgumentInfo.contextFree(ColorHelperArgumentType::colorArgumentType)
        );

        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> {
            ModCommands.register(commandDispatcher, commandBuildContext);
        });

        ModEntitySerializers.register((id, s) -> {
            FabricEntityDataRegistry.register(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id), s);
        });

        ClaySoldiersCommon.registerDispenseBehavior();

        ExternalMods.ACCESSORIES.ifLoaded(() -> ModAccessories::init);

        ModCapabilities.registerEnergy((type, lookup) -> EnergyStorage.SIDED.registerForBlockEntities((b, c) -> (EnergyStorage) lookup.apply(b, c), type));
        ModCapabilities.registerBlueprint((type, lookup) -> BLUEPRINT_REQUEST_HANDLER_LOOKUP.registerForBlockEntities((o, _) -> lookup.apply(o), type));
        ModCapabilities.registerAssignablePoi((type, lookup) -> ASSIGNABLE_POI_LOOKUP.registerForBlockEntities((o, _) -> lookup.apply(o), type));
        ModCapabilities.registerClayMobContainer((type, lookup) -> CLAY_MOB_CONTAINER_LOOKUP.registerForBlockEntities((o, _) -> lookup.apply(o), type));
        ModCapabilities.registerItemEnergy((lookup, items) -> {
            EnergyStorage.ITEM.registerForItems((itemStack, containerItemContext) -> {
                BatteryProperties max = lookup.apply(itemStack);
                if (max == null) {
                    return null;
                }
                return SimpleEnergyItem.createStorage(containerItemContext, max.capacity(), max.maxInsert(), max.maxExtract());
            }, items);
        });


        SimpleConfigFabric config = SimpleConfigFabric.of(ClaySoldiersCommon.MOD_ID).provider(namespace ->
                """
                        # Whether the Inventory of a clay soldier can be edited via the menu. May cause loss of items.
                        %s=false
                        # Hamster Wheel Energy Generation Speed
                        %s=3
                        # Enable/Disabled the Recipe to craft Shear Blades from Shears
                        %s=true
                        # Whether Clay Soldiers should drop their Inventory on death
                        %s=true
                        # Chance for Clay Soldiers to drop them self
                        %s=0.5f
                        # When true Chips can only be installed into Clay Soldiers loyal to the player
                        %s=true
                        # Energy Transfer Rate for Clay Soldiers with batteries
                        %s=15
                        # Charging Pad Energy Transfer Rate for the Player
                        %s=15
                        """.formatted(
                        IConfig.SOLDIER_MODIFY_MENU_KEY,
                        IConfig.HAMSTER_WHEEL_SPEED_KEY,
                        IConfig.SHEAR_BLADE_RECIPE_KEY,
                        IConfig.SOLDIER_DROP_INVENTORY_KEY,
                        IConfig.SOLDIER_DROP_SELF_KEY,
                        IConfig.CHIP_REQUIRES_LOYALTY_KEY,
                        IConfig.BASE_SOLDIER_ENERGY_TRANSFER_RATE_KEY,
                        IConfig.CHARGING_PAD_PLAYER_RATE_KEY
                )).request();

        if (config.isBroken()) {
            ClaySoldiersCommon.LOGGER.error("CSR Config: An Error occurred loading the {} Config File {}", ClaySoldiersCommon.MOD_ID, config);
        } else {
            ClaySoldiersCommon.LOGGER.info("CSR Config: Successfully loaded on Server: {}", config.configValues());
        }

        FabricConfig.init(
                config.getPositiveInt(IConfig.HAMSTER_WHEEL_SPEED_KEY, 3, Integer.MAX_VALUE),
                config.getBoolean(IConfig.SOLDIER_MODIFY_MENU_KEY, false),
                config.getBoolean(IConfig.SHEAR_BLADE_RECIPE_KEY, true),
                config.getFloatPercent(IConfig.SOLDIER_DROP_INVENTORY_KEY, 0.5f),
                config.getBoolean(IConfig.SOLDIER_DROP_SELF_KEY, true),
                config.getBoolean(IConfig.CHIP_REQUIRES_LOYALTY_KEY, true),
                config.getPositiveInt(IConfig.BASE_SOLDIER_ENERGY_TRANSFER_RATE_KEY, 15, Integer.MAX_VALUE),
                config.getNonNegativeInt(IConfig.CHARGING_PAD_PLAYER_RATE_KEY, 15, Integer.MAX_VALUE)
        );
    }

    record ReloadListenerWithProvider<T extends SimpleJsonResourceReloadListener<?>>(T resourceReloadListener,
                                                                                     BiConsumer<T, HolderLookup.Provider> providerSetter) implements PreparableReloadListener {

        @Override
        public @NonNull CompletableFuture<Void> reload(SharedState sharedState, @NonNull Executor exectutor, @NonNull PreparationBarrier barrier, @NonNull Executor applyExectutor) {
            providerSetter.accept(resourceReloadListener, sharedState.get(ResourceLoader.REGISTRY_LOOKUP_KEY));
            return resourceReloadListener.reload(sharedState, exectutor, barrier, applyExectutor);
        }
    }
}
