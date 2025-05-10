package net.bumblebee.claysoldiers.util.codec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import net.bumblebee.claysoldiers.soldierproperties.SoldierProperty;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.util.ErrorHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class SoldierPropertyMapCodec implements Codec<SoldierPropertyMap> {
    private static final Codec<SoldierPropertyType<?>> TYPE_CODEC = SoldierPropertyTypes.CODEC;
    private final Collection<Supplier<? extends SoldierPropertyType<?>>> ignored;

    public SoldierPropertyMapCodec(Collection<Supplier<? extends SoldierPropertyType<?>>> ignored) {
        this.ignored = ignored;
    }

    public SoldierPropertyMapCodec() {
        this(Set.of());
    }

    private <V> Codec<V> getSecond(SoldierPropertyType<V> type) {
        return type.getValueCodec();
    }

    @Override
    public <T> DataResult<Pair<SoldierPropertyMap, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> decode(ops, map)).map(spMap -> Pair.of(spMap, input));
    }

    public <T> DataResult<SoldierPropertyMap> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        List<? extends SoldierPropertyType<?>> toIgnore = ignored.stream().map(Supplier::get).toList();

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
                    var shouldIgnore = toIgnore.contains(p.type());
                    if (shouldIgnore) {
                        ErrorHandler.INSTANCE.error("Parsing a Soldier Property (%s), that should be ignored by the Codec".formatted(p));
                    }
                    return !shouldIgnore;
                })
                .toList();
        SoldierPropertyMap soldierPropertyMap = new SoldierPropertyMap(elements);

        List<String> exceptions = new ArrayList<>();
        soldierPropertyMap.validate(e -> exceptions.add(e.getMessage()));
        if (!exceptions.isEmpty()) {
            return DataResult.error(exceptions::toString);
        }

        final T errors = ops.createMap(failed.build().stream());

        return result.map(unit -> soldierPropertyMap).setPartial(soldierPropertyMap).mapError(e -> e + " missed input: " + errors);
    }

    @Override
    public <T> DataResult<T> encode(SoldierPropertyMap input, DynamicOps<T> ops, T prefix) {
        return encode(input, ops, ops.mapBuilder()).build(prefix);
    }

    private <T> RecordBuilder<T> encode(final SoldierPropertyMap input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
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

    @SuppressWarnings("unchecked")
    private static <T> SoldierProperty<T> createProperty(SoldierPropertyType<T> type, Object value) {
        return new SoldierProperty<>(type, (T) value);
    }
}
