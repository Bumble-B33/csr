package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.ClaySoldiersNeoForge;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunction;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunctionSerializer;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicate;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicateSerializer;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierBehaviour;
import net.bumblebee.claysoldiers.init.ClayDamageSources;
import net.bumblebee.claysoldiers.init.ModCreativeTab;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.platform.services.IPlatformHelper;
import net.bumblebee.claysoldiers.soldieritemtypes.ItemGenerator;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttack;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforgespi.Environment;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public boolean isClient() {
        return Environment.get().getDist().isClient();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isFabric() {
        return false;
    }

    @Override
    public boolean isDevEnv() {
        return !FMLLoader.isProduction();
    }

    @Override
    public String getEnergyUnitName() {
        return "FE";
    }

    @Override
    public <T extends Item> ItemLikeSupplier<T> registerItem(String id, Function<Item.Properties, T> item, Item.Properties properties) {
        return ItemLikeSupplier.create(ClaySoldiersNeoForge.ITEMS.registerItem(id, item, properties));
    }

    @Override
    public <T extends Block> ItemLikeSupplier<T> registerBlockWithItem(String id, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties properties) {
        DeferredBlock<T> blockHolder = ClaySoldiersNeoForge.BLOCKS.registerBlock(id, block, properties);
        ClaySoldiersNeoForge.ITEMS.registerSimpleBlockItem(id, blockHolder);
        return ItemLikeSupplier.create(blockHolder);
    }

    @Override
    public <T extends Block> ItemLikeSupplier<T> registerBlockWithItem(String id, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties properties, BiFunction<Block, Item.Properties, BlockItem> createBlockItem) {
        DeferredBlock<T> blockHolder = ClaySoldiersNeoForge.BLOCKS.registerBlock(id, block, properties);
        ClaySoldiersNeoForge.ITEMS.registerItem(id, itemProp -> createBlockItem.apply(blockHolder.get(), itemProp));
        return ItemLikeSupplier.create(blockHolder);
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String id, BlockEntityFactory<T> factory, List<Supplier<Block>> blocks) {
        return ClaySoldiersNeoForge.BLOCK_ENTITIES.register(id, () -> BlockEntityType.Builder.of(factory::create, blocks.stream().map(Supplier::get).toArray(Block[]::new)).build(null));
    }

    @Override
    public Supplier<SimpleParticleType> registerParticle(String id, Supplier<SimpleParticleType> particleTye) {
        return ClaySoldiersNeoForge.PARTICLE_TYPES.register(id, particleTye);
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(String id, Supplier<EntityType<T>> entityType) {
        return ClaySoldiersNeoForge.ENTITY_TYPE.register(id, entityType);
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponent(String id, Supplier<DataComponentType<T>> dataComponent) {
        return ClaySoldiersNeoForge.DATA_COMPONENTS.register(id, dataComponent);
    }

    @Override
    public <T extends Recipe<?>> Supplier<RecipeSerializer<T>> registerRecipe(String id, Supplier<RecipeSerializer<T>> recipe) {
        return ClaySoldiersNeoForge.RECIPE_SERIALIZER.register(id, recipe);
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenuType(String id, MenuFactory<T> menu) {
        return ClaySoldiersNeoForge.MENUS.register(id, () -> IMenuTypeExtension.create((windowId, inv, data) -> menu.create(windowId, inv, data.readVarInt())));
    }

    @Override
    public <T extends SoldierPropertyType<?>> Supplier<T> registerSoldierProperty(String id, Supplier<T> property) {
        return ClaySoldiersNeoForge.PROPERTY_TYPES.register(id, property);
    }

    @Override
    public <T extends ClayPoiFunction<T>> Supplier<ClayPoiFunctionSerializer<T>> registerClayFunctionSerializer(String id, Supplier<ClayPoiFunctionSerializer<T>> serializer) {
        return ClaySoldiersNeoForge.ClAY_POI_FUNCTION_SERIALIZERS.register(id, serializer);
    }

    @Override
    public <T extends ClayPredicate<T>> Supplier<ClayPredicateSerializer<T>> registerClayPredicateSerializer(String id, Supplier<ClayPredicateSerializer<T>> serializer) {
        return ClaySoldiersNeoForge.CLAY_PREDICATE_SERIALIZERS.register(id, serializer);
    }

    @Override
    public <T extends SpecialAttack<T>> Supplier<SpecialAttackSerializer<T>> registerSpecialAttackSerializer(String id, Supplier<SpecialAttackSerializer<T>> serializer) {
        return ClaySoldiersNeoForge.SPECIAL_ATTACK_SERIALIZERS.register(id, serializer);
    }

    @Override
    public <T extends ItemGenerator> Supplier<T> registerItemGenerator(String id, Supplier<T> itemGenerator) {
        return ClaySoldiersNeoForge.ITEM_GENERATORS.register(id, itemGenerator);
    }

    @Override
    public <T extends BossClaySoldierBehaviour> Supplier<T> registerClayBossBehaviour(String id, Supplier<T> behaviour) {
        return ClaySoldiersNeoForge.BOSS_BEHAVIOURS.register(id, behaviour);
    }

    @Override
    public <T> Registry<T> createRegistry(ResourceKey<Registry<T>> key, boolean synced) {
        return new RegistryBuilder<>(key).sync(synced).create();
    }

    @Override
    public GameRules.Key<GameRules.IntegerValue> createIntRule(String name, GameRules.Category category, int defaultValue) {
        return GameRules.register(name, category, GameRules.IntegerValue.create(defaultValue));
    }

    @Override
    public GameRules.Key<GameRules.BooleanValue> createBoolRule(String name, GameRules.Category category, boolean defaultValue) {
        return GameRules.register(name, category, GameRules.BooleanValue.create(defaultValue));
    }


    @Override
    public List<Item> getAllItems() {
        return ClaySoldiersNeoForge.ITEMS.getEntries().stream().map(d -> (Item) d.get()).toList();
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeModeTab(String id, Function<CreativeModeTab.Builder, CreativeModeTab> creativeModeTab) {
        return ClaySoldiersNeoForge.CREATIVE_MODE_TABS.register(id, () -> creativeModeTab.apply(CreativeModeTab.builder()));
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeModeTabSoldierItems() {
        return ClaySoldiersNeoForge.CREATIVE_MODE_TABS.register("clay_soldier_items",
                () -> CreativeModeTab.builder()
                .withTabsBefore(BuiltInRegistries.CREATIVE_MODE_TAB.getKey(ModCreativeTab.CLAY_SOLDIERS_TAB.get()))
                .title(Component.translatable(ModCreativeTab.CLAY_SOLDIER_ITEMS_TAB_TITLE))
                .icon(() -> ModItems.SHARPENED_STICK.get().getDefaultInstance())
                .displayItems((displayParameters, output) -> {
                    displayParameters.holders().lookup(Registries.ITEM).ifPresent(reg -> {
                        reg.get(ModTags.Items.SOLDIER_HOLDABLE).ifPresent(items -> items.forEach(itemHolder -> addItemIfAllowed(itemHolder, output, displayParameters)));
                        reg.get(ModTags.Items.SOLDIER_POI).ifPresent(s -> s.forEach(i -> addItemIfAllowed(i, output, displayParameters)));
                        reg.get(ModTags.Items.CLAY_HORSE_ARMOR).ifPresent(s -> s.forEach(i -> addItemIfAllowed(i, output, displayParameters)));
                    });
                })
                .build());
    }
    private static void addItemIfAllowed(Holder<Item> item, CreativeModeTab.Output output, CreativeModeTab.ItemDisplayParameters parameters) {
        if ((item.is(ModTags.Items.GAME_MASTER_ITEM) || item.value() instanceof GameMasterBlockItem) && !parameters.hasPermissions()) {
            return;
        }
        output.accept(item.value(), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
    }

    @Override
    public Holder<ArmorMaterial> registerArmorMaterial(String name, Supplier<ArmorMaterial> armorMaterial) {
        return ClaySoldiersNeoForge.ARMOR_MATERIALS.register(name, armorMaterial);
    }

    @Override
    public Holder<MobEffect> registerMobEffect(String id, Supplier<MobEffect> effect) {
        return ClaySoldiersNeoForge.MOB_EFFECTS.register(id, effect);
    }

    @Override
    public Holder<PoiType> registerPoiType(ResourceKey<PoiType> id, Supplier<PoiType> poiType) {
        return ClaySoldiersNeoForge.POI_TYPES.register(id.location().getPath(), poiType);
    }

    @Override
    public DamageSources createClayDamageSources(RegistryAccess registryAccess) {
        return new ClayDamageSources(registryAccess);
    }
}