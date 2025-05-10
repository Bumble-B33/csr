package net.bumblebee.claysoldiers.claypoifunction;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * The abstract class representing a codec serializable function that can be executed by {@code Clay Soldier}.
 *
 * @param <T> self
 */
public abstract class ClayPoiFunction<T extends ClayPoiFunction<T>> {
    public static final Codec<ClayPoiFunction<?>> CODEC = ClayPoiFunctionSerializer.CODEC.dispatch(ClayPoiFunction::getSerializer, ClayPoiFunctionSerializer::asMapCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClayPoiFunction<?>> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ClayPoiFunction<?> decode(RegistryFriendlyByteBuf byteBuf) {
            return ClayPoiFunctionSerializer.STREAM_CODEC.decode(byteBuf).getStreamCodec().decode(byteBuf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf o, ClayPoiFunction<?> clayPoiFunction) {
            ClayPoiFunctionSerializer.STREAM_CODEC.encode(o, clayPoiFunction.getSerializer());
            clayPoiFunction.encode(o);
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, List<ClayPoiFunction<?>>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());


    private final Supplier<ClayPoiFunctionSerializer<T>> serializerGetter;

    public ClayPoiFunction(Supplier<ClayPoiFunctionSerializer<T>> serializerGetter) {
        this.serializerGetter = serializerGetter;
    }

    public ClayPoiFunctionSerializer<T> getSerializer() {
        return this.serializerGetter.get();
    }

    /**
     * Executes this poi function.
     *
     * @param soldier the soldier executing this effect
     * @param source  the source of this effect
     */
    public abstract void accept(ClaySoldierInventorySetter soldier, ClayPoiSource source);

    /**
     * Returns the display name of this Poi Function.
     * Returns {@code null} to indicate the nam should not be displayed.
     */
    @Nullable
    protected Component getDisplayName() {
        return null;
    }

    /**
     * Returns the dynamic display name of this Poi Function.
     * Returns {@code null} to indicate the nam should not be displayed.
     *
     * @param player viewing this name
     */
    public List<Component> getDisplayNameDynamic(Player player) {
        Component displayName = getDisplayName();
        return displayName == null ? List.of() : List.of(displayName);
    }

    private void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        getSerializer().getStreamCodec().encode(registryFriendlyByteBuf, (T) this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
