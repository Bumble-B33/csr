package net.bumblebee.claysoldiers;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.blueprint.BlueprintManager;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunctionSerializer;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicateSerializer;
import net.bumblebee.claysoldiers.commands.ColorHelperArgumentType;
import net.bumblebee.claysoldiers.datagen.DataGenerators;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.entity.common.boss.BossClaySoldierBehaviour;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.integration.ExternalMods;
import net.bumblebee.claysoldiers.integration.accessories.ModAccessories;
import net.bumblebee.claysoldiers.integration.curios.ModCurios;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.platform.NeoForgeCapabilityManager;
import net.bumblebee.claysoldiers.platform.NeoForgeConfig;
import net.bumblebee.claysoldiers.platform.NeoForgeDataMapGetter;
import net.bumblebee.claysoldiers.platform.services.IDataMapGetter;
import net.bumblebee.claysoldiers.platform.services.NetworkManger;
import net.bumblebee.claysoldiers.soldieritemtypes.ItemGenerator;
import net.bumblebee.claysoldiers.soldieritemtypes.SoldierItemType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackSerializer;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.criterion.EntitySubPredicate;
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
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.*;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import net.neoforged.neoforge.resource.VanillaServerListeners;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Mod(ClaySoldiersCommon.MOD_ID)
public class ClaySoldiersNeoForge {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, ClaySoldiersCommon.MOD_ID);
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
    public static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> ENTITY_SUB_PREDICATE = DeferredRegister.create(Registries.ENTITY_SUB_PREDICATE_TYPE, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_ITEM_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<ClaySoldierChip.Type<?>> CLAY_SOLDIER_CHIPS = DeferredRegister.create(ModRegistries.CLAY_SOLDIER_MODULES_REGISTRY, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<ClaySoldierChipAddon> CLAY_SOLDIER_CHIP_ADDONS = DeferredRegister.create(ModRegistries.CLAY_SOLDIER_CHIP_ADDONS_REGISTRY, ClaySoldiersCommon.MOD_ID);

    public static final DeferredRegister<RecipeBookCategory> RECIPE_BOOK_CATEGORIES = DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ClaySoldiersCommon.MOD_ID);
    public static final DeferredRegister<SlotDisplay.Type<?>> SLOT_DISPLAYS = DeferredRegister.create(BuiltInRegistries.SLOT_DISPLAY, ClaySoldiersCommon.MOD_ID);


    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, ClaySoldiersCommon.MOD_ID);


    private static final Holder<ArgumentTypeInfo<?, ?>> COLOR_HELPER = COMMAND_ARGUMENT_TYPES.register("color_helper",
            () -> ArgumentTypeInfos.registerByClass(ColorHelperArgumentType.class, SingletonArgumentInfo.contextFree(ColorHelperArgumentType::colorArgumentType)));

    private final ClaySoldiersCommon.BlueprintTagLoad blueprintTagLoader = new ClaySoldiersCommon.BlueprintTagLoad();


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
        RECIPE_SERIALIZERS.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
        PROPERTY_TYPES.register(modEventBus);
        COMMAND_ARGUMENT_TYPES.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ITEM_GENERATORS.register(modEventBus);
        BOSS_BEHAVIOURS.register(modEventBus);
        POI_TYPES.register(modEventBus);
        CRITERION_TRIGGERS.register(modEventBus);
        ENTITY_SUB_PREDICATE.register(modEventBus);
        LOOT_ITEM_FUNCTIONS.register(modEventBus);
        CLAY_SOLDIER_CHIPS.register(modEventBus);
        CLAY_SOLDIER_CHIP_ADDONS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_BOOK_CATEGORIES.register(modEventBus);
        SLOT_DISPLAYS.register(modEventBus);
        ENTITY_DATA_SERIALIZERS.register(modEventBus);

        modEventBus.addListener(this::registerRegistry);
        modEventBus.addListener(this::registerPayload);
        modEventBus.addListener(this::entityAttributeEvent);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(DataGenerators::gatherData);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(NeoForgeDataMaps::registerDataMaps);
        modEventBus.addListener(this::addFeaturePacks);
        modEventBus.addListener(this::addDataPackRegistry);


        NeoForge.EVENT_BUS.addListener(this::reloadEvent);
        NeoForge.EVENT_BUS.addListener(this::serverStartEvent);
        NeoForge.EVENT_BUS.addListener(this::commandRegister);
        NeoForge.EVENT_BUS.addListener(this::onDataPackSync);
        NeoForge.EVENT_BUS.addListener(this::onTagLoadClient);
        NeoForge.EVENT_BUS.addListener(this::onTagLoadServer);
        NeoForge.EVENT_BUS.addListener(this::afterDataMapLoad);
        NeoForge.EVENT_BUS.addListener(this::playerHurtEvent);
        NeoForge.EVENT_BUS.addListener(this::recipeSyncEvent);
        NeoForge.EVENT_BUS.addListener(this::useItemOnBlockEvent);


        modContainer.registerConfig(ModConfig.Type.CLIENT, NeoForgeConfig.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, NeoForgeConfig.SPEC);

        ClaySoldiersCommon.init();

        ModEntitySerializers.register((id, s) -> ENTITY_DATA_SERIALIZERS.register(id, () -> s));

        ExternalMods.CURIOS.ifLoaded(() -> () -> new ModCurios(modEventBus));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ExternalMods.ACCESSORIES.ifLoaded(() -> ModAccessories::init);
            ClaySoldiersCommon.registerDispenseBehavior();
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        NeoForgeCapabilities.registerCapabilities(event);
    }

    private void playerHurtEvent(LivingIncomingDamageEvent event) {
        if (event.getAmount() < 0) {
            return;
        }
        if (event.getEntity() instanceof Player player && !player.level().isClientSide() && event.getSource().getEntity() instanceof LivingEntity livingEntity) {
            ClaySoldierSpawnItem.onPlayerHurt(player, livingEntity);
        }
    }

    private void registerRegistry(NewRegistryEvent event) {
        ModRegistries.register(event::register);
    }

    private void reloadEvent(AddServerReloadListenersEvent event) {

        event.addListener(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "capability_manager"), new NeoForgeCapabilityManager());
        event.addListener(BlueprintManager.LISTENER_KEY, new BlueprintManager(
                        blueprintTagLoader
                )
        );

        event.addDependency(VanillaServerListeners.LAST, BlueprintManager.LISTENER_KEY);
    }


    private void registerPayload(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ClaySoldiersCommon.MOD_ID);

        ClaySoldiersCommon.NETWORK_MANGER.forEach(payloadData -> registrar.playToClient(
                payloadData.id(), payloadData.codec(), (payload, context) -> context.enqueueWork(
                        () -> payload.handleClient(new NetworkManger.PayloadContext(Minecraft.getInstance(), context.player()))
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
        ModCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    private void addFeaturePacks(final AddPackFindersEvent event) {
        event.addPackFinders(
                Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "%s/%s".formatted(ClaySoldiersCommon.CSR_DATA_PACK_LOCATION, ClaySoldiersCommon.CSR_DEFAULT_DATA_PACK_PATH)),
                PackType.SERVER_DATA,
                Component.translatable(ClaySoldiersCommon.CSR_DEFAULT_DATA_PACK_LANG),
                PackSource.BUILT_IN,
                false,
                Pack.Position.BOTTOM
        );
    }

    private void addDataPackRegistry(final DataPackRegistryEvent.NewRegistry event) {
        ModRegistries.registerDynamicRegistry(new ClaySoldiersCommon.DynamicRegistryEvent() {
            @Override
            public <T> void register(ResourceKey<Registry<T>> registry, Codec<T> codec, @Nullable Codec<T> sync, @Nullable ClaySoldiersCommon.RegistryRegisteredCallBack<T> callBack) {
                if (callBack != null) {
                    event.dataPackRegistry(registry, codec, codec, r -> r
                            .onAdd((ignored, i, k, v) -> callBack.onRegister(i, k.identifier(), v)));
                } else {
                    event.dataPackRegistry(registry, codec, codec);
                }
            }
        });
    }

    private void afterDataMapLoad(final DataMapsUpdatedEvent event) {
        event.ifRegistry(Registries.ITEM, (registry) -> {
            if (event.getCause() == DataMapsUpdatedEvent.UpdateCause.SERVER_RELOAD) {
                SoldierItemType.onDataMapLoad(() -> {
                    event.getRegistries().lookupOrThrow(ModRegistries.SOLDIER_ITEM_TYPES).forEach(SoldierItemType::afterDataMapLoad);
                });
            }

            registry.getDataMap(NeoForgeDataMaps.SOLDIER_ARMOR).values().forEach(multiWearable -> {
                multiWearable.forEachWearableEffect(wearable -> wearable.buildTrims(event.getRegistries()));
            });


            Map<ResourceKey<Item>, SoldierHoldableEffect> map = registry.getDataMap(NeoForgeDataMaps.SOLDIER_HOLDABLE);
            IDataMapGetter.warnHoldable(map, (itemResourceKey, itemTagKey) -> {
                var opTag = registry.get(itemTagKey);
                if (opTag.isEmpty()) {
                    return false;
                }

                return opTag.orElseThrow().stream().anyMatch(h -> itemResourceKey.equals(h.getKey()));
            });

            NeoForgeDataMapGetter.setBySlot(IDataMapGetter.createBySlotMap(map, registry::getValue));
        });

    }

    private void onTagLoadServer(final TagsUpdatedEvent.ServerDataLoad event) {
        ClaySoldiersCommon.onTagLoad(event.getRegistries(), false);
        if (event.shouldUpdateStaticData()) {
            blueprintTagLoader.onTagLoad(event.getRegistries());
        }
    }

    private void onTagLoadClient(final TagsUpdatedEvent.ClientPacketReceived event) {
        ClaySoldiersCommon.onTagLoad(event.getRegistries(), true);
        if (event.shouldUpdateStaticData()) {
            blueprintTagLoader.onTagLoad(event.getRegistries());
        }
    }

    private void useItemOnBlockEvent(UseItemOnBlockEvent event) {
        ClaySoldiersCommon.useItemOnBlockEvent(event.getPlayer(), event.getLevel(), event.getHand(), event.getPos()).ifPresent(event::cancelWithResult);
    }

    private void recipeSyncEvent(final OnDatapackSyncEvent event) {
        event.sendRecipes(ModRecipes.CHIP_ASSEMBLY_TYPE);
    }
}