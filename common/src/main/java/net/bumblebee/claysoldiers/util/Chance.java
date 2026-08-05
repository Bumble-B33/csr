package net.bumblebee.claysoldiers.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;

public class Chance {
    public static final Codec<Chance> CODEC = Codec.either(Codec.floatRange(0, 1f), Codec.STRING).comapFlatMap(
            Chance::getFromChanceEither,
            Chance::createChanceEither
    );
    public static final StreamCodec<ByteBuf, Chance> STREAM_CODEC = ByteBufCodecs.FLOAT.map(Chance::of, s -> s.chance);
    public static final Chance ALWAYS = new Chance(1f);
    public static final Chance HALF = new Chance(0.5f);
    public static final Chance NEVER = new Chance(0f);

    private static DataResult<Chance> getFromChanceEither(Either<Float, String> either) {
        if (either.left().isPresent()) {
            return DataResult.success(new Chance(either.left().get()));
        }
        String parsedString = either.right().orElseThrow();
        if (parsedString.equals("always")) {
            return DataResult.success(ALWAYS);
        } else if (parsedString.equals("never")) {
            return DataResult.success(NEVER);
        }
        return DataResult.error(() -> "Cannot parse %s as a chance, needs to be [0.0 - 1.0], 'always' or 'never'".formatted(parsedString));
    }
    private static Either<Float, String> createChanceEither(Chance chance) {
        if (chance.chance >= 1) {
            return Either.right("always");
        } else if (chance.chance <= 0) {
            return Either.right("never");
        }
        return Either.left(chance.chance);
    }

    private final float chance;

    private Chance(float chance) {
        this.chance = chance;
    }

    public static Chance of(float percent) {
        if (percent >= 1f) {
            return ALWAYS;
        }
        if (percent <= 0f) {
            return NEVER;
        }
        if (percent == 0.5f) {
            return HALF;
        }

        return new Chance(percent);
    }

    public boolean isEmpty() {
        return chance <= 0;
    }

    public boolean alwaysTrue() {
        return chance >= 1;
    }

    public float getPercent() {
        return chance;
    }

    public boolean test(RandomSource random) {
        if (chance <= 0) {
            return false;
        } else if (chance >= 1f) {
            return true;
        }

        return random.nextFloat() <= chance;
    }

    /**
     *
     * @param luck positive to increase chance - negative to decrease chance
     */
    public boolean testWithLuck(RandomSource random, float luck) {
        if (chance <= 0) {
            return false;
        } else if (chance >= 1f) {
            return true;
        }

        float newChance = chance * (1f - (luck * 0.1f));

        return random.nextFloat() <= newChance;
    }
}
