package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunction;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunctionSerializer;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicate;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicateSerializer;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierBehaviour;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.platform.services.IPlatformHelper;
import net.bumblebee.claysoldiers.soldieritemtypes.ItemGenerator;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttack;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class FabricPlatformHelper implements IPlatformHelper {
    private final List<Item> items = new ArrayList<>();

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isFabric() {
        return true;
    }

    @Override
    public boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public String getEnergyUnitName() {
        return " E";
    }

    @Override
    public <T extends Item> ItemLikeSupplier<T> registerItem(String id, Function<Item.Properties, T> item, Item.Properties properties) {
        var unpacked = item.apply(properties);
        items.add(unpacked);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id), unpacked);
        return () -> unpacked;
    }

    @Override
    public <T extends Block> ItemLikeSupplier<T> registerBlockWithItem(String id, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties properties, BiFunction<Block, Item.Properties, BlockItem> createBlockItem) {
        var unpacked = block.apply(properties);
        registerItem(id, props -> new BlockItem(unpacked, props), new Item.Properties());
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id), unpacked);
        return () -> unpacked;
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String id, BlockEntityFactory<T> factory, List<Supplier<Block>> blocks) {
        var unpacked = BlockEntityType.Builder.of(factory::create, blocks.stream().map(Supplier::get).toArray(Block[]::new)).build();
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id), unpacked);
        return () -> unpacked;
    }

    @Override
    public Supplier<SimpleParticleType> registerParticle(String id, Supplier<SimpleParticleType> particleTye) {
        return defaultRegistration(BuiltInRegistries.PARTICLE_TYPE, id, particleTye);
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(String id, Supplier<EntityType<T>> entityType) {
        return defaultRegistration(BuiltInRegistries.ENTITY_TYPE, id, entityType);
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponent(String id, Supplier<DataComponentType<T>> dataComponent) {
        return defaultRegistration(BuiltInRegistries.DATA_COMPONENT_TYPE, id, dataComponent);
    }

    @Override
    public <T extends Recipe<?>> Supplier<RecipeSerializer<T>> registerRecipe(String id, Supplier<RecipeSerializer<T>> recipe) {
        return defaultRegistration(BuiltInRegistries.RECIPE_SERIALIZER, id, recipe);
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenuType(String id, MenuFactory<T> menu) {
        return defaultRegistration(BuiltInRegistries.MENU, id, () -> new ExtendedScreenHandlerType<>(menu::create, ByteBufCodecs.VAR_INT));
    }

    @Override
    public <T extends SoldierPropertyType<?>> Supplier<T> registerSoldierProperty(String id, Supplier<T> property) {
        return defaultRegistration(ModRegistries.SOLDIER_PROPERTY_TYPES_REGISTRY, id, property);
    }

    @Override
    public <T extends ClayPoiFunction<T>> Supplier<ClayPoiFunctionSerializer<T>> registerClayFunctionSerializer(String id, Supplier<ClayPoiFunctionSerializer<T>> serializer) {
        return defaultRegistration(ModRegistries.CLAY_POI_FUNCTION_REGISTRY, id, serializer);
    }

    @Override
    public <T extends ClayPredicate<T>> Supplier<ClayPredicateSerializer<T>> registerClayPredicateSerializer(String id, Supplier<ClayPredicateSerializer<T>> serializer) {
        return defaultRegistration(ModRegistries.CLAY_SOLDIER_PREDICATE_REGISTRY, id, serializer);
    }

    @Override
    public <T extends SpecialAttack<T>> Supplier<SpecialAttackSerializer<T>> registerSpecialAttackSerializer(String id, Supplier<SpecialAttackSerializer<T>> serializer) {
        return defaultRegistration(ModRegistries.SPECIAL_ATTACK_SERIALIZERS_REGISTRY, id, serializer);
    }

    @Override
    public <T extends ItemGenerator> Supplier<T> registerItemGenerator(String id, Supplier<T> itemGenerator) {
        return defaultRegistration(ModRegistries.ITEM_GENERATORS_REGISTRY, id, itemGenerator);
    }

    @Override
    public <T extends BossClaySoldierBehaviour> Supplier<T> registerClayBossBehaviour(String id, Supplier<T> behaviour) {
        return defaultRegistration(ModRegistries.BOSS_CLAY_SOLDIER_BEHAVIOURS_REGISTRY, id, behaviour);
    }

    @Override
    public <T> Registry<T> createRegistry(ResourceKey<Registry<T>> key, boolean synced) {
        var builder = FabricRegistryBuilder.createSimple(key);
        if (synced) {
            builder.attribute(RegistryAttribute.SYNCED);
        }

        return builder.buildAndRegister();
    }

    @Override
    public GameRules.Key<GameRules.IntegerValue> createIntRule(String name, GameRules.Category category, int defaultValue) {
        return GameRuleRegistry.register(name, category, GameRuleFactory.createIntRule(defaultValue));
    }

    @Override
    public GameRules.Key<GameRules.BooleanValue> createBoolRule(String name, GameRules.Category category, boolean defaultValue) {
        return GameRuleRegistry.register(name, category, GameRuleFactory.createBooleanRule(defaultValue));
    }

    @Override
    public List<Item> getAllItems() {
        return items;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeModeTab(String id, Function<CreativeModeTab.Builder, CreativeModeTab> creativeModeTab) {
        var tab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id), creativeModeTab.apply(FabricItemGroup.builder()));
        return () -> tab;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeModeTabSoldierItems() {
        List<Holder<Item>> duplicates = new ArrayList<>();
        var group = FabricItemGroup.builder()
                        .title(Component.translatable(ModCreativeTab.CLAY_SOLDIER_ITEMS_TAB_TITLE))
                        .icon(() -> ModItems.SHARPENED_STICK.get().getDefaultInstance())
                        .displayItems((displayParameters, output) -> {
                            displayParameters.holders().lookup(Registries.ITEM).ifPresent(reg -> {
                                reg.get(ModTags.Items.SOLDIER_HOLDABLE).ifPresent(items -> items.forEach(itemHolder -> addItemIfAllowed(itemHolder, output, displayParameters)));
                                reg.get(ModTags.Items.SOLDIER_POI).ifPresent(s -> s.forEach(i -> output.accept(i.value(), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY)));
                                reg.get(ModTags.Items.CLAY_HORSE_ARMOR).ifPresent(s -> s.forEach(i -> {
                                    try {
                                        output.accept(i.value(), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                                    } catch (IllegalStateException e) {
                                        duplicates.add(i);
                                    }
                                }));
                            });
                        })
                        .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath( ClaySoldiersCommon.MOD_ID, "dlay_soldiers"), group);
        ClaySoldiersCommon.LOGGER.debug("Added {} twice to Clay Soldier Items Tab", duplicates);
        return () -> group;
    }
    private static void addItemIfAllowed(Holder<Item> item, CreativeModeTab.Output output, CreativeModeTab.ItemDisplayParameters parameters) {
        if ((item.is(ModTags.Items.GAME_MASTER_ITEM) || item.value() instanceof GameMasterBlockItem) && !parameters.hasPermissions()) {
            return;
        }
        output.accept(item.value(), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
    }

    @Override
    public Holder<ArmorMaterial> registerArmorMaterial(String name, Supplier<ArmorMaterial> armorMaterial) {
        var unpacked = armorMaterial.get();
        return Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, name), unpacked);
    }


    @Override
    public Holder<MobEffect> registerMobEffect(String id, Supplier<MobEffect> effect) {
        var unpacked = effect.get();
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id), unpacked);
    }

    @Override
    public Holder<PoiType> registerPoiType(ResourceKey<PoiType> id, Supplier<PoiType> poiType) {
        var unpacked = poiType.get();
        return Holder.direct(PointOfInterestHelper.register(id.location(), unpacked.maxTickets(), unpacked.maxTickets(), unpacked.matchingStates()));
    }
    @Override
    public DamageSources createClayDamageSources(RegistryAccess registryAccess) {
        return new ClayDamageSources(registryAccess);
    }

    private  <B, T extends B> Supplier<T> defaultRegistration(Registry<B> registry, String id, Supplier<T> value) {
        var unpacked = value.get();
        Registry.register(registry, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id), unpacked);
        return () -> unpacked;
    }
}
