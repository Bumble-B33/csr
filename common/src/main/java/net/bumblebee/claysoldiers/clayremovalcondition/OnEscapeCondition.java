package net.bumblebee.claysoldiers.clayremovalcondition;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.util.Chance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

public class OnEscapeCondition extends RemovalCondition {
    public static final Codec<OnEscapeCondition> CODEC = CHANCE_CODEC.xmap(OnEscapeCondition::new, RemovalCondition::getChance);
    public static final StreamCodec<ByteBuf, OnEscapeCondition> STREAM_CODEC = createChanceStreamCodec(OnEscapeCondition::new);
    public static final String ESCAPE_STRING = COMPONENT_PREFIX + ".evacuate";

    public OnEscapeCondition(Chance chance) {
        super(chance, RemovalConditionContext.Type.FLIGHT);
    }

    public OnEscapeCondition(float chance) {
        this(Chance.of(chance));
    }

    @Override
    public boolean shouldRemove(AbstractClaySoldierEntity soldier, RemovalConditionContext context) {
        return baseTest(context.getType(), soldier.getRandom(), soldier.getChanceLuck()) && context.getMovementType() == RemovalConditionContext.MovementType.TO_SAFETY;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(ESCAPE_STRING);
    }
}
