package net.bumblebee.claysoldiers.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.init.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record ChargingParticleOption(int delay, float radius, float height) implements ParticleOptions {
    public static final MapCodec<ChargingParticleOption> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            Codec.INT.fieldOf("delay").forGetter(ChargingParticleOption::delay),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("diameter").forGetter(ChargingParticleOption::radius),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("height").forGetter(ChargingParticleOption::height)

            ).apply(in, ChargingParticleOption::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ChargingParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ChargingParticleOption::delay,
            ByteBufCodecs.FLOAT, ChargingParticleOption::radius,
            ByteBufCodecs.FLOAT, ChargingParticleOption::height,
            ChargingParticleOption::new
    );

    @Override
    public ParticleType<ChargingParticleOption> getType() {
        return ModParticles.CHARGING_PARTICLE.get();
    }

    public static ParticleType<ChargingParticleOption> createParticleType() {
        return new ParticleType<>(false) {
            @Override
            public MapCodec<ChargingParticleOption> codec() {
                return CODEC;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, ChargingParticleOption> streamCodec() {
                return STREAM_CODEC;
            }
        };
    }
}
