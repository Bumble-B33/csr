package net.bumblebee.claysoldiers.clayremovalcondition;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

public class OnBounceCondition extends RemovalCondition {
    public static final Codec<OnBounceCondition> CODEC = CHANCE_CODEC.xmap(OnBounceCondition::new, RemovalCondition::getChance);
    public static final StreamCodec<ByteBuf, OnBounceCondition> STREAM_CODEC = createChacneStreamCodec(OnBounceCondition::new);
    public static final String BOUNCE_LANG = COMPONENT_PREFIX + ".bounce";


    public OnBounceCondition(float chance) {
        super(chance, RemovalConditionContext.Type.BOUNCE);
    }

    @Override
    public boolean shouldRemove(AbstractClaySoldierEntity soldier, RemovalConditionContext context) {
        return baseTest(context.getType(), soldier.getRandom());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(BOUNCE_LANG);
    }
}
