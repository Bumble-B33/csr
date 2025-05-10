package net.bumblebee.claysoldiers.datamap.armor.accessories;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.datamap.armor.accessories.custom.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SoldierAccessorySlot<T extends RenderableAccessory> implements StringRepresentable {

    public static final SoldierAccessorySlot<CapeRenderable> CAPE = new SoldierAccessorySlot<>("cape", CapeRenderable.CODEC, CapeRenderable.STREAM_CODEC.cast());
    public static final SoldierAccessorySlot<SnorkelRenderable> SNORKEL = new SoldierAccessorySlot<>("snorkel", SnorkelRenderable.CODEC, SnorkelRenderable.STREAM_CODEC.cast());
    public static final SoldierAccessorySlot<GliderRenderable> GLIDER = new SoldierAccessorySlot<>("glider", GliderRenderable.CODEC, GliderRenderable.STREAM_CODEC);
    public static final SoldierAccessorySlot<SkullRenderable> HEAD_ITEM = new SoldierAccessorySlot<>("skull", SkullRenderable.CODEC, SkullRenderable.STREAM_CODEC);
    public static final SoldierAccessorySlot<ShieldRenderable> SHIELD = new SoldierAccessorySlot<>("shield", ShieldRenderable.CODEC, ShieldRenderable.STREAM_CODEC.cast());
    public static final SoldierAccessorySlot<StringRenderLayer> STRING = new SoldierAccessorySlot<>("wrapped", StringRenderLayer.CODEC, StringRenderLayer.STREAM_CODEC.cast());

    private static final SoldierAccessorySlot<?>[] SLOTS = new SoldierAccessorySlot<?>[] { CAPE, SNORKEL, GLIDER, HEAD_ITEM, SHIELD, STRING };

    private static final Codec<SoldierAccessorySlot<?>> CODEC = StringRepresentable.fromValues(SoldierAccessorySlot::values);
    public static final Codec<Map<SoldierAccessorySlot<?>, RenderableAccessory>> MAP_CODEC = Codec.dispatchedMap(CODEC, SoldierAccessorySlot::getCodecForAccessory);
    private static final StreamCodec<ByteBuf, SoldierAccessorySlot<?>> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf byteBuf, SoldierAccessorySlot<?> soldierAccessorySlot) {
            byteBuf.writeByte(getSlotIndex(soldierAccessorySlot));
        }

        @Override
        public SoldierAccessorySlot<?> decode(ByteBuf byteBuf) {
            return SLOTS[byteBuf.readByte()];
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, Map<SoldierAccessorySlot<?>, RenderableAccessory>> MAP_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Map<SoldierAccessorySlot<?>, RenderableAccessory> decode(RegistryFriendlyByteBuf byteBuf) {
            int i = ByteBufCodecs.readCount(byteBuf, Integer.MAX_VALUE);
            Map<SoldierAccessorySlot<?>, RenderableAccessory> m = new HashMap<>(Math.min(i, 65536));

            for (int j = 0; j < i; j++) {
                SoldierAccessorySlot<?> key = STREAM_CODEC.decode(byteBuf);
                RenderableAccessory value = key.streamCodec.decode(byteBuf);
                m.put(key, value);
            }

            return m;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf byteBuf, Map<SoldierAccessorySlot<?>, RenderableAccessory> map) {
            ByteBufCodecs.writeCount(byteBuf, map.size(), Integer.MAX_VALUE);
            map.forEach((key, value) -> {
                STREAM_CODEC.encode(byteBuf, key);
                key.encode(byteBuf, value);
            });
        }
    };
    private final String serializedName;
    private final Codec<T> accesoryCodec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;


    private SoldierAccessorySlot(String serializedName, Codec<T> accesoryCodec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        this.serializedName = serializedName;
        this.accesoryCodec = accesoryCodec;
        this.streamCodec = streamCodec;
    }

    public static SoldierAccessorySlot<?>[] values() {
        return SLOTS;
    }
    private static byte getSlotIndex(SoldierAccessorySlot<?> slot) {
        for (byte i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i] == slot) {
                return i;
            }
        }
        throw new IllegalArgumentException("No slot found for " + slot);
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    private Codec<T> getCodecForAccessory() {
        return accesoryCodec;
    }

    @SuppressWarnings("unchecked")
    private void encode(RegistryFriendlyByteBuf byteBuf, RenderableAccessory accessory) {
        try {
            streamCodec.encode(byteBuf, (T) accessory);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("%s cannot be encoded for %s".formatted(accessory, serializedName), e);
        }
    }

    @Override
    public String toString() {
        return "AccessorySlot[%s]".formatted(serializedName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SoldierAccessorySlot<?> that = (SoldierAccessorySlot<?>) o;
        return Objects.equals(serializedName, that.serializedName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(serializedName);
    }
}
