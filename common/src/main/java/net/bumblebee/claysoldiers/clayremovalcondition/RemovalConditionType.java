package net.bumblebee.claysoldiers.clayremovalcondition;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.*;


public class RemovalConditionType<T extends RemovalCondition> implements StringRepresentable {
    private static final List<RemovalConditionType<?>> TYPES = new ArrayList<>();

    public static final RemovalConditionType<OnHurtCondition> ON_HURT = new RemovalConditionType<>("on_hurt", OnHurtCondition.CODEC, OnHurtCondition.STREAM_CODEC.cast());
    public static final RemovalConditionType<OnUseCondition> ON_USE_MELEE = new RemovalConditionType<>("melee_use", OnUseCondition.MELEE_CODEC, OnUseCondition.MELEE_STREAM_CODEC.cast());
    public static final RemovalConditionType<OnUseCondition> ON_USE_RANGED = new RemovalConditionType<>("ranged_use", OnUseCondition.RANGED_CODEC, OnUseCondition.RANGED_STREAM_CODEC.cast());
    public static final RemovalConditionType<OnTeleportCondition> ON_TELEPORT = new RemovalConditionType<>("on_teleport", OnTeleportCondition.CODEC, OnTeleportCondition.STREAM_CODEC);
    public static final RemovalConditionType<OnEscapeCondition> ON_ESCAPE = new RemovalConditionType<>("on_escape", OnEscapeCondition.CODEC, OnEscapeCondition.STREAM_CODEC.cast());
    public static final RemovalConditionType<OnBounceCondition> ON_BOUNCE = new RemovalConditionType<>("on_bounce", OnBounceCondition.CODEC, OnBounceCondition.STREAM_CODEC.cast());

    private static final Codec<RemovalConditionType<?>> CODEC = StringRepresentable.fromValues(RemovalConditionType::values);
    public static final Codec<Map<RemovalConditionType<?>, RemovalCondition>> PAIR_CODEC = Codec.dispatchedMap(CODEC, s -> s.contextCodec);
    private static final StreamCodec<FriendlyByteBuf, RemovalConditionType<?>> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RemovalConditionType<?> decode(FriendlyByteBuf byteBuf) {
            return TYPES.get(byteBuf.readByte());
        }

        @Override
        public void encode(FriendlyByteBuf o, RemovalConditionType<?> removalConditionType) {
            o.writeByte(TYPES.indexOf(removalConditionType));
        }
    };
    public static final StreamCodec<FriendlyByteBuf, Map<RemovalConditionType<?>, RemovalCondition>> PAIR_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Map<RemovalConditionType<?>, RemovalCondition> decode(FriendlyByteBuf byteBuf) {
            int i = ByteBufCodecs.readCount(byteBuf, Integer.MAX_VALUE);
            Map<RemovalConditionType<?>, RemovalCondition> m = new HashMap<>(Math.min(i, 65536));

            for (int j = 0; j < i; j++) {
                RemovalConditionType<?> key = STREAM_CODEC.decode(byteBuf);
                RemovalCondition value = key.streamCodec.decode(byteBuf);
                m.put(key, value);
            }

            return m;
        }

        @Override
        public void encode(FriendlyByteBuf byteBuf, Map<RemovalConditionType<?>, RemovalCondition> map) {
            ByteBufCodecs.writeCount(byteBuf, map.size(), Integer.MAX_VALUE);
            map.forEach((key, value) -> {
                STREAM_CODEC.encode(byteBuf, key);
                key.encode(byteBuf, value);
            });
        }
    };
    private final String serializedName;
    private final Codec<T> contextCodec;
    private final StreamCodec<FriendlyByteBuf, T> streamCodec;

    private RemovalConditionType(String serializedName, Codec<T> contextCodec, StreamCodec<FriendlyByteBuf, T> streamCodec) {
        this.serializedName = serializedName;
        this.contextCodec = contextCodec;
        this.streamCodec = streamCodec;
        TYPES.add(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RemovalConditionType<?> that = (RemovalConditionType<?>) o;
        return Objects.equals(serializedName, that.serializedName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(serializedName);
    }

    @SuppressWarnings("unchecked")
    private void encode(FriendlyByteBuf byteBuf, RemovalCondition condition) {
        try {
            streamCodec.encode(byteBuf, (T) condition);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("%s cannot be encoded for %s".formatted(condition, serializedName), e);
        }
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    private static RemovalConditionType<?>[] values() {
        return TYPES.toArray(RemovalConditionType[]::new);
    }
}
