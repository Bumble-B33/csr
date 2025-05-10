package net.bumblebee.claysoldiers.datagen.tags;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ModTagProvider {
    public static List<TagsProvider<?>> getTagProviders(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        return List.of(
                new DamageTypeTagProvider(output, provider, existingFileHelper),
                new EntityTypeTagProvider(output, provider, existingFileHelper),
                new EnchantmentTagProvider(output, provider, existingFileHelper),
                new SoldierPropertyTypeTagProvider(output, provider, existingFileHelper),
                new PoiTypeTagProvider(output, provider, existingFileHelper)
        );
    }

    private static class DamageTypeTagProvider extends DamageTypeTagsProvider {
        public DamageTypeTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, @Nullable ExistingFileHelper existingFileHelper) {
            super(packOutput, completableFuture, ClaySoldiersCommon.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            this.tag(ModTags.DamageTypes.CLAY_SOLDIER_DAMAGE)
                    .addOptional(ModDamageTypes.CLAY_HURT.location())
                    .addOptional(ModDamageTypes.CLAY_ON_FIRE.location())
                    .add(DamageTypes.THROWN, DamageTypes.THORNS).addTag(DamageTypeTags.IS_EXPLOSION).addOptional(NeoForgeMod.POISON_DAMAGE.location());
        }
    }

    private static class EntityTypeTagProvider extends EntityTypeTagsProvider {
        public EntityTypeTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, @Nullable ExistingFileHelper existingFileHelper) {
            super(packOutput, completableFuture, ClaySoldiersCommon.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            this.tag(EntityTypeTags.UNDEAD).add(ModEntityTypes.ZOMBIE_CLAY_SOLDIER_ENTITY.get(), ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY.get());
            this.tag(ModTags.EntityTypes.CLAY_BOSS).add(ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get());
        }
    }

    private static class EnchantmentTagProvider extends EnchantmentTagsProvider {
        public EnchantmentTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, lookupProvider, ClaySoldiersCommon.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            this.tag(ModTags.Enchantments.SOLDIER_SLINGSHOT_EXCLUSIVE).add(Enchantments.PIERCING);
        }
    }

    private static class SoldierPropertyTypeTagProvider extends IntrinsicHolderTagsProvider<SoldierPropertyType<?>> {
        public SoldierPropertyTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, ModRegistries.SOLDIER_PROPERTY_TYPES, lookupProvider, SoldierPropertyTypeTagProvider::keyExtractor, ClaySoldiersCommon.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            this.tag(ModTags.SoldierPropertyTypes.REQUIRES_OWNER).add(SoldierPropertyTypes.TELEPORT_TO_OWNER.get());
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
        public PoiTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, provider, ClaySoldiersCommon.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            this.tag(ModTags.PoiTypes.SOLDIER_CONTAINER).add(ModPoiTypes.HAMSTER_WHEEL_POI_KEY);
        }
    }
}
