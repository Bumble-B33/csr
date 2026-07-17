package net.bumblebee.claysoldiers.datagen.tags;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChips;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class ModTagProvider {
    public static List<GatherDataEvent.DataProviderFromOutputLookup<TagsProvider<?>>> getTagProviders() {
        return List.of(
                DamageTypeTagProvider::new,
                EntityTypeTagProvider::new,
                EnchantmentTagProvider::new,
                SoldierPropertyTypeTagProvider::new,
                PoiTypeTagProvider::new,
                ChipTagProvider::new
        );
    }

    private static class DamageTypeTagProvider extends DamageTypeTagsProvider {
        public DamageTypeTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(packOutput, completableFuture, ClaySoldiersCommon.MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            this.tag(ModTags.DamageTypes.CLAY_SOLDIER_DAMAGE)
                    .add(ModDamageTypes.CLAY_ON_FIRE)
                    .add(ModDamageTypes.CLAY_HURT)
                    .add(DamageTypes.THORNS)
                    .add(DamageTypes.THROWN)
                    .addTag(DamageTypeTags.IS_EXPLOSION)
                    .addOptional(NeoForgeMod.POISON_DAMAGE);
        }
    }

    private static class EntityTypeTagProvider extends EntityTypeTagsProvider {
        public EntityTypeTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(packOutput, completableFuture, ClaySoldiersCommon.MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            this.tag(EntityTypeTags.UNDEAD).add(ModEntityTypes.ZOMBIE_CLAY_SOLDIER_ENTITY.get(), ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY.get());
            this.tag(EntityTypeTags.BURN_IN_DAYLIGHT).add(ModEntityTypes.ZOMBIE_CLAY_SOLDIER_ENTITY.get(), ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY.get());

            this.tag(ModTags.EntityTypes.CLAY_BOSS).add(ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get());

            this.tag(ModTags.EntityTypes.CLAY_SOLDIER)
                    .add(ModEntityTypes.CLAY_SOLDIER_ENTITY.get())
                    .add(ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY.get())
                    .add(ModEntityTypes.ZOMBIE_CLAY_SOLDIER_ENTITY.get());
        }
    }

    private static class EnchantmentTagProvider extends EnchantmentTagsProvider {
        public EnchantmentTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider, ClaySoldiersCommon.MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            this.tag(ModTags.Enchantments.SOLDIER_SLINGSHOT_EXCLUSIVE).add(Enchantments.PIERCING);
        }
    }

    private static class SoldierPropertyTypeTagProvider extends IntrinsicHolderTagsProvider<SoldierPropertyType<?>> {
        public SoldierPropertyTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, ModRegistries.SOLDIER_PROPERTY_TYPES, lookupProvider, SoldierPropertyTypeTagProvider::keyExtractor, ClaySoldiersCommon.MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            this.tag(ModTags.SoldierPropertyTypes.REQUIRES_OWNER)
                    .add(SoldierPropertyTypes.TELEPORT_TO_OWNER.get(), SoldierPropertyTypes.BREAKING_POWER.get(), SoldierPropertyTypes.LUCK.get());
        }

        private static ResourceKey<SoldierPropertyType<?>> keyExtractor(SoldierPropertyType<?> type) {
            return ModTagProvider.keyExtractor(ModRegistries.SOLDIER_PROPERTY_TYPES_REGISTRY, type);
        }
    }


    private static class PoiTypeTagProvider extends PoiTypeTagsProvider {
        public PoiTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
            super(output, provider, ClaySoldiersCommon.MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            this.tag(ModTags.PoiTypes.SOLDIER_CONTAINER)
                    .add(ModPoiTypes.SINGLE_SOLDIER_CONTAINER_POI_KEY)
                    .add(ModPoiTypes.CLAY_CACTUS_POI_KEY);
        }
    }

    private static class ChipTagProvider extends IntrinsicHolderTagsProvider<ClaySoldierChip.Type<?>> {
        public ChipTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, ModRegistries.CLAY_SOLDIER_MODULES, lookupProvider, ChipTagProvider::keyExtractor, ClaySoldiersCommon.MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            this.tag(ModTags.ClaySoldierChips.REQUIRES_POI_POS)
                    .add(
                            ClaySoldierChips.USE_POI.get(),
                            ClaySoldierChips.DIG_TYPE.get(),
                            ClaySoldierChips.BUILD_BLUEPRINT_TYPE.get(),
                            ClaySoldierChips.PLACE_SEEDS_TYPE.get(),
                            ClaySoldierChips.PICK_UP_ITEMS_TYPE.get(),
                            ClaySoldierChips.BEEKEEPING_TYPE.get()
                    );
        }

        private static ResourceKey<ClaySoldierChip.Type<?>> keyExtractor(ClaySoldierChip.Type<?> type) {
            return ModTagProvider.keyExtractor(ModRegistries.CLAY_SOLDIER_MODULES_REGISTRY, type);
        }
    }


    private static <T> ResourceKey<T> keyExtractor(Registry<T> registry, T type) {
        var res = registry.getKey(type);
        if (res == null) {
            ClaySoldiersCommon.LOGGER.error("Cannot create Tag with an Unregistered Property");
            throw new IllegalArgumentException("Cannot create Tag with an Unregistered Property");
        }
        return ResourceKey.create(registry.key(), res);
    }

    public static class ForcedTagEntry extends TagEntry {
        private final TagEntry delegate;

        public static TagEntry tag(TagKey<?> tag) {
            return new ForcedTagEntry(tag(tag.location()), true);
        }

        public ForcedTagEntry(TagEntry delegate, boolean tag) {
            super(delegate.id, tag, delegate.required);
            this.delegate = delegate;
        }

        @Override
        public <T> boolean build(TagEntry.Lookup<T> arg, Consumer<T> consumer) {
            return delegate.build(arg, consumer);
        }

        @Override
        public boolean verifyIfPresent(Predicate<Identifier> objectExistsTest, Predicate<Identifier> tagExistsTest) {
            return true;
        }
    }
}
