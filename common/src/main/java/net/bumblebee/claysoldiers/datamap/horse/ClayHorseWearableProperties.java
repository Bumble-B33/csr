package net.bumblebee.claysoldiers.datamap.horse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public record ClayHorseWearableProperties(float protection, Item armorItem, ColorHelper color) {
    public static final Codec<ClayHorseWearableProperties> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.FLOAT.optionalFieldOf("protection", 0f).forGetter(ClayHorseWearableProperties::protection),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("armor_item").forGetter(ClayHorseWearableProperties::armorItem),
            ColorHelper.CODEC.optionalFieldOf("color", ColorHelper.EMPTY).forGetter(ClayHorseWearableProperties::color)
    ).apply(in, ClayHorseWearableProperties::new));
}
