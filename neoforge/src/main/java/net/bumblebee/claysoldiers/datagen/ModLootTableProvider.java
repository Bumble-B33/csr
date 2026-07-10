package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.ClaySoldiersNeoForge;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.loot.SetRandomClayMobTeam;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
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

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, Set.of(), List.of(
                new SubProviderEntry(BlockLootTableProvider::new, LootContextParamSets.BLOCK),
                new SubProviderEntry(BossLootTableProvider::new, LootContextParamSets.ENTITY),
                new SubProviderEntry(ChestLootSubProvider::new, LootContextParamSets.CHEST)

        ), pRegistries);
    }

    private static class BlockLootTableProvider extends BlockLootSubProvider {
        protected BlockLootTableProvider(HolderLookup.Provider lookupProvider) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), lookupProvider);
        }

        @Override
        protected void generate() {
            dropSelf(ModBlocks.HAMSTER_WHEEL_BLOCK.get());
            dropSelf(ModBlocks.EASEL_BLOCK.get());
            dropSelf(ModBlocks.ESCRITOIRE_BLOCK.get());
            dropSelf(ModBlocks.CHIP_ASSEMBLER.get());
            dropOther(ModBlocks.SUGAR_CANE_HAMMOCK.get(), Items.SUGAR_CANE);
            dropOther(ModBlocks.CACTUS_HOUSE.get(), Items.CACTUS);

        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ClaySoldiersNeoForge.BLOCKS.getEntries().stream().map(s -> (Block) s.get()).toList();
        }
    }

    private record ChestLootSubProvider(HolderLookup.Provider lookup) implements LootTableSubProvider {

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
            biConsumer.accept(ModLootTables.SMALL_HOUSE,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(ModItems.CLAY_COOKIE)
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1f, 3f)))
                                    )
                                    .add(LootItem.lootTableItem(ModItems.CLAY_DISRUPTOR))
                            )
                            .withPool(LootPool.lootPool()
                                    .setRolls(UniformGenerator.between(5f, 10f))
                                    .add(LootItem.lootTableItem(Items.CLAY_BALL)
                                            .setWeight(10)
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(2f, 5f)))
                                    )
                                    .add(LootItem.lootTableItem(Items.STICK)
                                            .setWeight(3)
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1f, 2f)))
                                    )
                                    .add(LootItem.lootTableItem(Items.COPPER_INGOT)
                                            .setWeight(3)
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1f, 2f)))
                                    )
                                    .add(LootItem.lootTableItem(Items.CLAY)
                                            .setWeight(2)
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1f, 2f)))
                                    )
                                    .add(LootItem.lootTableItem(ModItems.CLAY_SOLDIER)
                                            .setWeight(8)
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(3f, 7f)))
                                            .apply(SetRandomClayMobTeam.of())
                                    )
                            )
            );
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
                                                    .withEnchantment(createForSlingShot(lookup), ConstantValue.exactly(1f))
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
            biConsumer.accept(
                    ModBossBehaviours.ZOMBIE_LOOT_TABLE,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1f))
                                    .add(LootItem.lootTableItem(ModItems.CLAY_STAFF))
                                    .add(LootItem.lootTableItem(Items.BOOK)
                                            .apply(new SetEnchantmentsFunction.Builder()
                                                    .withEnchantment(createForSlingShot(lookup), ConstantValue.exactly(1f))
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

    public static Holder.Reference<Enchantment> createForSlingShot(HolderLookup.Provider lookup) {
        return lookup.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ModEnchantments.SOLDIER_PROJECTILE);
    }
}
