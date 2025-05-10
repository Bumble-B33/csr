package net.bumblebee.claysoldiers.claypoifunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class ClayPoiFunctionSerializer<F extends ClayPoiFunction<F>> {
    public static final Codec<ClayPoiFunctionSerializer<?>> CODEC = ModRegistries.CLAY_POI_FUNCTION_REGISTRY.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, ClayPoiFunctionSerializer<?>> STREAM_CODEC = ByteBufCodecs.registry(ModRegistries.CLAY_POI_FUNCTION_SERIALIZERS);
    private final MapCodec<F> subCodec;
    private final StreamCodec<RegistryFriendlyByteBuf, F> streamCodec;


    public ClayPoiFunctionSerializer(MapCodec<F> subCodec, StreamCodec<RegistryFriendlyByteBuf, F> streamCodec) {
        this.subCodec = subCodec;
        this.streamCodec = streamCodec;

    }

    public MapCodec<F> asMapCodec() {
        return subCodec;
    }

    public StreamCodec<RegistryFriendlyByteBuf, F> getStreamCodec() {
        return streamCodec;
    }
}
