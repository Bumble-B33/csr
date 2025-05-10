package net.bumblebee.claysoldiers.claysoldierpredicate;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This represents a {@code Codec} serializable Predicate that test a {@code Clay Soldier} on certain condition.
 *
 * @param <T> self
 */
public abstract class ClayPredicate<T extends ClayPredicate<T>> implements Predicate<ClaySoldierInventoryQuery> {
    public static final Codec<ClayPredicate<?>> CODEC = ClayPredicateSerializer.CODEC.dispatch(ClayPredicate::getSerializer, ClayPredicateSerializer::toMapCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClayPredicate<?>> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ClayPredicate<?> decode(RegistryFriendlyByteBuf byteBuf) {
            return ClayPredicateSerializer.STREAM_CODEC.decode(byteBuf).getStreamCodec().decode(byteBuf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf o, ClayPredicate<?> clayPoiFunction) {
            ClayPredicateSerializer.STREAM_CODEC.encode(o, clayPoiFunction.getSerializer());
            clayPoiFunction.encode(o);
        }
    };

    private final Supplier<ClayPredicateSerializer<T>> serializerGetter;

    public ClayPredicateSerializer<T> getSerializer() {
        return this.serializerGetter.get();
    }


    public ClayPredicate(Supplier<ClayPredicateSerializer<T>> serializerGetter) {
        this.serializerGetter = serializerGetter;
    }

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param soldier the {@code ClaySoldier} to test
     * @return {@code true} if the {@code ClaySoldier} fulfills this predicate,
     * otherwise {@code false}
     */
    public abstract boolean test(ClaySoldierInventoryQuery soldier);

    /**
     * Returns the priority of this predicate.
     * @return the priority of this predicate
     */
    public abstract ClayPredicatePriority getPriority();

    /**
     * Returns the display name of this predicate.
     * May be null to indicate this predicate has no display.
     */
    @Nullable
    public abstract Component getDisplayName();

    private void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        getSerializer().getStreamCodec().encode(registryFriendlyByteBuf, (T) this);
    }
}
