package net.bumblebee.claysoldiers.soldierproperties.types;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClaySoldierInventoryQuery;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.UnitProperty;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UnitPropertyType extends SoldierPropertyType<UnitProperty> {
    private static final Codec<UnitProperty> CODEC = Codec.either(
            Codec.unit(UnitProperty.INSTANCE),
            Codec.BOOL
    ).comapFlatMap(UnitPropertyType::getFromEither, Either::left);
    private static final StreamCodec<RegistryFriendlyByteBuf, UnitProperty> STREAM_CODEC = StreamCodec.unit(UnitProperty.INSTANCE);


    public UnitPropertyType() {
        super(CODEC, STREAM_CODEC, UnitProperty.INSTANCE, e -> 1, (u1, u2) -> u1, null);
    }

    @Override
    public List<Component> getDisplayNameWithValue(UnitProperty value, @Nullable ClaySoldierInventoryQuery soldier) {
        return List.of(getDisplayName());
    }

    private static DataResult<UnitProperty> getFromEither(Either<UnitProperty, Boolean> either) {
        if (!either.right().orElse(true)) {
            return DataResult.error(() -> "Cannot have UnitProperty with false");
        }
        return DataResult.success(UnitProperty.INSTANCE);
    }
}
