package net.bumblebee.claysoldiers.util.codec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.util.EffectHolder;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class CodecUtils {
    public static final Codec<Float> CHANCE_CODEC = Codec.either(Codec.floatRange(0, 1f), Codec.STRING).comapFlatMap(
            CodecUtils::getFromChanceEither,
            CodecUtils::createChanceEither
    );
    private static DataResult<Float> getFromChanceEither(Either<Float, String> either) {
        if (either.left().isPresent()) {
            return DataResult.success(either.left().get());
        }
        String parsedString = either.right().orElseThrow();
        if (parsedString.equals("always")) {
            return DataResult.success(1F);
        } else if (parsedString.equals("never")) {
            return DataResult.success(0F);
        }
        return DataResult.error(() -> "Cannot parse %s as a chance, needs to be [0.0 - 1.0], 'always' or 'never'".formatted(parsedString));
    }
    private static Either<Float, String> createChanceEither(float chance) {
        if (chance >= 1) {
            return Either.right("always");
        } else if (chance <= 0) {
            return Either.right("never");
        }
        return Either.left(chance);
    }

    public static final Codec<Integer> TIME_CODEC = Codec.either(ExtraCodecs.NON_NEGATIVE_INT, Codec.STRING).comapFlatMap(
            CodecUtils::getTimeFromEither, time -> time % 20 == 0 ? Either.right(time / 20 + "s") : Either.left(time)
    );
    public static DataResult<Integer> getTimeFromEither(Either<Integer, String> either) {
        if (either.left().isPresent()) {
            return DataResult.success(either.left().get());
        }
        String[] split = either.right().orElseThrow().split("s");

        if (split.length != 1) {
            return DataResult.error(() -> "Cannot parse %s as a time, needs to be [0 - %d] followed by 's'".formatted(either.right().orElseThrow(), Integer.MAX_VALUE / 20));
        }
        try {
            int parsed = Integer.parseInt(split[0]);
            if (parsed < 0 || parsed >= Integer.MAX_VALUE / 20) {
                return DataResult.error(() -> "Cannot parse %s as a time as seconds, needs to be [0 - %d] followed by 's'".formatted(split[0], Integer.MAX_VALUE / 20));
            }
            return DataResult.success(parsed * 20);
        } catch (NumberFormatException e) {
            return DataResult.error(() -> "Cannot parse %s as a time as seconds, needs to be [0 - %d] followed by 's'".formatted(split[0], Integer.MAX_VALUE / 20));
        }
    }

    public static <T> Codec<List<T>> getSingleOrListCodec(Codec<T> single) {
        return Codec.withAlternative(single.listOf(), single, List::of);
    }

    public static <T, H extends Supplier<T>> Codec<T> withReplacementValues(Codec<T> single, Codec<H> replace, H[] possibleReplacements) {
        return Codec.either(replace, single).xmap(
                CodecUtils::getFromEitherWithReplacement,
                value -> createEitherWithReplacement(value, possibleReplacements)
        );
    }

    private static <T, H extends Supplier<T>> T getFromEitherWithReplacement(Either<H, T> either) {
        if (either.left().isPresent()) {
            return either.left().get().get();
        }
        return either.right().orElseThrow();
    }

    private static <T, H extends Supplier<T>> Either<H, T> createEitherWithReplacement(T value, H[] possibleRedirects) {
        for (H red : possibleRedirects) {
            if (red.get().equals(value)) {
                return Either.left(red);
            }
        }
        return Either.right(value);
    }

    public static <E extends EffectHolder> Products.P3<RecordCodecBuilder.Mu<E>, Holder<MobEffect>, Integer, Integer> addEffectAnd() {
        return addEffectAnd(null);
    }

    public static <E extends EffectHolder> Products.P3<RecordCodecBuilder.Mu<E>, Holder<MobEffect>, Integer, Integer> addEffectAnd(@Nullable String prefix) {
        return new Products.P3<>(
                BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(EffectHolder::effectHolder),
                TIME_CODEC.optionalFieldOf(prefix == null ? "duration" : prefix + "Duration", 0).forGetter(EffectHolder::duration),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf(prefix == null ? "amplifier" : prefix + "Amplifier", 0).forGetter(EffectHolder::amplifier)
        );
    }

    public static <T> MapCodec<Set<T>> singularOrPluralCodecOptional(final Codec<T> codec, final String singularName) {
        return singularOrPluralCodecOptional(codec, singularName, "%ss".formatted(singularName));
    }

    private static <T> MapCodec<Set<T>> singularOrPluralCodecOptional(final Codec<T> codec, final String singularName, final String pluralName) {
        return Codec.mapEither(codec.fieldOf(singularName), setOf(codec).optionalFieldOf(pluralName, Set.of())).xmap(
                either -> either.map(ImmutableSet::of, ImmutableSet::copyOf),
                set -> set.size() == 1 ? Either.left(set.iterator().next()) : Either.right(set));
    }

    private static <T> Codec<Set<T>> setOf(final Codec<T> codec) {
        return Codec.list(codec).xmap(ImmutableSet::copyOf, ImmutableList::copyOf);
    }

    public static <T> Codec<T> byNameCodecWithDefaultModId(Registry<T> registry) {
        return referenceHolderWithLifecycle(registry)
                .flatComapMap(Holder.Reference::value, holder -> safeCastToReference(registry, registry.wrapAsHolder(holder)));
    }

    private static <T> Codec<Holder.Reference<T>> referenceHolderWithLifecycle(Registry<T> registry) {
        Codec<Holder.Reference<T>> codec = CODEC_DEFAULT_MOD_ID
                .comapFlatMap(
                        holder -> registry.getHolder(holder)
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + registry.key() + ": " + holder)),
                        p_325513_ -> p_325513_.key().location()
                );
        return ExtraCodecs.overrideLifecycle(
                codec, p_325514_ -> registry.registrationInfo(p_325514_.key()).map(RegistrationInfo::lifecycle).orElse(Lifecycle.experimental())
        );
    }

    private static <T> DataResult<Holder.Reference<T>> safeCastToReference(Registry<T> registry, Holder<T> holder) {
        return holder instanceof Holder.Reference reference
                ? DataResult.success(reference)
                : DataResult.error(() -> "Unregistered holder in " + registry.key() + ": " + holder);
    }

    private static final Codec<ResourceLocation> CODEC_DEFAULT_MOD_ID = Codec.STRING.comapFlatMap(CodecUtils::read, CodecUtils::fromStringResourceLocation).stable();

    private static DataResult<ResourceLocation> read(String input) {
        try {
            return DataResult.success(parse(input));
        } catch (ResourceLocationException resourcelocationexception) {
            return DataResult.error(() -> "Not a valid resource location: " + input + " " + resourcelocationexception.getMessage());
        }
    }

    /**
     * Parses a {@code ResourceLocation} from the given String, if no namespace is present,
     * {@link ClaySoldiersCommon#MOD_ID MOD_ID} will be used instead.
     * @param input the {@code String} to parse
     * @return the parsed {@code ResourceLocation}
     */
    public static ResourceLocation parse(String input) {
        int i = input.indexOf(':');
        if (i >= 0) {
            String path = input.substring(i + 1);
            if (i != 0) {
                String modId = input.substring(0, i);
                return ResourceLocation.fromNamespaceAndPath(modId, path);
            } else {
                return ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, path);
            }
        } else {
            return ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, input);
        }
    }

    private static String fromStringResourceLocation(ResourceLocation location) {
        if (location.getNamespace().equals(ClaySoldiersCommon.MOD_ID)) {
            return location.getPath();
        }
        return location.toString();
    }

    public static <T extends Enum<T>> StreamCodec<FriendlyByteBuf, T> createEnumStreamCodec(Class<T> enumClass) {
        return new StreamCodec<>() {
            @Override
            public void encode(FriendlyByteBuf o, T t) {
                o.writeEnum(t);
            }

            @Override
            public T decode(FriendlyByteBuf byteBuf) {
                return byteBuf.readEnum(enumClass);
            }
        };
    }
}
