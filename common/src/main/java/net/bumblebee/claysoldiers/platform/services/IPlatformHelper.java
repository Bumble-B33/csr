package net.bumblebee.claysoldiers.platform.services;

import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunction;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunctionSerializer;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicate;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicateSerializer;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierBehaviour;
import net.bumblebee.claysoldiers.platform.ItemLikeSupplier;
import net.bumblebee.claysoldiers.soldieritemtypes.ItemGenerator;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttack;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IPlatformHelper {
    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    boolean isFabric();

    boolean isDevEnv();

    boolean isClient();

    String getEnergyUnitName();

    default <T> T ifDevEv(Supplier<T> action, T elseValue) {
        return isDevEnv() ? action.get() : elseValue;
    }

    <T extends Item> ItemLikeSupplier<T> registerItem(String id, Function<Item.Properties, T> item, Item.Properties properties);

    default  <T extends Block> ItemLikeSupplier<T> registerBlockWithItem(String id, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties properties) {
        return registerBlockWithItem(id, block, properties, BlockItem::new);
    }

    <T extends Block> ItemLikeSupplier<T> registerBlockWithItem(String id, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties properties, BiFunction<Block, Item.Properties, BlockItem> createBlockItem);

    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String id, BlockEntityFactory<T> factory, List<Supplier<Block>> blocks);
    Supplier<SimpleParticleType> registerParticle(String id, Supplier<SimpleParticleType> particleTye);
    <T extends Entity> Supplier<EntityType<T>> registerEntity(String id, Supplier<EntityType<T>> entityType);
    <T> Supplier<DataComponentType<T>> registerDataComponent(String id, Supplier<DataComponentType<T>> dataComponent);
    <T extends Recipe<?>> Supplier<RecipeSerializer<T>> registerRecipe(String id, Supplier<RecipeSerializer<T>> recipe);
    <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenuType(String id, MenuFactory<T> menu);
    <T extends SoldierPropertyType<?>> Supplier<T> registerSoldierProperty(String id, Supplier<T> property);
    <T extends ClayPoiFunction<T>> Supplier<ClayPoiFunctionSerializer<T>> registerClayFunctionSerializer(String id, Supplier<ClayPoiFunctionSerializer<T>> serializer);
    <T extends ClayPredicate<T>> Supplier<ClayPredicateSerializer<T>> registerClayPredicateSerializer(String id, Supplier<ClayPredicateSerializer<T>> serializer);
    <T extends SpecialAttack<T>> Supplier<SpecialAttackSerializer<T>> registerSpecialAttackSerializer(String id, Supplier<SpecialAttackSerializer<T>> serializer);

    <T extends ItemGenerator> Supplier<T> registerItemGenerator(String id, Supplier<T> itemGenerator);
    <T extends BossClaySoldierBehaviour> Supplier<T> registerClayBossBehaviour(String id, Supplier<T> behaviour);

    <T> Registry<T> createRegistry(ResourceKey<Registry<T>> key, boolean synced);

    GameRules.Key<GameRules.IntegerValue> createIntRule(String name, GameRules.Category category, int defaultValue);
    GameRules.Key<GameRules.BooleanValue> createBoolRule(String name, GameRules.Category category, boolean defaultValue);

    List<Item> getAllItems();
    Supplier<CreativeModeTab> registerCreativeModeTab(String id, Function<CreativeModeTab.Builder, CreativeModeTab> creativeModeTab);
    Supplier<CreativeModeTab> registerCreativeModeTabSoldierItems();

    Holder<ArmorMaterial> registerArmorMaterial(String name, Supplier<ArmorMaterial> armorMaterial);
    Holder<MobEffect> registerMobEffect(String id, Supplier<MobEffect> effect);
    Holder<PoiType> registerPoiType(ResourceKey<PoiType> id, Supplier<PoiType> poiType);

    DamageSources createClayDamageSources(RegistryAccess registryAccess);

    interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }
    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int id, Inventory inventory, int extraData);
    }
}
