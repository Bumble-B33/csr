package net.bumblebee.claysoldiers;


import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.blueprint.BlueprintManger;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunctionSerializer;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicateSerializer;
import net.bumblebee.claysoldiers.commands.ClaySoldierCommands;
import net.bumblebee.claysoldiers.commands.ColorHelperArgumentType;
import net.bumblebee.claysoldiers.commands.DefaultedResourceLocationArgument;
import net.bumblebee.claysoldiers.datagen.DataGenerators;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierBehaviour;
import net.bumblebee.claysoldiers.init.ModCapabilities;
import net.bumblebee.claysoldiers.init.ModDataMaps;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.integration.CreateHandCrankPoi;
import net.bumblebee.claysoldiers.integration.ExternalMods;
import net.bumblebee.claysoldiers.integration.accessories.ModAccessories;
import net.bumblebee.claysoldiers.integration.curios.ModCuriosProvider;
import net.bumblebee.claysoldiers.platform.NeoForgeCapabilityManger;
import net.bumblebee.claysoldiers.platform.NeoForgeDataMapGetter;
import net.bumblebee.claysoldiers.platform.NeoForgeNetworkManger;
import net.bumblebee.claysoldiers.platform.services.IDataMapGetter;
import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.bumblebee.claysoldiers.soldieritemtypes.ItemGenerator;
import net.bumblebee.claysoldiers.soldieritemtypes.SoldierItemType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.*;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import org.jetbrains.annotations.Nullable;

