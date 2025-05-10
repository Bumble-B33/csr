package net.bumblebee.claysoldiers.claysoldierpredicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public final class ClayPredicateSerializer<P extends ClayPredicate<P>> {
    public static final Codec<ClayPredicateSerializer<?>> CODEC = ResourceLocation.CODEC.xmap(ModRegistries.CLAY_SOLDIER_PREDICATE_REGISTRY::get, ClayPredicateSerializer::getId);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClayPredicateSerializer<?>> STREAM_CODEC = ByteBufCodecs.registry(ModRegistries.CLAY_PREDICATE_SERIALIZERS);

    private final Supplier<MapCodec<P>> subCodec;
    private final StreamCodec<RegistryFriendlyByteBuf, P> streamCodec;

    public ClayPredicateSerializer(Codec<P> subCodec) {
        this(subCodec, ByteBufCodecs.fromCodec(subCodec).cast());
    }
    public ClayPredicateSerializer(Codec<P> suCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
        this.subCodec = () -> suCodec.fieldOf("test");
        this.streamCodec = streamCodec;
    }

    public ResourceLocation getId() {
        return ModRegistries.CLAY_SOLDIER_PREDICATE_REGISTRY.getKey(this);
    }

    public MapCodec<P> toMapCodec() {
        return subCodec.get();
    }

    public StreamCodec<RegistryFriendlyByteBuf, P> getStreamCodec() {
        return streamCodec;
    }
}
