package net.bumblebee.claysoldiers.clayremovalcondition;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

public class OnUseCondition extends RemovalCondition {
    public static final Codec<OnUseCondition> MELEE_CODEC = createForType(RemovalConditionContext.Type.MELEE_ATTACK);
    public static final Codec<OnUseCondition> RANGED_CODEC = createForType(RemovalConditionContext.Type.RANGED_ATTACK);
    public static final StreamCodec<ByteBuf, OnUseCondition> MELEE_STREAM_CODEC = createChacneStreamCodec(OnUseCondition::melee);
    public static final StreamCodec<ByteBuf, OnUseCondition> RANGED_STREAM_CODEC = createChacneStreamCodec(OnUseCondition::ranged);
    public static final String MELEE_LANG_KEY = COMPONENT_PREFIX + ".on_use.melee";
    public static final String RANGED_LANG_KEY = COMPONENT_PREFIX + ".on_use.ranged";
    public static final String ERROR_LANG_KEY = COMPONENT_PREFIX + ".on_use.error";


    public static OnUseCondition melee(float chance) {
        return new OnUseCondition(RemovalConditionContext.Type.MELEE_ATTACK, chance);
    }
    public static OnUseCondition ranged(float chance) {
        return new OnUseCondition(RemovalConditionContext.Type.RANGED_ATTACK, chance);
    }

    private OnUseCondition(RemovalConditionContext.Type type, float chance) {
        super(chance, type);
    }

    @Override
    public boolean shouldRemove(AbstractClaySoldierEntity soldier, RemovalConditionContext context) {
        return baseTest(context.getType(), soldier.getRandom());
    }

    @Override
    public Component getDisplayName() {
        return switch (getType()) {
            case RANGED_ATTACK -> Component.translatable(RANGED_LANG_KEY);
            case MELEE_ATTACK -> Component.translatable(MELEE_LANG_KEY);
            default -> Component.translatable(ERROR_LANG_KEY, getType().getSerializedName()).withStyle(ChatFormatting.RED);
        };
    }

    private static Codec<OnUseCondition> createForType(RemovalConditionContext.Type type) {
        return CHANCE_CODEC.xmap(chance -> new OnUseCondition(type, chance), RemovalCondition::getChance);
    }
}
