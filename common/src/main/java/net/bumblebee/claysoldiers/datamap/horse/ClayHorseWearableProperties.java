package net.bumblebee.claysoldiers.datamap.horse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ClayHorseWearableProperties {
    public static final Codec<ClayHorseWearableProperties> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.FLOAT.optionalFieldOf("protection", 0f).forGetter(ClayHorseWearableProperties::protection),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("armor_item").forGetter(ClayHorseWearableProperties::armorItem),
            ColorHelper.CODEC.optionalFieldOf("color", ColorHelper.EMPTY).forGetter(ClayHorseWearableProperties::color),
            ClayHorseSlot.CODEC.optionalFieldOf("slot", ClayHorseSlot.ARMOR).forGetter(ClayHorseWearableProperties::getSlot)
    ).apply(in, ClayHorseWearableProperties::new));
    private final float protection;
    private final Item armorItem;
    private final ColorHelper color;
    @Nullable
    private Equippable equippable;
    private final ItemStack stack;
    private final ClayHorseSlot slot;

    public ClayHorseWearableProperties(float protection, Item armorItem, ColorHelper color, ClayHorseSlot slot) {
        this.protection = protection;
        this.armorItem = armorItem;
        this.color = color;
        this.stack = armorItem.getDefaultInstance();
        this.slot = slot;
    }

    public static Builder of(ClayHorseSlot slot) {
        return new Builder(slot);
    }

    public float protection() {
        return protection;
    }

    public Item armorItem() {
        return armorItem;
    }

    public ColorHelper color() {
        return color;
    }

    public ItemStack getStack() {
        return stack;
    }

    public @Nullable Equippable getEquippable() {
        if (equippable == null) {
            equippable = armorItem.getDefaultInstance().get(DataComponents.EQUIPPABLE);
        }
        return equippable;
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
                Objects.equals(this.armorItem, that.armorItem) &&
                Objects.equals(this.color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protection, armorItem, color);
    }

    @Override
    public String toString() {
        return "ClayHorseWearableProperties[" +
                "protection=" + protection + ", " +
                "armorItem=" + armorItem + ", " +
                "color=" + color + ']';
    }

    public static class Builder {
        private float protection = 0f;
        private Item armorItem = Items.AIR;
        private ColorHelper color = ColorHelper.EMPTY;
        private final ClayHorseSlot slot;

        public Builder(ClayHorseSlot slot) {
            this.slot = slot;
        }

        public Builder protection(float protection) {
            this.protection = protection;
            return this;
        }

        public Builder armorItem(Item armorItem) {
            this.armorItem = armorItem != null ? armorItem : Items.AIR;
            return this;
        }

        public Builder color(ColorHelper color) {
            this.color = color != null ? color : ColorHelper.EMPTY;
            return this;
        }

        public ClayHorseWearableProperties build() {
            return new ClayHorseWearableProperties(
                    protection,
                    armorItem,
                    color,
                    slot
            );
        }
    }


}
