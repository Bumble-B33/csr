package net.bumblebee.claysoldiers.datagen.tags;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
                PoiTypeTagProvider::new
        );
    }

    private static class DamageTypeTagProvider extends DamageTypeTagsProvider {
        public DamageTypeTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(packOutput, completableFuture, ClaySoldiersCommon.MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            this.tag(ModTags.DamageTypes.CLAY_SOLDIER_DAMAGE)
                    .add(ForcedTagEntry.element(ModDamageTypes.CLAY_HURT))
                    .add(ForcedTagEntry.element(ModDamageTypes.CLAY_ON_FIRE))
                    .add(DamageTypes.THROWN, DamageTypes.THORNS)
                    .addTag(DamageTypeTags.IS_EXPLOSION)
                    .addOptional(NeoForgeMod.POISON_DAMAGE.location());
        }
    }

    private static class EntityTypeTagProvider extends EntityTypeTagsProvider {
        public EntityTypeTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(packOutput, completableFuture, ClaySoldiersCommon.MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            this.tag(EntityTypeTags.UNDEAD).add(ModEntityTypes.ZOMBIE_CLAY_SOLDIER_ENTITY.get(), ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY.get());
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
            this.tag(ModTags.SoldierPropertyTypes.REQUIRES_OWNER).add(SoldierPropertyTypes.TELEPORT_TO_OWNER.get(), SoldierPropertyTypes.BREAKING_POWER.get());
        }

        private static ResourceKey<SoldierPropertyType<?>> keyExtractor(SoldierPropertyType<?> type) {
            var res = ModRegistries.SOLDIER_PROPERTY_TYPES_REGISTRY.getKey(type);
            if (res == null) {
                ClaySoldiersCommon.LOGGER.error("Cannot create Tag with an Unregistered Property");
                throw new IllegalArgumentException("Cannot create Tag with an Unregistered Property");
            }
            return ResourceKey.create(ModRegistries.SOLDIER_PROPERTY_TYPES, res);
        }
    }

    private static class PoiTypeTagProvider extends PoiTypeTagsProvider {
        public PoiTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
            super(output, provider, ClaySoldiersCommon.MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            this.tag(ModTags.PoiTypes.SOLDIER_CONTAINER).add(ModPoiTypes.HAMSTER_WHEEL_POI_KEY);
        }
    }

    public static class ForcedTagEntry extends TagEntry {
        private final TagEntry delegate;

        public static TagEntry element(ResourceKey<?> key) {
            return new ForcedTagEntry(element(key.location()), false);
        }
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
        public boolean verifyIfPresent(Predicate<ResourceLocation> objectExistsTest, Predicate<ResourceLocation> tagExistsTest) {
            return true;
        }
    }
}
