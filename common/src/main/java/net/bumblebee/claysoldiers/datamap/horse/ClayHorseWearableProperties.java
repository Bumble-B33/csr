package net.bumblebee.claysoldiers.datamap.horse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public final class ClayHorseWearableProperties {
    public static final Codec<ClayHorseWearableProperties> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.FLOAT.optionalFieldOf("protection", 0f).forGetter(ClayHorseWearableProperties::protection),
            ColorHelper.CODEC.optionalFieldOf("color", ColorHelper.EMPTY).forGetter(ClayHorseWearableProperties::color),
            ClayHorseSlot.CODEC.optionalFieldOf("slot", ClayHorseSlot.ARMOR).forGetter(ClayHorseWearableProperties::getSlot),
            ResourceKey.codec(EquipmentAssets.ROOT_ID).optionalFieldOf("asset_id").forGetter(ClayHorseWearableProperties::getAsset)
    ).apply(in, (pr, c, s, e) -> new ClayHorseWearableProperties(pr, c, s, e.orElse(null))));
    private final float protection;
    private final ColorHelper color;
    @Nullable
    private final ResourceKey<EquipmentAsset> assetId;
    private final ClayHorseSlot slot;

    private ClayHorseWearableProperties(float protection, ColorHelper color, ClayHorseSlot slot, @Nullable ResourceKey<EquipmentAsset> assetId) {
        this.protection = protection;
        this.color = color;
        this.slot = slot;
        this.assetId = assetId;
    }

    public static Builder of(ClayHorseSlot slot) {
        return new Builder(slot);
    }

    public float protection() {
        return protection;
    }

    public ColorHelper color() {
        return color;
    }

    public @Nullable ResourceKey<EquipmentAsset> assetId() {
        return assetId;
    }

    private Optional<ResourceKey<EquipmentAsset>> getAsset() {
        return Optional.ofNullable(assetId);
    }

    public ClayHorseSlot getSlot() {
        return slot;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ClayHorseWearableProperties) obj;
        return Float.floatToIntBits(this.protection) == Float.floatToIntBits(that.protection) &&
                Objects.equals(this.color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protection, color);
    }

    @Override
    public String toString() {
        return "ClayHorseWearableProperties[" +
                "protection=" + protection + ", " +
                "armor=" + assetId + ", " +
                "color=" + color + ']';
    }

    public static class Builder {
        private float protection = 0f;
        private ColorHelper color = ColorHelper.EMPTY;
        private final ClayHorseSlot slot;
        @Nullable
        private ResourceKey<EquipmentAsset> equippable;

        public Builder(ClayHorseSlot slot) {
            this.slot = slot;
        }

        public Builder protection(float protection) {
            this.protection = protection;
            return this;
        }

        public Builder color(ColorHelper color) {
            this.color = color != null ? color : ColorHelper.EMPTY;
            return this;
        }


        public Builder setArmor(ResourceKey<EquipmentAsset> armor) {
            equippable = armor;
            return this;
        }

        public ClayHorseWearableProperties build() {
            return new ClayHorseWearableProperties(
                    protection,
                    color,
                    slot,
                    equippable
            );
        }
    }


}
