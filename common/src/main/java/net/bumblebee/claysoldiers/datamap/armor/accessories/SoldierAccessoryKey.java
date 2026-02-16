package net.bumblebee.claysoldiers.datamap.armor.accessories;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.datamap.armor.accessories.custom.CapeAccessoryData;
import net.bumblebee.claysoldiers.datamap.armor.accessories.custom.GliderAccessoryData;
import net.bumblebee.claysoldiers.datamap.armor.accessories.custom.SkullAccessoryData;
import net.bumblebee.claysoldiers.datamap.armor.accessories.custom.TextureAccessoryData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.HashMap;
import java.util.Map;

public class SoldierAccessoryKey<T extends SoldierAccessoryData> implements StringRepresentable {
    public static final SoldierAccessoryKey<CapeAccessoryData> CAPE = new SoldierAccessoryKey<>("cape", CapeAccessoryData.CODEC, CapeAccessoryData.STREAM_CODEC.cast());
    public static final SoldierAccessoryKey<GliderAccessoryData> GLIDER = new SoldierAccessoryKey<>("glider", GliderAccessoryData.CODEC, GliderAccessoryData.STREAM_CODEC);
    public static final SoldierAccessoryKey<TextureAccessoryData> SHIELD = new SoldierAccessoryKey<>("shield", TextureAccessoryData.CODEC, TextureAccessoryData.STREAM_CODEC.cast());
    public static final SoldierAccessoryKey<SkullAccessoryData> SKULL = new SoldierAccessoryKey<>("skull", SkullAccessoryData.CODEC, SkullAccessoryData.STREAM_CODEC.cast());
    public static final SoldierAccessoryKey<TextureAccessoryData> SNORKEL = new SoldierAccessoryKey<>("snorkel", TextureAccessoryData.CODEC, TextureAccessoryData.STREAM_CODEC.cast());
    public static final SoldierAccessoryKey<TextureAccessoryData> STRING = new SoldierAccessoryKey<>("string", TextureAccessoryData.CODEC, TextureAccessoryData.STREAM_CODEC.cast());

    private static final SoldierAccessoryKey<?>[] SLOTS = new SoldierAccessoryKey<?>[]{ CAPE, GLIDER, SHIELD, SKULL, SNORKEL, STRING };

    private static final Codec<SoldierAccessoryKey<?>> CODEC = StringRepresentable.fromValues(SoldierAccessoryKey::values);
    public static final Codec<Map<SoldierAccessoryKey<?>, SoldierAccessoryData>> MAP_CODEC = Codec.dispatchedMap(CODEC, SoldierAccessoryKey::getCodecForData);
    private static final StreamCodec<ByteBuf, SoldierAccessoryKey<?>> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SoldierAccessoryKey<?> decode(ByteBuf byteBuf) {
            return SLOTS[byteBuf.readByte()];
        }

        @Override
        public void encode(ByteBuf byteBuf, SoldierAccessoryKey<?> soldierAccessoryKey) {
            byteBuf.writeByte(getSlotIndex(soldierAccessoryKey));
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, Map<SoldierAccessoryKey<?>, SoldierAccessoryData>> MAP_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Map<SoldierAccessoryKey<?>, SoldierAccessoryData> decode(RegistryFriendlyByteBuf byteBuf) {
            int i = ByteBufCodecs.readCount(byteBuf, Integer.MAX_VALUE);
            Map<SoldierAccessoryKey<?>, SoldierAccessoryData> m = new HashMap<>(Math.min(i, 65536));

            for (int j = 0; j < i; j++) {
                SoldierAccessoryKey<?> key = STREAM_CODEC.decode(byteBuf);
                SoldierAccessoryData value = key.streamCodec.decode(byteBuf);
                m.put(key, value);
            }

            return m;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf byteBuf, Map<SoldierAccessoryKey<?>, SoldierAccessoryData> map) {
            ByteBufCodecs.writeCount(byteBuf, map.size(), Integer.MAX_VALUE);
            map.forEach((key, value) -> {
                STREAM_CODEC.encode(byteBuf, key);
                key.encode(byteBuf, value);
            });
        }
    };


    private final String serializedName;
    private final Codec<T> dataCodec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    public SoldierAccessoryKey(String serializedName, Codec<T> dataCodec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        this.serializedName = serializedName;
        this.dataCodec = dataCodec;
        this.streamCodec = streamCodec;
    }

    public Codec<T> getCodecForData() {
        return dataCodec;
    }

    @SuppressWarnings("unchecked")
    private void encode(RegistryFriendlyByteBuf byteBuf, SoldierAccessoryData accessory) {
        try {
            streamCodec.encode(byteBuf, (T) accessory);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("%s cannot be encoded for %s".formatted(accessory, serializedName), e);
        }
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static SoldierAccessoryKey<?>[] values() {
        return SLOTS;
    }

    private static byte getSlotIndex(SoldierAccessoryKey<?> slot) {
        for (byte i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i] == slot) {
                return i;
            }
        }
        throw new IllegalArgumentException("No slot found for " + slot);
    }
}
