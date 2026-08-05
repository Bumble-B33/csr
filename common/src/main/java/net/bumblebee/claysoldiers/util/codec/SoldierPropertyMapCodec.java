package net.bumblebee.claysoldiers.util.codec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.soldierproperties.*;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class SoldierPropertyMapCodec<A extends SoldierPropertyMapReader> implements Codec<A> {
    private static final Codec<SoldierPropertyType<?>> TYPE_CODEC = SoldierPropertyTypes.CODEC;
    private Collection<Supplier<? extends SoldierPropertyType<?>>> ignored;
    private @Nullable Collection<? extends SoldierPropertyType<?>> mappedToIgnore;

    protected SoldierPropertyMapCodec(@NonNull Collection<Supplier<? extends SoldierPropertyType<?>>> ignored) {
        this.ignored = ignored;
    }

    public static Codec<SoldierPropertyMap> map() {
        return map(List.of());
    }

    public static Codec<SoldierPropertyMap> map(Collection<Supplier<? extends SoldierPropertyType<?>>> ignored) {
        return new SoldierPropertyMapCodec<>(ignored) {
            @Override
            protected SoldierPropertyMap create(List<? extends SoldierProperty<?>> entries) {
                return new SoldierPropertyMap(entries);
            }
        };
    }

    public static Codec<SoldierPropertyMapReader> immutable(Collection<Supplier<? extends SoldierPropertyType<?>>> ignored) {
        return new SoldierPropertyMapCodec<>(List.of()) {
            @Override
            protected SoldierPropertyMapReader create(List<? extends SoldierProperty<?>> entries) {
                return SoldierPropertyMap.of(entries);
            }
        };
    }

    private <V> Codec<V> getSecond(SoldierPropertyType<V> type) {
        return type.getValueCodec();
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> decode(ops, map)).map(spMap -> Pair.of(spMap, input));
    }

    private <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        if (mappedToIgnore == null) {
            mappedToIgnore = ignored.stream().map(Supplier::get).toList();
            ignored = null;
        }

        final ImmutableMap.Builder<SoldierPropertyType<?>, Supplier<?>> read = ImmutableMap.builder();
        final ImmutableList.Builder<Pair<T, T>> failed = ImmutableList.builder();

        final DataResult<Unit> result = input.entries().reduce(
                DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
                (r, pair) -> {
                    final DataResult<SoldierPropertyType<?>> k = TYPE_CODEC.parse(ops, pair.getFirst());
                    final DataResult<?> v = getSecond(k.result().orElseThrow()).parse(ops, pair.getSecond());

                    final DataResult<Pair<SoldierPropertyType<?>, ?>> entry = k.apply2stable(Pair::of, v);
                    entry.error().ifPresent(e -> failed.add(pair));

                    return r.apply2stable((u, p) -> {
                        read.put(p.getFirst(), p::getSecond);
                        return u;
                    }, entry);
                },
                (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2)
        );


        final List<? extends SoldierProperty<?>> elements = read.build().entrySet()
                .stream().map(entry -> createProperty(entry.getKey(), entry.getValue().get()))
                .filter(p -> {
                    var shouldIgnore = mappedToIgnore.contains(p.type());
                    if (shouldIgnore) {
                        ClaySoldiersCommon.ERROR_HANDLER.error("Parsing a Soldier Property (%s), that should be ignored by the Codec".formatted(p));
                    }
                    return !shouldIgnore;
                })
                .toList();
        A soldierPropertyMap = create(elements);

        List<String> exceptions = new ArrayList<>();
        validate(soldierPropertyMap, exceptions::add);
        if (!exceptions.isEmpty()) {
            return DataResult.error(exceptions::toString);
        }

        final T errors = ops.createMap(failed.build().stream());

        return result.map(unit -> soldierPropertyMap).setPartial(soldierPropertyMap).mapError(e -> e + " missed input: " + errors);
    }

    protected abstract A create(List<? extends SoldierProperty<?>> entries);

    @Override
    public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
        return encode(input, ops, ops.mapBuilder()).build(prefix);
    }

    private <T> RecordBuilder<T> encode(final SoldierPropertyMapReader input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        for (var entry : input) {
            encodeSingle(entry, ops, prefix);
        }

        return prefix;
    }

    private <T, V> void encodeSingle(final SoldierProperty<V> input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {

        prefix.add(
                TYPE_CODEC.encodeStart(ops, input.type()),
                getSecond(input.type()).encodeStart(ops, input.value()));
    }

    protected void validate(A soldierProperties, Consumer<String> exceptionHandler) {
        SoldierPropertyMap.validate(soldierProperties, exceptionHandler);
    }

    @SuppressWarnings("unchecked")
    private static <T> SoldierProperty<T> createProperty(SoldierPropertyType<T> type, Object value) {
        return new SoldierProperty<>(type, (T) value);
    }
}
