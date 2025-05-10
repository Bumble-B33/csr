package net.bumblebee.claysoldiers.soldierproperties.customproperties;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.soldierproperties.combined.ValueCombiner;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialEffectCategory;
import net.bumblebee.claysoldiers.soldierproperties.translation.KeyableTranslatableProperty;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

public enum AttackTypeProperty implements StringRepresentable, KeyableTranslatableProperty {
    NORMAL("normal", true, true, true, AttackTypeProperty::shouldAttackClaySoldier, null),
    PACIFIST("pacifist", false, true, true, (s, t) -> false, ChatFormatting.WHITE),
    AGGRESSIVE("aggressive", true, false, false, AttackTypeProperty::canSeeTarget, ChatFormatting.DARK_RED),
    SUPPORT("support", false, true, true, AttackTypeProperty::healerPredicate, ChatFormatting.GREEN),
    KING("king", true, false, true, AttackTypeProperty::royaltyPredicate, ChatFormatting.GOLD),
    QUEEN("queen", true, false, true, AttackTypeProperty::royaltyPredicate, ChatFormatting.AQUA),
    ZOMBIE("zombie", true, false, false, AttackTypeProperty::shouldAttackClaySoldier, ChatFormatting.DARK_GREEN),
    VAMPIRE("vampire", true, false, false, AttackTypeProperty::shouldAttackClaySoldier, null) {
        @Override
        public Style getStyle() {
            return Style.EMPTY.withColor(0x660707);
        }

        @Override
        public Style getAnimatedStyle(LivingEntity livingEntity) {
            return Style.EMPTY.withColor(0x660707 + (triangleWave(livingEntity.tickCount, 0x30) << 16));
        }

        @Override
        public Component getAnimatedDisplayName(LivingEntity livingEntity) {
            return Component.translatable(translatableKey()).withStyle(getAnimatedStyle(livingEntity));
        }
    },
    BOSS("boss", true, false, false, null, ChatFormatting.WHITE) {
        @Override
        public boolean canAttack(AbstractClaySoldierEntity attacker, LivingEntity target) {
            if (target.getType().is(ModTags.EntityTypes.CLAY_BOSS)) {
                return false;
            }
            return target instanceof Player || target instanceof ClayMobEntity;
        }

        @Override
        public Style getAnimatedStyle(LivingEntity livingEntity) {
            if (livingEntity instanceof ClayMobEntity clayMobEntity) {
                return Style.EMPTY.withColor(clayMobEntity.getClayTeam().getColor(livingEntity, 0));
            }
            return super.getAnimatedStyle(livingEntity);
        }

        @Override
        public Component getAnimatedDisplayName(LivingEntity livingEntity) {
            return Component.translatable(translatableKey()).withStyle(getAnimatedStyle(livingEntity));
        }
    };

    private static AttackTypeProperty[] withOutBoss;
    public static final Codec<AttackTypeProperty> CODEC = StringRepresentable.fromEnum(AttackTypeProperty::withOutBoss);
    public static final StreamCodec<FriendlyByteBuf, AttackTypeProperty> STREAM_CODEC = CodecUtils.createEnumStreamCodec(AttackTypeProperty.class);
    public static final ToIntFunction<AttackTypeProperty> TO_INT = Enum::ordinal;
    public static final ValueCombiner<AttackTypeProperty> COMBINER = (t1, t2) -> {
        if (!t1.compatibleWith(t2)) {
            throw new IllegalArgumentException("Cannot Combine Attack Type %s with %s".formatted(t1, t2));
        }
        return (t1 != NORMAL) ? t1 : t2;
    };

    private final String serializedName;
    private final boolean fightsBack;
    private final boolean canBeRidden;
    private final boolean canBeRevived;
    private final ClaySoldierTargetPredicate targetPredicate;
    private final ChatFormatting chatFormatting;

    AttackTypeProperty(String serializedName, boolean fightsBack, boolean canBeRidden, boolean canBeRevived, ClaySoldierTargetPredicate targetPredicate, ChatFormatting chatFormatting) {
        this.serializedName = serializedName;
        this.fightsBack = fightsBack;
        this.canBeRidden = canBeRidden;
        this.canBeRevived = canBeRevived;
        this.targetPredicate = targetPredicate;
        this.chatFormatting = chatFormatting;
    }


