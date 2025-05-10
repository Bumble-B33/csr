package net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.soldier.ZombieClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModEffects;
import net.bumblebee.claysoldiers.soldierproperties.combined.ValueCombiner;
import net.bumblebee.claysoldiers.util.EffectHolder;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class SpecialAttacks {
    private static final Supplier<SpecialAttackSerializer<Thorns>> THORNS_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerSpecialAttackSerializer("thorns", () -> new SpecialAttackSerializer<>(Thorns.CODEC, Thorns.STREAM_CODEC));
    private static final Supplier<SpecialAttackSerializer<SneakAttack>> SNEAK_ATTACK_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerSpecialAttackSerializer("sneak_attack", () -> new SpecialAttackSerializer<>(SneakAttack.CODEC, SneakAttack.STREAM_CODEC));
    private static final Supplier<SpecialAttackSerializer<LightningAttack>> LIGHTNING_ATTACK_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerSpecialAttackSerializer("lightning_attack", () -> new SpecialAttackSerializer<>(LightningAttack.CODEC, LightningAttack.STREAM_CODEC));
    private static final Supplier<SpecialAttackSerializer<EffectAttack>> EFFECT_ATTACK_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerSpecialAttackSerializer("effect", () -> new SpecialAttackSerializer<>(EffectAttack.CODEC, EffectAttack.STREAM_CODEC));
    private static final Supplier<SpecialAttackSerializer<CritAttack>> CRIT_ATTACK_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerSpecialAttackSerializer("critical_hit", () -> new SpecialAttackSerializer<>(CritAttack.CODEC, CritAttack.STREAM_CODEC));
    private static final Supplier<SpecialAttackSerializer<Smite>> SMITE_ATTACK_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerSpecialAttackSerializer("smite", () -> new SpecialAttackSerializer<>(Smite.CODEC, Smite.STREAM_CODEC));
    private static final Supplier<SpecialAttackSerializer<Ignite>> IGNITE_ATTACK_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerSpecialAttackSerializer("ignite", () -> new SpecialAttackSerializer<>(Ignite.CODEC, Ignite.STREAM_CODEC));


    public static class Thorns extends SpecialAttack<Thorns> {
        public static final Codec<Thorns> CODEC = createDefaultDamageCodec(Thorns::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, Thorns> STREAM_CODEC = createDefaultStreamCodec(Thorns::new);
        public static final String DISPLAY_NAME_KEY = DISPLAY_KEY_PREFIX + "thorns";

        public Thorns(SpecialAttackType attackType, float reflectDamage) {
            super(THORNS_SERIALIZER, attackType, reflectDamage);
        }

        @Override
        public boolean condition(ClayMobEntity attacker, Entity target) {
            return attacker.getRandom().nextFloat() < 0.75f;
        }

        @Override
        public @Nullable Component getDisplayName() {
            return Component.translatable(DISPLAY_NAME_KEY).withStyle(ChatFormatting.DARK_GREEN);
        }
    }
    public static class SneakAttack extends SpecialAttack<SneakAttack> {
        public static final Codec<SneakAttack> CODEC = createDefaultDamageCodec(SneakAttack::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, SneakAttack> STREAM_CODEC = createDefaultStreamCodec(SneakAttack::new);

        public static final String DISPLAY_NAME_KEY = DISPLAY_KEY_PREFIX + "sneak_attack";

        public SneakAttack(SpecialAttackType attackType, float bonusDamage) {
            super(SNEAK_ATTACK_SERIALIZER, attackType, bonusDamage);
        }

        @Override
        public void attackEffect(ClayMobEntity attacker, Entity target) {
            if (attacker.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.1), target.getZ(), 1, 0.1, 0.0, 0.1, 0.2);
            }
        }

        @Override
        public boolean condition(ClayMobEntity attacker, Entity target) {
            return attacker.isInvisible();
        }

        @Override
        public @Nullable Component getDisplayName() {
            return Component.translatable(DISPLAY_NAME_KEY);
        }
    }
    public static class LightningAttack extends SpecialAttack<LightningAttack> {
        public static final Codec<LightningAttack> CODEC = createDefaultDamageCodec(LightningAttack::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, LightningAttack> STREAM_CODEC = createDefaultStreamCodec(LightningAttack::new);

        public static final String DISPLAY_NAME_KEY = DISPLAY_KEY_PREFIX + "lightning";

        public LightningAttack(SpecialAttackType attackType, float bonusDamage)  {
            super(LIGHTNING_ATTACK_SERIALIZER, attackType, bonusDamage);
        }
        @Override
        public void attackEffect(ClayMobEntity attacker, Entity target) {
            if (attacker.level() instanceof ServerLevel serverLevel) {
                LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (lightningbolt != null) {
                    lightningbolt.moveTo(Vec3.atBottomCenterOf(target.blockPosition()));
                    lightningbolt.setVisualOnly(true);
                    serverLevel.addFreshEntity(lightningbolt);
                    attacker.playSound(SoundEvents.TRIDENT_THUNDER.value(), 0.5f, 1f);
                }
            }
        }

        @Override
        public @Nullable Component getDisplayName() {
            return Component.translatable(DISPLAY_NAME_KEY).withStyle(ChatFormatting.WHITE);
        }
    }
    public static class EffectAttack extends SpecialAttack<EffectAttack> implements EffectHolder {
        public static final Codec<EffectAttack> CODEC = RecordCodecBuilder.create(in -> in.group(
                SpecialAttackType.CODEC.fieldOf("attack_type").forGetter(EffectAttack::getAttackType),
                Codec.FLOAT.optionalFieldOf("damage", 0f).forGetter(EffectAttack::getDamage)
        ).and(CodecUtils.addEffectAnd()).apply(in, EffectAttack::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, EffectAttack> STREAM_CODEC = StreamCodec.composite(
                SpecialAttackType.STREAM_CODEC, EffectAttack::getAttackType,
                ByteBufCodecs.FLOAT, EffectAttack::getDamage,
                MobEffect.STREAM_CODEC, EffectAttack::effectHolder,
                ByteBufCodecs.VAR_INT, EffectAttack::duration,
                ByteBufCodecs.VAR_INT, EffectAttack::amplifier,
                EffectAttack::new
        );
        public static final String DISPLAY_NAME_KEY = DISPLAY_KEY_PREFIX + "effect";

        private final Holder<MobEffect> effect;
        private final int duration;
        private final int amplifier;

        /**
         * Creates a new Special Attack that applies an effect
         * @param effect the effect to apply
         * @param duration in ticks
         * @param amplifier the amplifier
         * @param specialAttackType when to apply
         * @param bonusDamage the bonus damage of the attack
         */
        public EffectAttack(SpecialAttackType specialAttackType, float bonusDamage, Holder<MobEffect> effect, int duration, int amplifier) {
            super(EFFECT_ATTACK_SERIALIZER, specialAttackType, bonusDamage);
            this.effect = effect;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        @Override
        public void attackEffect(ClayMobEntity attacker, Entity target) {
            if (target instanceof LivingEntity livingTarget) {
                livingTarget.addEffect(new MobEffectInstance(effect, duration, amplifier, false, true, true));
            }
        }

        @Override
        public boolean isForCategory(SpecialEffectCategory category) {
            return switch (effect.value().getCategory()) {
                case NEUTRAL -> true;
                case BENEFICIAL -> category.isSupportive();
                case HARMFUL -> category.isHarmful();
            };
        }

        @Override
        public boolean shouldAttackTarget(LivingEntity target) {
            return !target.hasEffect(effect);
        }

        @Override
        public Holder<MobEffect> effectHolder() {
            return effect;
        }

        @Override
        public int duration() {
            return duration;
        }

        @Override
        public int amplifier() {
            return amplifier;
        }

        @Override
        public String toString() {
            return super.toString() + " " + effectHolderToString();
        }
        @Override
        public @Nullable Component getDisplayName() {
            return Component.translatable(DISPLAY_NAME_KEY).append(" ").append(effect().getDisplayName()).withColor(effect().getColor());
        }
    }
    public static class CritAttack extends SpecialAttack<CritAttack> {
        public static final Codec<CritAttack> CODEC = RecordCodecBuilder.create(in -> in.group(
                SpecialAttackType.CODEC.fieldOf("attack_type").forGetter(CritAttack::getAttackType),
                Codec.FLOAT.optionalFieldOf("damage", 0f).forGetter(CritAttack::getDamage),
                ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("chance", 0.5f).forGetter(c -> c.chance)
        ).apply(in, CritAttack::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, CritAttack> STREAM_CODEC = StreamCodec.composite(
                SpecialAttackType.STREAM_CODEC, CritAttack::getAttackType,
                ByteBufCodecs.FLOAT, CritAttack::getDamage,
                ByteBufCodecs.FLOAT, c -> c.chance,
                CritAttack::new
        );
        public static final String DISPLAY_NAME_KEY = DISPLAY_KEY_PREFIX + "crit";

        private final float chance;

        public CritAttack(SpecialAttackType attackType, float bonusDamage, float chance) {
            super(CRIT_ATTACK_SERIALIZER, attackType, bonusDamage);
            this.chance = chance;
        }

        @Override
        public boolean condition(ClayMobEntity attacker, Entity target) {
            return attacker.getRandom().nextFloat() >= chance;
        }

        @Override
        public @Nullable Component getDisplayName() {
            return Component.translatable(DISPLAY_NAME_KEY);
        }
    }
    public static class Smite extends SpecialAttack<Smite> {
        public static final Codec<Smite> CODEC = SpecialAttack.createDefaultDamageCodec(Smite::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, Smite> STREAM_CODEC = createDefaultStreamCodec(Smite::new);

        public static final String DISPLAY_NAME_KEY = DISPLAY_KEY_PREFIX + "smite";

        public Smite(SpecialAttackType attackType, float bonusDamage) {
            super(SMITE_ATTACK_SERIALIZER, attackType, bonusDamage);
        }

        @Override
        public boolean condition(ClayMobEntity attacker, Entity target) {
            return target.getType().is(EntityTypeTags.UNDEAD);
        }

        @Override
        public void attackEffect(ClayMobEntity attacker, Entity target) {
            if (target instanceof ZombieClaySoldierEntity zombie) {
                if (zombie.previousTeamSameAs(attacker)) {
                    zombie.cureZombieSoldier();
                }
            }
            if (target instanceof LivingEntity livingEntity) {
                livingEntity.removeEffect(ModEffects.VAMPIRE_CONVERSION);
            }
        }

        @Override
        public boolean shouldAttackTarget(LivingEntity target) {
            return target.getType().is(EntityTypeTags.UNDEAD);
        }

        @Override
        public @Nullable Component getDisplayName() {
            return Component.translatable(DISPLAY_NAME_KEY).withStyle(ChatFormatting.YELLOW);
        }
    }
    public static class Ignite extends SpecialAttack<Ignite> implements ValueCombiner<Ignite> {
        public static final Codec<Ignite> CODEC = RecordCodecBuilder.create(in -> in.group(
                SpecialAttackType.CODEC.fieldOf("attack_type").forGetter(Ignite::getAttackType),
                Codec.FLOAT.optionalFieldOf("damage", 0f).forGetter(Ignite::getDamage),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("duration").forGetter(c -> c.duration)
        ).apply(in, Ignite::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Ignite> STREAM_CODEC = StreamCodec.composite(
                SpecialAttackType.STREAM_CODEC, Ignite::getAttackType,
                ByteBufCodecs.FLOAT, Ignite::getDamage,
                ByteBufCodecs.VAR_INT, c -> c.duration,
                Ignite::new
        );

        public static final String DISPLAY_NAME_KEY = DISPLAY_KEY_PREFIX + "ignite";

        private final int duration;

        public Ignite(SpecialAttackType attackType, float bonusDamage, int duration) {
            super(IGNITE_ATTACK_SERIALIZER, attackType, bonusDamage);
            this.duration = duration;
        }


        @Override
        public void attackEffect(ClayMobEntity attacker, Entity target) {
            target.igniteForTicks(duration);
        }


        @Override
        public @Nullable Component getDisplayName() {
            return Component.translatable(DISPLAY_NAME_KEY, duration / 20f).withStyle(ChatFormatting.GOLD);
        }

        @Override
        public Ignite combine(Ignite first, Ignite second) {
            return new Ignite(first.getAttackType(), first.getDamage() + second.getDamage(), first.duration + second.duration);
        }
    }


    public static void init() {
    }
}