@Mod(ClaySoldiersCommon.MOD_ID)
public class ClaySoldiersNeoForge {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPE = DeferredRegister.create(Registries.ENTITY_TYPE, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<SoldierPropertyType<?>> PROPERTY_TYPES = DeferredRegister.create(ModRegistries.SOLDIER_PROPERTY_TYPES, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<ClayPoiFunctionSerializer<?>> ClAY_POI_FUNCTION_SERIALIZERS = DeferredRegister.create(ModRegistries.CLAY_POI_FUNCTION_SERIALIZERS, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<ClayPredicateSerializer<?>> CLAY_PREDICATE_SERIALIZERS = DeferredRegister.create(ModRegistries.CLAY_SOLDIER_PREDICATE_REGISTRY, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<SpecialAttackSerializer<?>> SPECIAL_ATTACK_SERIALIZERS = DeferredRegister.create(ModRegistries.SPECIAL_ATTACK_SERIALIZERS_REGISTRY, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<ItemGenerator> ITEM_GENERATORS = DeferredRegister.create(ModRegistries.ITEM_GENERATORS, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<BossClaySoldierBehaviour> BOSS_BEHAVIOURS = DeferredRegister.create(ModRegistries.BOSS_CLAY_SOLDIER_BEHAVIOURS, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, ClaySoldiersCommon.MOD_ID);


    private static final Holder<ArgumentTypeInfo<?, ?>> COLOR_HELPER = COMMAND_ARGUMENT_TYPES.register("color_helper",
            () -> ArgumentTypeInfos.registerByClass(ColorHelperArgumentType.class, SingletonArgumentInfo.contextFree(ColorHelperArgumentType::colorArgumentType)));
    private static final Holder<ArgumentTypeInfo<?, ?>> ALL_TEAMS = COMMAND_ARGUMENT_TYPES.register("all_teams",
            () -> ArgumentTypeInfos.registerByClass(DefaultedResourceLocationArgument.AllClayMobTeam.class, SingletonArgumentInfo.contextAware(DefaultedResourceLocationArgument::all)));
    private static final Holder<ArgumentTypeInfo<?, ?>> SOLDIER_ITEM_TYPE = COMMAND_ARGUMENT_TYPES.register("soldier_item_types",
            () -> ArgumentTypeInfos.registerByClass(DefaultedResourceLocationArgument.SoldierItemType.class, SingletonArgumentInfo.contextAware(DefaultedResourceLocationArgument::itemType)));

    public static final FeatureFlag BLUEPRINT_FLAG = FeatureFlags.REGISTRY.getFlag(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "blueprint"));

    public ClaySoldiersNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ENTITY_TYPE.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        CLAY_PREDICATE_SERIALIZERS.register(modEventBus);
        SPECIAL_ATTACK_SERIALIZERS.register(modEventBus);
        ClAY_POI_FUNCTION_SERIALIZERS.register(modEventBus);
        MENUS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        RECIPE_SERIALIZER.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
        PROPERTY_TYPES.register(modEventBus);
        COMMAND_ARGUMENT_TYPES.register(modEventBus);
        ARMOR_MATERIALS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ITEM_GENERATORS.register(modEventBus);
        BOSS_BEHAVIOURS.register(modEventBus);
        POI_TYPES.register(modEventBus);

        modEventBus.addListener(this::registerRegistry);
        modEventBus.addListener(this::registerPayload);
        modEventBus.addListener(this::entityAttributeEvent);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(DataGenerators::gatherData);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(ModDataMaps::registerDataMaps);
        modEventBus.addListener(this::addFeaturePacks);
        modEventBus.addListener(this::addDataPackRegistry);

        NeoForge.EVENT_BUS.addListener(this::reloadEvent);
        NeoForge.EVENT_BUS.addListener(this::serverStartEvent);
        NeoForge.EVENT_BUS.addListener(this::commandRegister);
        NeoForge.EVENT_BUS.addListener(this::onDataPackSync);
        NeoForge.EVENT_BUS.addListener(this::onTagLoad);
        NeoForge.EVENT_BUS.addListener(this::afterDataMapLoad);

        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigNeoForge.SPEC);
        ClaySoldiersCommon.init();

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ExternalMods.ACCESSORIES.ifLoaded(() -> ModAccessories::init);
            ExternalMods.CURIOS.ifLoaded(() -> ModCuriosProvider::init);
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        ModCapabilities.registerCapabilities(event);
        ExternalMods.CREATE.ifLoaded(() -> () -> CreateHandCrankPoi.register(event));
    }

    private void registerRegistry(NewRegistryEvent event) {
        event.register(ModRegistries.CLAY_SOLDIER_PREDICATE_REGISTRY);
        event.register(ModRegistries.SPECIAL_ATTACK_SERIALIZERS_REGISTRY);
        event.register(ModRegistries.CLAY_POI_FUNCTION_REGISTRY);
        event.register(ModRegistries.SOLDIER_PROPERTY_TYPES_REGISTRY);
        event.register(ModRegistries.ITEM_GENERATORS_REGISTRY);
        event.register(ModRegistries.BOSS_CLAY_SOLDIER_BEHAVIOURS_REGISTRY);
    }

    private void reloadEvent(AddReloadListenerEvent event) {
        event.addListener(new NeoForgeCapabilityManger());
        event.addListener(new BlueprintManger(
                event.getRegistryAccess().registryOrThrow(Registries.BLOCK).asLookup(),
                event.getRegistryAccess().lookupOrThrow(ModRegistries.BLUEPRINTS),
                (manger, resourceManager) -> manger.onTagLoad(resourceManager, event.getConditionContext().getTag(ModTags.Blocks.BLUEPRINT_BLACK_LISTED))
        ));
    }

    private void registerPayload(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ClaySoldiersCommon.MOD_ID);

        NeoForgeNetworkManger.forEach(payloadData -> registrar.playToClient(
                payloadData.id(), payloadData.codec(), (payload, context) -> context.enqueueWork(
                        () -> payloadData.clientHandler().accept(payload, new INetworkManger.PayloadContext(Minecraft.getInstance(), context.player()))
                )
        ));
    }

    private void entityAttributeEvent(final EntityAttributeCreationEvent event) {
        ClaySoldiersCommon.entityAttributes(event::put);
    }

    private void serverStartEvent(final ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ClaySoldiersCommon.serverStartedEvent(server);
    }

    private void onDataPackSync(final OnDatapackSyncEvent event) {
        ClaySoldiersCommon.playerJoinsServerEvent(event.getPlayer(), event.getRelevantPlayers());
    }

    private void commandRegister(final RegisterCommandsEvent event) {
        ClaySoldierCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    private void addFeaturePacks(final AddPackFindersEvent event) {
        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "data/%s/datapacks/%s".formatted(ClaySoldiersCommon.MOD_ID, ClaySoldiersCommon.BLUEPRINT_PACK_PATH)),
                PackType.SERVER_DATA,
                Component.translatable(ClaySoldiersCommon.BLUEPRINT_DATA_PACK_LANG),
                PackSource.FEATURE,
                false,
                Pack.Position.BOTTOM
        );

        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "%s/%s".formatted(ClaySoldiersCommon.CSR_DEFAULT_PACK_LOCATION, ClaySoldiersCommon.CSR_DEFAULT_DATA_PACK_PATH)),
                PackType.SERVER_DATA,
                Component.translatable(ClaySoldiersCommon.CSR_DEFAULT_DATA_PACK_LANG),
                PackSource.BUILT_IN,
                false,
                Pack.Position.BOTTOM
        );
    }

    private void addDataPackRegistry(final DataPackRegistryEvent.NewRegistry event) {
        ClaySoldiersCommon.registerDynamicRegistry(new ClaySoldiersCommon.DynamicRegistryEvent() {
            @Override
            public <T> void register(ResourceKey<Registry<T>> registry, Codec<T> codec, @Nullable Codec<T> sync, @Nullable ClaySoldiersCommon.RegistryRegisteredCallBack<T> callBack) {
                if (callBack == null) {
                    event.dataPackRegistry(registry, codec, codec);
                } else {
                    event.dataPackRegistry(registry, codec, codec, r -> r.onAdd((ignored, i, k, v) -> callBack.onRegister(i, k.location(), v)));

                }
            }
        });
    }

    private void afterDataMapLoad(final DataMapsUpdatedEvent event) {
        event.ifRegistry(Registries.ITEM, (registry) -> {
            if (event.getCause() == DataMapsUpdatedEvent.UpdateCause.SERVER_RELOAD) {
                event.getRegistries().registryOrThrow(ModRegistries.SOLDIER_ITEM_TYPES).forEach(SoldierItemType::afterDataMapLoad);
            }
            var map = registry.getDataMap(ModDataMaps.SOLDIER_HOLDABLE);
            IDataMapGetter.warnHoldable(map, (itemResourceKey, itemTagKey) -> {
                var opTag = registry.getTag(itemTagKey);
                if (opTag.isEmpty()) {
                    return false;
                }

                return opTag.orElseThrow().stream().anyMatch(h -> itemResourceKey.equals(h.getKey()));
            });

            NeoForgeDataMapGetter.setBySlot(IDataMapGetter.createBySlotMap(map, registry::get));
        });

    }

    private void onTagLoad(final TagsUpdatedEvent event) {
        ClaySoldiersCommon.onTagLoad(event.getRegistryAccess(), event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD);
    }
}