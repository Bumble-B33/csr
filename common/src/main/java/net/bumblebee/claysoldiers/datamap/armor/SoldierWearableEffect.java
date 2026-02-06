package net.bumblebee.claysoldiers.datamap.armor;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class SoldierWearableEffect {
    public static final Codec<SoldierWearableEffect> CODEC = RecordCodecBuilder.create(in -> in.group(
                    ArmorModel.CODEC.optionalFieldOf("model", ArmorModel.EMPTY).forGetter(s -> s.armorModel),
                    CodecUtils.singularOrPluralCodecOptional(SoldierArmorTrim.CODEC, "trim").forGetter(SoldierWearableEffect::trims),
                    ColorHelper.CODEC.optionalFieldOf("color", ColorHelper.EMPTY).forGetter(SoldierWearableEffect::getColorHelper),
                    Codec.BOOL.optionalFieldOf("offset_color", false).forGetter(s -> s.offsetColor))
            .apply(in, SoldierWearableEffect::createSided)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SoldierWearableEffect> STREAM_CODEC = StreamCodec.composite(
            ArmorModel.STREAM_CODEC, s -> s.armorModel,
            SoldierArmorTrim.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), SoldierWearableEffect::trims,
            ColorHelper.STREAM_CODEC, SoldierWearableEffect::getColorHelper,
            ByteBufCodecs.BOOL, SoldierWearableEffect::isAffectedByOffsetColor,
            (SoldierWearableEffect::createSided)
    );

    private final ColorHelper color;
    private final ArmorModel armorModel;
    protected final Set<SoldierArmorTrim> trims;

    private final boolean offsetColor;

    private SoldierWearableEffect(ArmorModel model, ColorHelper color, Set<SoldierArmorTrim> trims, boolean offsetColor) {
        this.color = color;
        this.armorModel = model;
        this.trims = trims;
        this.offsetColor = offsetColor;
    }

    @ApiStatus.Internal
    public SoldierWearableEffect(@Nullable Item item, ColorHelper color, Set<SoldierArmorTrim> trims, boolean offsetColor) {
        this(item == null ? ArmorModel.EMPTY : new ArmorModel(item), color, trims, offsetColor);
    }

    public ColorHelper getColorHelper() {
        return color;
    }

    @Nullable
    protected Item copyModel() {
        return armorModel.item;
    }

    private Set<SoldierArmorTrim> trims() {
        return trims;
    }


    public boolean isAffectedByOffsetColor() {
        return offsetColor;
    }

    private static SoldierWearableEffect createSided(ArmorModel model, Set<SoldierArmorTrim> trims, ColorHelper color, boolean colorOffset) {
        if (ClaySoldiersCommon.PLATFORM.isClient()) {
            return ClientSoldierWearableEffect.create(model.item, color, trims, colorOffset);
        } else {
            return new SoldierWearableEffect(model, color, trims, colorOffset);
        }
    }

    public void buildTrims(HolderLookup.Provider registryAccess) {
    }

    @Override
    public String toString() {
        return "%s{%s, %s}".formatted(this.getClass().getSimpleName(), armorModel, !trims.isEmpty() ? "Trims(" + trims.size()  + ")" : "No Trims");
    }

    public static class SoldierArmorTrim {
        private static final Codec<SoldierArmorTrim> CODEC = RecordCodecBuilder.create(in -> in.group(
                ResourceLocation.CODEC.fieldOf("pattern").forGetter(t -> t.pattern.location()),
                ResourceLocation.CODEC.fieldOf("material").forGetter(t -> t.material.location()),
                ColorHelper.CODEC.optionalFieldOf("color", ColorHelper.EMPTY).forGetter(t -> t.color)
        ).apply(in, SoldierArmorTrim::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, SoldierArmorTrim> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, s -> s.pattern.location(),
                ResourceLocation.STREAM_CODEC, s -> s.material.location(),
                ColorHelper.STREAM_CODEC, s -> s.color,
                SoldierArmorTrim::new
        );

        private final ResourceKey<TrimPattern> pattern;
        private final ResourceKey<TrimMaterial> material;
        private final ColorHelper color;

        private SoldierArmorTrim(ResourceLocation pattern, ResourceLocation material, ColorHelper color) {
            this(ResourceKey.create(Registries.TRIM_PATTERN, pattern), ResourceKey.create(Registries.TRIM_MATERIAL, material), color);
        }

        public SoldierArmorTrim(ResourceKey<TrimPattern> pattern, ResourceKey<TrimMaterial> material, ColorHelper color) {
            this.pattern = pattern;
            this.material = material;
            this.color = color;
        }


        @Nullable
        public ArmorTrim createTrim(HolderLookup.Provider access) {
            Optional<Holder.Reference<TrimPattern>> trimPattern = access.lookupOrThrow(Registries.TRIM_PATTERN).get(pattern);
            Optional<Holder.Reference<TrimMaterial>> trimMaterial = access.lookupOrThrow(Registries.TRIM_MATERIAL).get(material);
            if (trimPattern.isPresent() && trimMaterial.isPresent()) {
                return new ArmorTrim(trimMaterial.orElseThrow(), trimPattern.orElseThrow());
            }
            return null;
        }

        public ColorHelper getColor() {
            return color;
        }
    }

    private record ArmorModel(Item item, boolean empty) {
        private static final ArmorModel EMPTY = new ArmorModel(null, true);
        private static final Codec<ArmorModel> CODEC = Codec.either(BuiltInRegistries.ITEM.byNameCodec(), Codec.BOOL).xmap(
                ArmorModel::fromEither, a -> a.empty ? Either.right(true) : Either.left(a.item)
        );
        private static final StreamCodec<RegistryFriendlyByteBuf, ArmorModel> STREAM_CODEC = ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.ITEM))
                .map(s -> s.map(ArmorModel::new).orElse(EMPTY), a -> a.empty ? Optional.empty() : Optional.of(a.item));

        private ArmorModel(Item item) {
            this(Objects.requireNonNull(item), false);
        }

        @Override
        public String toString() {
            return "ArmorModel[%s]".formatted(empty ? "Empty" : item);
        }

        private static ArmorModel fromEither(Either<Item, Boolean> either) {
            if (either.right().isPresent()) {
                return EMPTY;
            }
            return new ArmorModel(either.left().orElseThrow());
        }
    }
}
