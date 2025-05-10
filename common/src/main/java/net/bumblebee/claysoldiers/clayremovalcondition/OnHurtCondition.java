package net.bumblebee.claysoldiers.clayremovalcondition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;

public class OnHurtCondition extends RemovalCondition {
    public static final String HURT_LANG_KEY = COMPONENT_PREFIX + ".hurt";
    private final DamageSourcePredicate predicate;

    public static final Codec<OnHurtCondition> CODEC = RecordCodecBuilder.create(in -> in.group(
            DamageSourcePredicate.CODEC.optionalFieldOf("condition", DamageSourcePredicate.Builder.damageType().build()).forGetter(s -> s.predicate),
            CHANCE_CODEC.optionalFieldOf("chance", 1f).forGetter(RemovalCondition::getChance)
    ).apply(in, OnHurtCondition::new));
    public static final StreamCodec<ByteBuf, OnHurtCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(DamageSourcePredicate.CODEC), c -> c.predicate,
            ByteBufCodecs.FLOAT, RemovalCondition::getChance,
            OnHurtCondition::new
    );

    public OnHurtCondition(DamageSourcePredicate predicate, float chance) {
        super(chance, RemovalConditionContext.Type.HURT);
        this.predicate = predicate;
    }
    public OnHurtCondition(DamageSourcePredicate.Builder predicate, float chance) {
        this(predicate.build(), chance);
    }

    @Override
    public boolean shouldRemove(AbstractClaySoldierEntity soldier, RemovalConditionContext context) {
        if (baseTest(context.getType(), soldier.getRandom())) {
            assert context.getDamageSource() != null;
            return predicate.matches((ServerLevel) soldier.level(), soldier.position(), context.getDamageSource());
        }
        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(HURT_LANG_KEY);
    }
}
