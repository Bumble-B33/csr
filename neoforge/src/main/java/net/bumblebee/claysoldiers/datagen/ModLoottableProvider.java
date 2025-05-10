package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.init.ModBlocks;
import net.bumblebee.claysoldiers.init.ModBossBehaviours;
import net.bumblebee.claysoldiers.init.ModEnchantments;
import net.bumblebee.claysoldiers.init.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModLoottableProvider extends LootTableProvider {
    public ModLoottableProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, Set.of(), List.of(
                new SubProviderEntry(BlockLoottableProvider::new, LootContextParamSets.BLOCK),
                new SubProviderEntry(BossLootTableProvider::new, LootContextParamSets.ENTITY)
        ), pRegistries);
    }

    private static class BlockLoottableProvider extends BlockLootSubProvider {
        protected BlockLoottableProvider(HolderLookup.Provider lookupProvider) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), lookupProvider);
        }

        @Override
        protected void generate() {
            dropSelf(ModBlocks.HAMSTER_WHEEL_BLOCK.get());
            dropSelf(ModBlocks.EASEL_BLOCK.get());
            dropSelf(ModBlocks.ESCRITOIRE_BLOCK.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(ModBlocks.HAMSTER_WHEEL_BLOCK.get(), ModBlocks.EASEL_BLOCK.get(), ModBlocks.ESCRITOIRE_BLOCK.get());
        }
    }

    private record BossLootTableProvider(HolderLookup.Provider lookup) implements LootTableSubProvider {
        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
            biConsumer.accept(
                    ModBossBehaviours.DEFAULT_LOOT_TABLE,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1f))
                                    .add(LootItem.lootTableItem(ModItems.CLAY_POUCH))
                                    .add(LootItem.lootTableItem(ModItems.CLAY_STAFF))
                                    .when(LootItemKilledByPlayerCondition.killedByPlayer())
                            )
                            .withPool(LootPool.lootPool()
                                    .setRolls(UniformGenerator.between(3f, 5f))
                                    .add(LootItem.lootTableItem(Items.CLAY_BALL)
                                            .setWeight(10)
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1f, 2f)))
                                    )
                                    .add(LootItem.lootTableItem(Items.CLAY)
                                            .setWeight(2)
                                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1f)))
                                    )
                            )

            );
            biConsumer.accept(
                    ModBossBehaviours.VAMPIRE_LOOT_TABLE,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1f))
                                    .add(LootItem.lootTableItem(ModItems.CLAY_POUCH))
                                    .add(LootItem.lootTableItem(Items.BOOK)
                                            .apply(new SetEnchantmentsFunction.Builder()
                                                    .withEnchantment(FakeHolder.createForSlingShot(lookup), ConstantValue.exactly(1f))
                                            )
                                    )
                                    .when(LootItemKilledByPlayerCondition.killedByPlayer())
                            )
                            .withPool(LootPool.lootPool()
                                    .setRolls(UniformGenerator.between(3f, 5f))
                                    .add(LootItem.lootTableItem(Items.CLAY_BALL)
                                            .setWeight(10)
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1f, 2f)))
                                    )
                                    .add(LootItem.lootTableItem(Items.CLAY)
                                            .setWeight(2)
                                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1f)))
                                    )
                            )

            );
        }
    }


    private static class FakeHolder extends Holder.Reference<Enchantment> {
        public FakeHolder(HolderLookup.Provider lookup, ResourceKey<Enchantment> key, Enchantment value) {
            super(Type.STAND_ALONE, lookup.lookupOrThrow(Registries.ENCHANTMENT), key, value);
        }

        public static FakeHolder createForSlingShot(HolderLookup.Provider lookup) {
            return new FakeHolder(lookup, ModEnchantments.SOLDIER_PROJECTILE, Enchantment.enchantment(
                    Enchantment.definition(
                            HolderSet.empty(),
                            2,
                            1,
                            Enchantment.constantCost(20),
                            Enchantment.constantCost(50),
                            4,
                            EquipmentSlotGroup.ANY
                    )
            ).build(ModEnchantments.SOLDIER_PROJECTILE.location())
            );
        }

        @Override
        public boolean canSerializeIn(HolderOwner<Enchantment> owner) {
            return true;
        }
    }
}
