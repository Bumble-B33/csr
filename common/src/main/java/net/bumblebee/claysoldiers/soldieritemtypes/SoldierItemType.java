package net.bumblebee.claysoldiers.soldieritemtypes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class SoldierItemType {
    public static final Codec<SoldierItemType> CODEC = RecordCodecBuilder.create(in -> in.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(s -> s.tag),
            ModRegistries.ITEM_GENERATORS_REGISTRY.byNameCodec().fieldOf("generator").forGetter(s -> s.generator),
            Codec.STRING.optionalFieldOf("name", "").forGetter(s -> s.name)
    ).apply(in, SoldierItemType::new));
    public static final String LANG = "clay_soldier_item_type";
    private static final Logger LOGGER = ClaySoldiersCommon.LOGGER;

    @Nullable
    private static Runnable dataMapLoad = null;
    @Nullable
    private static Runnable postTagLoad = null;
    private static List<Generator> types;

    private final TagKey<Item> tag;
    private final ItemGenerator generator;
    private List<WeightedItem> available;
    @Nullable
    private String descriptionId;
    private final String name;

    public SoldierItemType(TagKey<Item> tag, ItemGenerator generator) {
        this(tag, generator, "");
    }
    public SoldierItemType(TagKey<Item> tag, ItemGenerator generator, String name) {
        this.tag = tag;
        this.generator = generator;
        this.name = name;
    }

    public static void onDataMapLoad(@NotNull Runnable runnable) {
        dataMapLoad = () -> {
            runnable.run();
            ClaySoldiersCommon.LOGGER.info("SoldierItemTypes: Weight finalized");
        };
        ClaySoldiersCommon.LOGGER.info("Datamap Loaded {}", (postTagLoad == null ? "Tags Not Loaded" : "Tags Loaded"));

        if (postTagLoad != null) {
            dataMapLoad.run();
            dataMapLoad = null;

            postTagLoad.run();
            postTagLoad = null;
        }
    }

    public static void onTagLoad(HolderLookup.Provider registries) {
        var reg = registries.lookupOrThrow(ModRegistries.SOLDIER_ITEM_TYPES);
        reg.listElements().forEach(type -> type.value().onTagLoad(tag -> registries.lookupOrThrow(Registries.ITEM).get(tag)));
        if (dataMapLoad != null) {
            postTagLoad(reg.listElements().map(Holder::value));
            dataMapLoad.run();
            dataMapLoad = null;
        } else {
            postTagLoad = () -> postTagLoad(reg.listElements().map(Holder::value));
        }
    }

    private static void postTagLoad(Stream< SoldierItemType> all) {
        ClaySoldiersCommon.LOGGER.info("Post Tag Loaded");
        types = all.filter(s -> !s.available.isEmpty() && s.generator.limitedBy() != ItemGenerator.Limit.ZERO).map(SoldierItemType::asGenerator).toList();
    }

    public void onTagLoad(Function<TagKey<Item>, Optional<HolderSet.Named<Item>>> tagGetter) {
        var opt = tagGetter.apply(tag);

        opt.ifPresentOrElse(
                holderSet -> {
                    available = holderSet.stream().map(h -> new WeightedItem(h.value())).toList();
                },
                () -> {
                    available = List.of();
                    LOGGER.warn("Tag {} for SoldierItemType {} not present", tag, name.isEmpty() ? descriptionId : name);
                }
        );
    }

    public void afterDataMapLoad() {
        if (available != null) {
            available = available.stream().filter(w -> w.finalizeWeight() > 0).toList();
        } else {
            throw new IllegalStateException("Cannot complete SoldierItemType before tags are loaded");
        }
    }



    public boolean isEmpty() {
        return available.isEmpty();
    }




    private Generator asGenerator() {
        return new Generator() {
            @Override
            public NonNullList<ItemStack> generateForTag(int count, RandomSource random) {
                return generator.generateForTag(available, count, random);
            }

            @Override
            public ItemGenerator.Limit limitedBy() {
                return generator.limitedBy();
            }
        };
    }

    public NonNullList<ItemStack> getItems(RandomSource random, int count) {
        if (available == null || types == null) {
            throw new IllegalStateException("Tried to generate items before tag loading");
        }
        return generator.generate(available, count, random, types);
    }

    @Override
    public String toString() {
        return "SoldierItemType(%s)[%s]".formatted(generator, available != null ? available.size() : "null");
    }

    public Component getDisplayName() {
        if (!name.isEmpty()) {
            return Component.literal(name);
        }
        return descriptionId != null ? Component.translatable(descriptionId) : Component.literal("[unregistered]");
    }
    public void onRegister(ResourceLocation id) {
        descriptionId = Util.makeDescriptionId(LANG, id);
    }
}