package net.bumblebee.claysoldiers.util.codec;

import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.soldierproperties.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public abstract class SoldierPropertyMapStreamCodec<A extends SoldierPropertyMapReader> implements StreamCodec<RegistryFriendlyByteBuf, A> {

    public static StreamCodec<RegistryFriendlyByteBuf, SoldierPropertyMap> map() {
        return new SoldierPropertyMapStreamCodec<>() {
            @Override
            protected SoldierPropertyMap create(List<SoldierProperty<?>> entries) {
                return new SoldierPropertyMap(entries);
            }
        };
    }

    public static StreamCodec<RegistryFriendlyByteBuf, SoldierPropertyMapReader> immutable() {
        return new SoldierPropertyMapStreamCodec<>() {
            @Override
            protected SoldierPropertyMapReader create(List<SoldierProperty<?>> entries) {
                return SoldierPropertyMap.of(entries);
            }
        };
    }

    protected abstract A create(List<SoldierProperty<?>> entries);

    @Override
    public A decode(RegistryFriendlyByteBuf buffer) {
        int size = ByteBufCodecs.readCount(buffer, getMaxSize());
        List<SoldierProperty<?>> map = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            SoldierPropertyType<?> type = SoldierPropertyTypes.STREAM_CODEC.decode(buffer);

            SoldierProperty<?> property = decodeFromType(type, buffer);
            map.add(property);
        }
        return create(map);
    }

    private static <T> SoldierProperty<T> decodeFromType(SoldierPropertyType<T> type, RegistryFriendlyByteBuf buffer) {
        return new SoldierProperty<>(type, type.streamDecode(buffer));
    }

    @Override
    public void encode(RegistryFriendlyByteBuf pBuffer, A map) {
        ByteBufCodecs.writeCount(pBuffer, map.size(), getMaxSize());
        map.forEach(soldierProperty -> {
            SoldierPropertyTypes.STREAM_CODEC.encode(pBuffer, soldierProperty.type());
            soldierProperty.streamEncode(pBuffer);
        });
    }

    private int getMaxSize() {
        int size = ModRegistries.SOLDIER_PROPERTY_TYPES_REGISTRY.size();
        return size == 0 ? Integer.MAX_VALUE : size;
    }
}
