package net.bumblebee.claysoldiers.datamap.armor;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.util.ErrorHandler;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SoldierWearableEffect {
    public static final Codec<SoldierWearableEffect> CODEC = RecordCodecBuilder.create(in -> in.group(
                    ArmorModel.CODEC.optionalFieldOf("model", ArmorModel.EMPTY).forGetter(s -> s.armorModel),
                    ColorHelper.CODEC.optionalFieldOf("color", ColorHelper.EMPTY).forGetter(SoldierWearableEffect::getColorHelper),
                    CodecUtils.singularOrPluralCodecOptional(SoldierArmorTrim.CODEC, "trim").forGetter(SoldierWearableEffect::trims),
                    Codec.BOOL.optionalFieldOf("offset_color", false).forGetter(s -> s.offsetColor))
            .apply(in, SoldierWearableEffect::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SoldierWearableEffect> STREAM_CODEC = StreamCodec.composite(
            ArmorModel.STREAM_CODEC, s -> s.armorModel,
            ColorHelper.STREAM_CODEC, SoldierWearableEffect::getColorHelper,
            SoldierArmorTrim.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), SoldierWearableEffect::trims,
            ByteBufCodecs.BOOL, SoldierWearableEffect::isAffectedByOffsetColor,
            SoldierWearableEffect::new
    );

    private final ColorHelper color;
    @NotNull
    private final ArmorModel armorModel;
    private final boolean shouldRenderArmor;
    protected final Set<SoldierArmorTrim> trims;
    private final boolean offsetColor;

    private List<TrimHolder> finishedArmorTrims;


    protected SoldierWearableEffect(ArmorModel model, ColorHelper color, Set<SoldierArmorTrim> trims, boolean offsetColor) {
        this.color = color;
        this.armorModel = model;
        this.trims = trims;
        this.offsetColor = offsetColor;
        this.shouldRenderArmor = model.assetId != null;
    }

    public ColorHelper getColorHelper() {
        return color;
    }

    public boolean shouldRenderArmor() {
        return shouldRenderArmor;
    }
    @Nullable
    public ResourceKey<EquipmentAsset> getAssetId() {
        return armorModel.assetId;
    }

    private Set<SoldierArmorTrim> trims() {
        return trims;
    }


    public boolean isAffectedByOffsetColor() {
        return offsetColor;
    }

    public void buildTrims(HolderLookup.Provider registryAccess) {
        if (finishedArmorTrims != null) {
            return;
        }
        finishedArmorTrims = new ArrayList<>(trims.size());

        for (SoldierArmorTrim trim : trims) {
            ArmorTrim armorTrim = trim.createTrim(registryAccess);
            if (armorTrim != null) {
                finishedArmorTrims.add(new TrimHolder(armorTrim, trim.getColor()));
            } else {
                ErrorHandler.INSTANCE.error("Failed to create an ArmorTrim for " + trim);
            }
        }
    }

    public Iterable<TrimHolder> getArmorTrims() {
        return finishedArmorTrims == null ? List.of() : finishedArmorTrims;
    }

    @Override
    public String toString() {
        return "%s{%s, %s}".formatted(this.getClass().getSimpleName(),
                getAssetId() != null ? getAssetId() : "NoArmor",
                "Finished Trims(" + finishedArmorTrims.size() + ")"
        );
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

    protected static final class ArmorModel {
        private static final ArmorModel EMPTY = new ArmorModel(null);
        private static final Codec<ArmorModel> CODEC = Codec.either(
                BuiltInRegistries.ITEM.byNameCodec(), ResourceLocation.CODEC
        ).xmap(ArmorModel::fromEither, s -> Either.right(s.assetId.location())).orElse(EMPTY);

        private static final StreamCodec<ByteBuf, ArmorModel> STREAM_CODEC = ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC)
                .map(s -> s.map(ArmorModel::of).orElse(EMPTY), a -> a.assetId == null ? Optional.empty() : Optional.of(a.assetId.location()));

        @Nullable
        private final ResourceKey<EquipmentAsset> assetId;

        private ArmorModel(ResourceKey<EquipmentAsset> asset) {
            this.assetId = asset;
        }

        private static ArmorModel fromEither(Either<Item, ResourceLocation> either) {
            if (either.right().isPresent()) {
                return ArmorModel.of(either.right().orElseThrow());
            }
            return ArmorModel.of(either.left().orElseThrow());
        }

        private static ArmorModel of(@Nullable ResourceLocation resourceLocation) {
            if (resourceLocation == null) {
                return EMPTY;
            }
            return new ArmorModel(ResourceKey.create(EquipmentAssets.ROOT_ID, resourceLocation));
        }

        private static ArmorModel of(@Nullable Item item) {
            if (item == null) {
                return EMPTY;
            }
            Equippable equippable = item.getDefaultInstance().get(DataComponents.EQUIPPABLE);
            if (equippable == null) {
                return EMPTY;
            }
            return equippable.assetId().map(ArmorModel::new).orElse(EMPTY);
        }

        @Override
        public String toString() {
            return "ArmorModel[%s]".formatted(assetId == null ? "Empty" : assetId);
        }
    }

    public record TrimHolder(ArmorTrim trim, ColorHelper color) {}

    public static Builder armor(Item armorCopy) {
        return new Builder(ArmorModel.of(armorCopy));
    }

    public static Builder armor(@NotNull ResourceKey<EquipmentAsset> armorCopy) {
        return new Builder(new ArmorModel(armorCopy));
    }

    public static Builder empty() {
        return new Builder(ArmorModel.EMPTY);
    }

    public static class Builder {
        private final Set<SoldierWearableEffect.SoldierArmorTrim> trims = new HashSet<>();
        private ColorHelper color = ColorHelper.EMPTY;
        private final ArmorModel armorCopy;
        private boolean offsetColor = false;

        private Builder(@Nullable ArmorModel armorCopy) {
            this.armorCopy = armorCopy;
        }


        public Builder color(ColorHelper color) {
            this.color = color;
            return this;
        }

        public Builder color(int color) {
            this.color = ColorHelper.color(color);
            return this;
        }

        public Builder affectedOffsetColor() {
            this.offsetColor = true;
            return this;
        }

        public Builder addTrim(ResourceKey<TrimPattern> pattern, ResourceKey<TrimMaterial> material) {
            return addTrim(pattern, material, ColorHelper.EMPTY);
        }

        public Builder addTrim(ResourceKey<TrimPattern> pattern, ResourceKey<TrimMaterial> material, ColorHelper color) {
            this.trims.add(new SoldierWearableEffect.SoldierArmorTrim(pattern, material, color));
            return this;
        }

        public SoldierWearableEffect build() {
            return new SoldierWearableEffect(armorCopy, color, trims, offsetColor);
        }
    }
}
