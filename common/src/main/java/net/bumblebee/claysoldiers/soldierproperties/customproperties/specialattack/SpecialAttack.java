package net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.soldierproperties.combined.ValueCombiner;
import net.bumblebee.claysoldiers.soldierproperties.translation.ITranslatableProperty;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class SpecialAttack<T extends SpecialAttack<T>> implements ITranslatableProperty {
    public static final Codec<SpecialAttack<?>> CODEC = SpecialAttackSerializer.CODEC.dispatch(SpecialAttack::specialAttack, SpecialAttackSerializer::asMapCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf, SpecialAttack<?>> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SpecialAttack<?> decode(RegistryFriendlyByteBuf byteBuf) {
            return SpecialAttackSerializer.STREAM_CODEC.decode(byteBuf).getStreamCodec().decode(byteBuf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf o, SpecialAttack<?> clayPoiFunction) {
            SpecialAttackSerializer.STREAM_CODEC.encode(o, clayPoiFunction.specialAttack());
            clayPoiFunction.encode(o);
        }
    };
    public static final Codec<List<SpecialAttack<?>>> LIST_CODEC = CodecUtils.getSingleOrListCodec(CODEC);
    public static final ValueCombiner<List<SpecialAttack<?>>> COMBINER = (l1, l2) -> {
        var newList = new ArrayList<>(l1);
        newList.addAll(l2);
        return newList;
    };

    public static final ToIntFunction<List<SpecialAttack<?>>> TO_INT = List::size;
    protected static final String DISPLAY_KEY_PREFIX = ClaySoldiersCommon.CLAY_SOLDIER_PROPERTY_LANG + "special_attack.";

    private final Supplier<SpecialAttackSerializer<T>> serializerGetter;
    private final float bonusDamage;
    private final SpecialAttackType attackType;

    public SpecialAttack(Supplier<SpecialAttackSerializer<T>> serializerGetter, SpecialAttackType attackType, float bonusDamage) {
        this.serializerGetter = serializerGetter;
        this.bonusDamage = bonusDamage;
        this.attackType = attackType;
    }

    public SpecialAttackSerializer<T> specialAttack() {
        return serializerGetter.get();
    }

    /**
     * Gets the flat bonus damage of this attack
     */
    protected float getDamage() {
        return bonusDamage;
    }

    public SpecialAttackType getAttackType() {
        return attackType;
    }

    /**
     * Returns whether this attack is for the given category
     * @param category the category
     */
    public boolean isForCategory(SpecialEffectCategory category) {
        return category.isHarmful();
    }

    /**
     * whether to attack a target or look for a better one.
     * Only used for {@link SpecialAttackType#MELEE MELEE} attacks.
     */
    public boolean shouldAttackTarget(LivingEntity target) {
        return true;
    }

    /**
     * Test if this attack should be performed
     */
    public boolean condition(ClayMobEntity attacker, Entity target) {
        return true;
    }

    /**
     * The effect of this attack
     */
    public void attackEffect(ClayMobEntity attacker, Entity target) {
    }

    public float getBonusDamage(ClayMobEntity attacker, Entity target) {
        return (condition(attacker, target)) ? bonusDamage : 0f;
    }
    public void  performAttackEffect(ClayMobEntity attacker, Entity target) {
        if (condition(attacker, target)) {
            attackEffect(attacker, target);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + getAttackType().getSerializedName() + ")" + bonusDamageToString();
    }
    protected final String bonusDamageToString() {
        return (bonusDamage != 0 ? " Damage: " + bonusDamage : "");
    }

    protected static <P extends SpecialAttack<P>> Codec<P> createDefaultDamageCodec(BiFunction<SpecialAttackType, Float, P> instantiation) {
        return RecordCodecBuilder.create(in -> in.group(
                SpecialAttackType.CODEC.fieldOf("attack_type").forGetter(SpecialAttack::getAttackType),
                Codec.FLOAT.fieldOf("damage").forGetter(SpecialAttack::getDamage)
        ).apply(in, instantiation));
    }
    protected static <P extends SpecialAttack<P>> StreamCodec<RegistryFriendlyByteBuf, P> createDefaultStreamCodec(BiFunction<SpecialAttackType, Float, P> instantiation) {
        return StreamCodec.composite(
                SpecialAttackType.STREAM_CODEC, SpecialAttack::getAttackType,
                ByteBufCodecs.FLOAT, SpecialAttack::getDamage,
                instantiation
        );
    }

    @Override
    public @Nullable Component getDisplayName() {
        return Component.literal(toString());
    }

    private void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        specialAttack().getStreamCodec().encode(registryFriendlyByteBuf, (T) this);
    }
}