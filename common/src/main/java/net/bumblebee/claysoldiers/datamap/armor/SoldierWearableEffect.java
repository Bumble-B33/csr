package net.bumblebee.claysoldiers.datamap.armor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SoldierWearableEffect {
    public static final Codec<SoldierWearableEffect> CODEC = RecordCodecBuilder.create(in -> in.group(
                    ResourceKey.codec(EquipmentAssets.ROOT_ID).optionalFieldOf("model").forGetter(s -> Optional.ofNullable(s.armorModel)),
                    ColorHelper.CODEC.optionalFieldOf("color", ColorHelper.EMPTY).forGetter(SoldierWearableEffect::getColorHelper),
                    CodecUtils.singularOrPluralCodecOptional(SoldierArmorTrim.CODEC, "trim").forGetter(SoldierWearableEffect::trims),
                    Codec.BOOL.optionalFieldOf("offset_color", false).forGetter(s -> s.offsetColor))
            .apply(in, ((model, color, trims, offsetColor) -> new SoldierWearableEffect(model.orElse(null), color, trims, offsetColor)))
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SoldierWearableEffect> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ResourceKey.streamCodec(EquipmentAssets.ROOT_ID)), s -> Optional.ofNullable(s.armorModel),
            ColorHelper.STREAM_CODEC, SoldierWearableEffect::getColorHelper,
            SoldierArmorTrim.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), SoldierWearableEffect::trims,
            ByteBufCodecs.BOOL, SoldierWearableEffect::isAffectedByOffsetColor,
            (model, color, trim, offestColor) ->  new SoldierWearableEffect(model.orElse(null), color, trim, offestColor)
    );

    private final ColorHelper color;
    @Nullable
    private final ResourceKey<EquipmentAsset> armorModel;
    private final boolean shouldRenderArmor;
    protected final Set<SoldierArmorTrim> trims;
    private final boolean offsetColor;

    private List<TrimHolder> finishedArmorTrims;


    protected SoldierWearableEffect(@Nullable ResourceKey<EquipmentAsset> model, ColorHelper color, Set<SoldierArmorTrim> trims, boolean offsetColor) {
        this.color = color;
        this.armorModel = model;
        this.trims = trims;
        this.offsetColor = offsetColor;
        this.shouldRenderArmor = model != null;
    }

    public ColorHelper getColorHelper() {
        return color;
    }

    public boolean shouldRenderArmor() {
        return shouldRenderArmor;
    }
    @Nullable
    public ResourceKey<EquipmentAsset> getAssetId() {
        return armorModel;
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
                ClaySoldiersCommon.ERROR_HANDLER.error("Failed to create an ArmorTrim for " + trim);
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
                Identifier.CODEC.fieldOf("pattern").forGetter(t -> t.pattern.identifier()),
                Identifier.CODEC.fieldOf("material").forGetter(t -> t.material.identifier()),
                ColorHelper.CODEC.optionalFieldOf("color", ColorHelper.EMPTY).forGetter(t -> t.color)
        ).apply(in, SoldierArmorTrim::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, SoldierArmorTrim> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, s -> s.pattern.identifier(),
                Identifier.STREAM_CODEC, s -> s.material.identifier(),
                ColorHelper.STREAM_CODEC, s -> s.color,
                SoldierArmorTrim::new
        );

        private final ResourceKey<TrimPattern> pattern;
        private final ResourceKey<TrimMaterial> material;
        private final ColorHelper color;

        private SoldierArmorTrim(Identifier pattern, Identifier material, ColorHelper color) {
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

    public record TrimHolder(ArmorTrim trim, ColorHelper color) {}

    public static Builder armor(@NotNull ResourceKey<EquipmentAsset> armorCopy) {
        return new Builder(armorCopy);
    }

    public static Builder empty() {
        return new Builder(null);
    }

    public static class Builder {
        private final Set<SoldierWearableEffect.SoldierArmorTrim> trims = new HashSet<>();
        private ColorHelper color = ColorHelper.EMPTY;
        private final ResourceKey<EquipmentAsset> armorCopy;
        private boolean offsetColor = false;

        private Builder(@Nullable ResourceKey<EquipmentAsset> armorCopy) {
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
