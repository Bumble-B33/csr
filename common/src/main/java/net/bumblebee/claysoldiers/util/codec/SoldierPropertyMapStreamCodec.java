package net.bumblebee.claysoldiers.util.codec;

import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.soldierproperties.SoldierProperty;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SoldierPropertyMapStreamCodec implements StreamCodec<RegistryFriendlyByteBuf, SoldierPropertyMap> {
    @Override
    public SoldierPropertyMap decode(RegistryFriendlyByteBuf buffer) {
        int size = ByteBufCodecs.readCount(buffer, getMaxSize());
        SoldierPropertyMap map = new SoldierPropertyMap();
        for (int i = 0; i < size; i++) {
            SoldierProperty<?> property = decodeSoldierProperty(buffer);
            map.addPropertyForce(property);
        }
        return map;
    }
    @SuppressWarnings("unchecked")
    private static <T> SoldierProperty<T> decodeSoldierProperty(RegistryFriendlyByteBuf buffer) {
        SoldierPropertyType<T> type = (SoldierPropertyType<T>) SoldierPropertyTypes.STREAM_CODEC.decode(buffer);
        T value = type.streamDecode(buffer);
        return new SoldierProperty<>(type, value);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf pBuffer, SoldierPropertyMap map) {
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
