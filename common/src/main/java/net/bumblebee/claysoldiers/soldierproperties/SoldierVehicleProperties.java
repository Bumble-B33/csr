package net.bumblebee.claysoldiers.soldierproperties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicate;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicates;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SoldierVehicleProperties(SoldierPropertyMapReader properties, ClayPredicate<?> predicate) {
    public static final SoldierVehicleProperties EMPTY = new SoldierVehicleProperties(SoldierPropertyMap.EMPTY_MAP, ClayPredicates.ConstantPredicate.getAlwaysTruePredicate());
    public static final Codec<SoldierVehicleProperties> CODEC = RecordCodecBuilder.create(in -> in.group(
            SoldierPropertyMap.IMMUTABLE_NON_ITEM_CODEC.fieldOf("properties").forGetter(SoldierVehicleProperties::properties),
            ClayPredicate.CODEC.optionalFieldOf("predicate", ClayPredicates.ConstantPredicate.getAlwaysTruePredicate()).forGetter(SoldierVehicleProperties::predicate)
    ).apply(in, SoldierVehicleProperties::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SoldierVehicleProperties> STREAM_CODEC = StreamCodec.composite(
            SoldierPropertyMap.IMMUTABLE_STREAM_CODEC, SoldierVehicleProperties::properties,
            ClayPredicate.STREAM_CODEC, SoldierVehicleProperties::predicate,
            SoldierVehicleProperties::new
    );

    public SoldierPropertyMapReader getProperties() {
        return properties;
    }

    public SoldierVehicleProperties(SoldierPropertyMap properties) {
        this(properties, ClayPredicates.ConstantPredicate.getAlwaysTruePredicate());
    }
}