    /**
     * Returns whether this {@code AttackType} fights back.
     */
    public boolean fightsBack() {
        return this.fightsBack;
    }

    public boolean compatibleWith(AttackTypeProperty attackType) {
        return this == attackType || this == NORMAL || attackType == NORMAL;
    }

    /**
     * Returns whether this {@code AttackType} is royalty.
     */
    public boolean isRoyalty() {
        return this == QUEEN || this == KING;
    }

    /**
     * Returns whether a {@code ClaySoldier} with this {@code AttackType} can be ridden.
     */
    public boolean rideable() {
        return canBeRidden;
    }

    /**
     * Returns whether this {@code AttackType} is supportive.
     */
    public boolean isSupportive() {
        return this == SUPPORT;
    }

    public boolean canWork() {
        return this == PACIFIST;
    }

    public boolean canBeRevived() {
        return canBeRevived;
    }

    /**
     * Returns the {@link SpecialEffectCategory} this {@code AttackType} is for.
     */
    public SpecialEffectCategory forType() {
        return isSupportive() ? SpecialEffectCategory.BENEFICIAL : SpecialEffectCategory.HARMFUL;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    /**
     * Returns whether the attacker can attack the target.
     */
    public boolean canAttack(AbstractClaySoldierEntity attacker, LivingEntity target) {
        if (!(target instanceof ClayMobEntity clayMobEntity)) {
            return false;
        }
        return targetPredicate.canAttack(attacker, clayMobEntity);
    }

    private static boolean shouldAttackClaySoldier(AbstractClaySoldierEntity self, ClayMobEntity target) {
        if (!canSeeTarget(self, target)) {
            return false;
        }
        if (target instanceof AbstractClaySoldierEntity claySoldier && claySoldier.getAttackType() == AGGRESSIVE) {
            return true;
        }
        return self.shouldAttackTeamHolder(target);
    }

    private static boolean canSeeTarget(AbstractClaySoldierEntity self, ClayMobEntity target) {
        return !target.isInvisible() || target.isCurrentlyGlowing() || self.canSeeInvis();
    }

    private static boolean healerPredicate(AbstractClaySoldierEntity self, ClayMobEntity target) {
        if (target instanceof AbstractClaySoldierEntity claySoldier) {
            if (claySoldier.getAttackType() == AttackTypeProperty.AGGRESSIVE) {
                return false;
            }
        }
        return self.sameTeamAs(target);
    }

    private static boolean royaltyPredicate(AbstractClaySoldierEntity self, ClayMobEntity target) {
        if (target instanceof AbstractClaySoldierEntity soldierEntity) {
            if (self.getAttackType() == soldierEntity.getAttackType()) {
                return true;
            }
        }
        if (self.getAttackType() == QUEEN) {
            return false;
        }
        return shouldAttackClaySoldier(self, target);
    }

    @Override
    public String translatableKey() {
        return ClaySoldiersCommon.CLAY_SOLDIER_PROPERTY_LANG + "attack_type." + serializedName;
    }

    @Override
    public ChatFormatting getFormat() {
        return chatFormatting;
    }

    @Override
    public @Nullable Component getDisplayName() {
        return KeyableTranslatableProperty.super.getDisplayName();
    }

    public @Nullable Style getAnimatedStyle(LivingEntity livingEntity) {
        return getStyle();
    }

    private static int triangleWave(int tick, int max) {
        int period = 2 * max;
        int mod = tick % period;

        if (mod <= max) {
            return mod; // counting up
        } else {
            return period - mod; // counting down
        }
    }

    private static AttackTypeProperty[] withOutBoss() {
        if (withOutBoss == null) {
            withOutBoss = new AttackTypeProperty[]{
                    NORMAL, PACIFIST, AGGRESSIVE, SUPPORT, KING, QUEEN, ZOMBIE, VAMPIRE
            };
        }

        return withOutBoss;
    }

    @FunctionalInterface
    public interface ClaySoldierTargetPredicate {
        boolean canAttack(AbstractClaySoldierEntity self, ClayMobEntity target);
    }
}
