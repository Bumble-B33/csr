package net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class SpecialAttackSerializer<P extends SpecialAttack<P>> {
    public static final Codec<SpecialAttackSerializer<?>> CODEC = ModRegistries.SPECIAL_ATTACK_SERIALIZERS_REGISTRY.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, SpecialAttackSerializer<?>> STREAM_CODEC = ByteBufCodecs.registry(ModRegistries.SPECIAL_ATTACK_SERIALIZERS);
    private final MapCodec<P> subCodec;
    private final StreamCodec<RegistryFriendlyByteBuf, P> streamCodec;

    public SpecialAttackSerializer(Codec<P> subCodec) {
        this(subCodec, ByteBufCodecs.fromCodec(subCodec).cast());
    }
    public SpecialAttackSerializer(Codec<P> subCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
        this.subCodec = subCodec.fieldOf("attack_properties");
        this.streamCodec = streamCodec;
    }
    public MapCodec<P> asMapCodec() {
        return subCodec;
    }

    public StreamCodec<RegistryFriendlyByteBuf, P> getStreamCodec() {
        return streamCodec;
    }

}
