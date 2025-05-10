package net.bumblebee.claysoldiers.soldierproperties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicate;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicates;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SoldierVehicleProperties(SoldierPropertyMap properties, ClayPredicate<?> predicate) {
    public static final SoldierVehicleProperties EMPTY = new SoldierVehicleProperties(SoldierPropertyMap.EMPTY_MAP, ClayPredicates.ConstantPredicate.getAlwaysTruePredicate());
    public static final Codec<SoldierVehicleProperties> CODEC = RecordCodecBuilder.create(in -> in.group(
            SoldierPropertyMap.CODEC_FOR_NON_ITEM.fieldOf("properties").forGetter(SoldierVehicleProperties::properties),
            ClayPredicate.CODEC.optionalFieldOf("predicate", ClayPredicates.ConstantPredicate.getAlwaysTruePredicate()).forGetter(SoldierVehicleProperties::predicate)
    ).apply(in, SoldierVehicleProperties::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SoldierVehicleProperties> STREAM_CODEC = StreamCodec.composite(
            SoldierPropertyMap.STREAM_CODEC, SoldierVehicleProperties::properties,
            ClayPredicate.STREAM_CODEC, SoldierVehicleProperties::predicate,
            SoldierVehicleProperties::new
    );

    public SoldierVehicleProperties(SoldierPropertyMap properties) {
        this(properties, ClayPredicates.ConstantPredicate.getAlwaysTruePredicate());
    }
}
