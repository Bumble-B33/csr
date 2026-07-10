package net.bumblebee.claysoldiers.platform;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunction;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunctionSerializer;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicate;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicateSerializer;
import net.bumblebee.claysoldiers.entity.common.boss.BossClaySoldierBehaviour;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.platform.services.IPlatformHelper;
import net.bumblebee.claysoldiers.soldieritemtypes.ItemGenerator;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttack;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.GameMasterBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.ArrayList;
import java.util.List;
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
    public <T extends Item> ItemLikeSupplier<T> registerItem(String id, Function<Item.Properties, T> item) {
        Identifier location = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id);
        var unpacked = item.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, location)));
        items.add(unpacked);
        Registry.register(BuiltInRegistries.ITEM, location, unpacked);
        return () -> unpacked;
    }

    @Override
    public <T extends Block> ItemLikeSupplier<T> registerBlockWithItem(String id, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties properties) {
        Identifier location = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id);
        var unpacked = block.apply(properties.setId(ResourceKey.create(Registries.BLOCK, location)));
        registerItem(id, props -> new BlockItem(unpacked, props.useBlockDescriptionPrefix()));

        Registry.register(BuiltInRegistries.BLOCK, location, unpacked);
        return () -> unpacked;
    }

    @Override
    public <T extends Block> Supplier<T> registerBlockWithoutItem(String id, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties properties) {
        Identifier location = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id);
        T unpacked = block.apply(properties.setId(ResourceKey.create(Registries.BLOCK, location)));
        Registry.register(BuiltInRegistries.BLOCK, location, unpacked);
        return () -> unpacked;
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String id, BlockEntityFactory<T> factory, List<Supplier<? extends Block>> blocks) {
        var unpacked = FabricBlockEntityTypeBuilder.create(factory::create, blocks.stream().map(Supplier::get).toArray(Block[]::new)).build();
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id), unpacked);
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
    public <T extends SlotDisplay> void registerSlotDisplay(String id, SlotDisplay.Type<T> recipe) {
        defaultRegistrationStrict(BuiltInRegistries.SLOT_DISPLAY, id, recipe);
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenuType(String id, MenuFactory<T> menu) {
        return defaultRegistration(BuiltInRegistries.MENU, id, () -> new ExtendedMenuType<>(menu::create, ByteBufCodecs.VAR_INT));
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
        var builder = FabricRegistryBuilder.create(key);
        if (synced) {
            builder.attribute(RegistryAttribute.SYNCED);
        }

        return builder.buildAndRegister();
    }

    @Override
    public <T extends CriterionTrigger<?>> Supplier<T> registerCriterionTrigger(String name, Supplier<T> criterionTrigger) {
        return defaultRegistration(BuiltInRegistries.TRIGGER_TYPES, name, criterionTrigger);
    }

    @Override
    public <T extends EntitySubPredicate> Supplier<MapCodec<T>> registerEntitySubPredicate(String name, Supplier<MapCodec<T>> subPredicate) {
        return defaultRegistration(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, name, subPredicate);
    }

    @Override
    public <T extends LootItemFunction> Supplier<MapCodec<T>> registerLootItemFunction(String name, Supplier<MapCodec<T>> lootItemFunction) {
        return defaultRegistration(BuiltInRegistries.LOOT_FUNCTION_TYPE, name, lootItemFunction);
    }


    @Override
    public <T> Supplier<ClaySoldierChip.Type<T>> registerClaySoldierModule(String name, Supplier<ClaySoldierChip.Type<T>> chipType) {
        return defaultRegistration(ModRegistries.CLAY_SOLDIER_MODULES_REGISTRY, name, chipType);
    }

    @Override
    public <T extends ClaySoldierChipAddon> T registerClaySoldierChipAddon(String name, T addon) {
        return defaultRegistrationStrict(ModRegistries.CLAY_SOLDIER_CHIP_ADDONS_REGISTRY, name, addon);
    }

    @Override
    public <T extends RecipeBookCategory> T registerRecipeBookCategory(String name, T category) {
        return defaultRegistrationStrict(BuiltInRegistries.RECIPE_BOOK_CATEGORY, name, category);
    }

    @Override
    public <T extends RecipeType<?>> T registerRecipeType(String name, T type) {
        return defaultRegistrationStrict(BuiltInRegistries.RECIPE_TYPE, name, type);
    }

    @Override
    public List<Item> getAllItems() {
        return items;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeModeTab(String id, Function<CreativeModeTab.Builder, CreativeModeTab> creativeModeTab) {
        var tab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id), creativeModeTab.apply(FabricCreativeModeTab.builder()));
        return () -> tab;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeModeTabSoldierItems() {
        List<Holder<Item>> duplicates = new ArrayList<>();
        var group = FabricCreativeModeTab.builder()
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
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_items"), group);
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
    public Holder<MobEffect> registerMobEffect(String id, Supplier<MobEffect> effect) {
        return defaultHolderRegistration(BuiltInRegistries.MOB_EFFECT, id, effect);
    }

    @Override
    public CreativeModeTab.DisplayItemsGenerator createGeneratorForAll() {
        return ((itemDisplayParameters, output) -> {
            for (Item item : ClaySoldiersCommon.PLATFORM.getAllItems()) {
                if (item == ModItems.BLUEPRINT.get()) {
                    ModCreativeTab.modifyBlueprint(output::accept, itemDisplayParameters.holders());
                } else {
                    output.accept(item);
                }
            }
            ModCreativeTab.modifySoldierItems(output::accept, itemDisplayParameters.holders());
            output.accept(
                    ModItems.createEnchantedBook(itemDisplayParameters.holders(), ModEnchantments.SOLDIER_PROJECTILE, 1)
            );
        });
    }

    @Override
    public Supplier<PoiType> registerPoiType(ResourceKey<PoiType> id, Supplier<PoiType> poiType) {
        var type = PoiHelper.register(id.identifier(), poiType.get().maxTickets(), poiType.get().validRange(), poiType.get().matchingStates());
        return () -> type;
    }

    @Override
    public DamageSources createClayDamageSources(RegistryAccess registryAccess) {
        return new ClayDamageSources(registryAccess);
    }

    private <B, T extends B> Supplier<T> defaultRegistration(Registry<B> registry, String id, Supplier<T> value) {
        var unpacked = value.get();
        Registry.register(registry, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id), unpacked);
        return () -> unpacked;
    }

    private <B, T extends B> T defaultRegistrationStrict(Registry<B> registry, String id, T value) {
        return Registry.register(registry, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id), value);
    }

    private <B, T extends B> Holder<T> defaultHolderRegistration(Registry<B> registry, String id, Supplier<T> value) {
        var unpacked = value.get();
        return Registry.registerForHolder(registry, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, id), unpacked);
    }
}
