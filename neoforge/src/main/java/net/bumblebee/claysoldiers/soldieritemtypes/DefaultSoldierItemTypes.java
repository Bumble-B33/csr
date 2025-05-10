package net.bumblebee.claysoldiers.soldieritemtypes;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datagen.api.ClaySoldiersItemProvider;
import net.bumblebee.claysoldiers.init.ModItemGenerators;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.soldierproperties.translation.KeyableTranslatableProperty;
import net.minecraft.Util;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public enum DefaultSoldierItemTypes implements ClaySoldiersItemProvider.ItemTagHolder, KeyableTranslatableProperty {
    BASIC("basic", ModTags.Items.BASIC),
    RANGED("ranged", ModTags.Items.RANGED),
    TANK("tank", ModTags.Items.TANK),
    ARMORED("armored", ModTags.Items.ARMORED),
    SPECIALIST("specialist", ModTags.Items.SPECIALIST),
    HEALER("support", ModTags.Items.HEALER),
    DIVER("diver", ModTags.Items.DIVER),
    ARSONIST("arsonist", ModTags.Items.ARSONIST),
    MAGICIAN("magician", ModTags.Items.MAGICIAN),
    ROYALTY("royalty", ModTags.Items.ROYALTY, ModItemGenerators.ONE_OF_EACH),
    FASHION("fashion", ModTags.Items.FASHION, ModItemGenerators.ONE_OF_EACH_NO_TAG),
    KINGDOM("kingdom", ModTags.Items.KINGDOM, ModItemGenerators.COMBINED_DEFAULTED);

    private final String name;
    private final TagKey<Item> tag;
    private final Supplier<ItemGenerator> generator;

    DefaultSoldierItemTypes(String name, TagKey<Item> tag, Supplier<ItemGenerator> generator) {
        this.name = name;
        this.tag = tag;
        this.generator = generator;
    }
    DefaultSoldierItemTypes(String name, TagKey<Item> tag) {
        this(name, tag, ModItemGenerators.DEFAULT);
    }

    public static void registerAll(BootstrapContext<SoldierItemType> context) {
        for (DefaultSoldierItemTypes value : DefaultSoldierItemTypes.values()) {
            value.register(context);
        }
    }

    private void register(BootstrapContext<SoldierItemType> context) {
        context.register(
                ResourceKey.create(ModRegistries.SOLDIER_ITEM_TYPES, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, name)),
                new SoldierItemType(tag, generator.get()));
    }

    @Override
    public TagKey<Item> getTag() {
        return tag;
    }

    @Override
    public String translatableKey() {
        return Util.makeDescriptionId(SoldierItemType.LANG, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, name));
    }
}