package net.bumblebee.claysoldiers.platform.services;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunction;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunctionSerializer;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicate;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicateSerializer;
import net.bumblebee.claysoldiers.entity.common.boss.BossClaySoldierBehaviour;
import net.bumblebee.claysoldiers.platform.ItemLikeSupplier;
import net.bumblebee.claysoldiers.soldieritemtypes.ItemGenerator;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttack;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackSerializer;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.criterion.EntitySubPredicate;
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
import net.minecraft.world.item.CreativeModeTab;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;
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

    <T extends Item> ItemLikeSupplier<T> registerItem(String id, Function<Item.Properties, T> item);

    <T extends Block> ItemLikeSupplier<T> registerBlockWithItem(String id, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties properties);

    <T extends Block> Supplier<T> registerBlockWithoutItem(String id, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties properties);

    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String id, BlockEntityFactory<T> factory, List<Supplier<? extends Block>> blocks);
    Supplier<SimpleParticleType> registerParticle(String id, Supplier<SimpleParticleType> particleTye);
    <T extends Entity> Supplier<EntityType<T>> registerEntity(String id, Supplier<EntityType<T>> entityType);
    <T> Supplier<DataComponentType<T>> registerDataComponent(String id, Supplier<DataComponentType<T>> dataComponent);
    <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenuType(String id, MenuFactory<T> menu);
    <T extends SoldierPropertyType<?>> Supplier<T> registerSoldierProperty(String id, Supplier<T> property);
    <T extends ClayPoiFunction<T>> Supplier<ClayPoiFunctionSerializer<T>> registerClayFunctionSerializer(String id, Supplier<ClayPoiFunctionSerializer<T>> serializer);
    <T extends ClayPredicate<T>> Supplier<ClayPredicateSerializer<T>> registerClayPredicateSerializer(String id, Supplier<ClayPredicateSerializer<T>> serializer);
    <T extends SpecialAttack<T>> Supplier<SpecialAttackSerializer<T>> registerSpecialAttackSerializer(String id, Supplier<SpecialAttackSerializer<T>> serializer);

    <T extends ItemGenerator> Supplier<T> registerItemGenerator(String id, Supplier<T> itemGenerator);
    <T extends BossClaySoldierBehaviour> Supplier<T> registerClayBossBehaviour(String id, Supplier<T> behaviour);

    <T> Registry<T> createRegistry(ResourceKey<Registry<T>> key, boolean synced);

    <T extends CriterionTrigger<?>> Supplier<T> registerCriterionTrigger(String name, Supplier<T> criterionTrigger);
    <T extends EntitySubPredicate> Supplier<MapCodec<T>> registerEntitySubPredicate(String name, Supplier<MapCodec<T>> subPredicate);

    <T extends LootItemFunction> Supplier<MapCodec<T>> registerLootItemFunction(String name, Supplier<MapCodec<T>> lootItemFunction);

    <T> Supplier<ClaySoldierChip.Type<T>> registerClaySoldierModule(String name, Supplier<ClaySoldierChip.Type<T>> chipType);

    <T extends ClaySoldierChipAddon> T registerClaySoldierChipAddon(String name, T addon);

    <T extends RecipeBookCategory> T registerRecipeBookCategory(String name, T category);

    <T extends RecipeType<?>> T registerRecipeType(String name, T type);

    <T extends Recipe<?>> Supplier<RecipeSerializer<T>> registerRecipe(String id, Supplier<RecipeSerializer<T>> recipe);

    <T extends SlotDisplay> void registerSlotDisplay(String id, SlotDisplay.Type<T> recipe);


    List<Item> getAllItems();
    Supplier<CreativeModeTab> registerCreativeModeTab(String id, Function<CreativeModeTab.Builder, CreativeModeTab> creativeModeTab);
    Supplier<CreativeModeTab> registerCreativeModeTabSoldierItems();

    Holder<MobEffect> registerMobEffect(String id, Supplier<MobEffect> effect);
    Supplier<PoiType> registerPoiType(ResourceKey<PoiType> id, Supplier<PoiType> poiType);

    DamageSources createClayDamageSources(RegistryAccess registryAccess);

    CreativeModeTab.DisplayItemsGenerator createGeneratorForAll();

    interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }
    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int id, Inventory inventory, int extraData);
    }
}
