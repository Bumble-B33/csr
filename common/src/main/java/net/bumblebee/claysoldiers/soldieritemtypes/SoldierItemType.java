package net.bumblebee.claysoldiers.soldieritemtypes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.Util;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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

    private static Runnable postTagLoad = () -> {};
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


    public void onTagLoad(Function<TagKey<Item>, Optional<HolderSet.Named<Item>>> tagGetter) {
        var opt = tagGetter.apply(tag);

        opt.ifPresentOrElse(
                holderSet -> {
                    available = holderSet.stream().map(h -> new WeightedItem(h.value())).toList();
                },
                () -> available = List.of()
        );
    }

    public void afterDataMapLoad() {
        if (available == null) {
            throw new IllegalStateException("Cannot complete SoldierItemType before tags are loaded");
        }
        available = available.stream().filter(w -> w.finalizeWeight() > 0).toList();
    }

    public boolean isEmpty() {
        return available.isEmpty();
    }

    public static void postTagLoad(Stream<SoldierItemType> all) {
        types = all.filter(s -> !s.available.isEmpty() && s.generator.limitedBy() != ItemGenerator.Limit.ZERO).map(SoldierItemType::asGenerator).toList();
        postTagLoad.run();
    }

    public static void setTagLoadCallback(Runnable runnable) {
        postTagLoad = runnable;
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