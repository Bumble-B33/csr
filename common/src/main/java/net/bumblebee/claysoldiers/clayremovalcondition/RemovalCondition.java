package net.bumblebee.claysoldiers.clayremovalcondition;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;

import java.util.function.Function;

public abstract class RemovalCondition {
    public static final String COMPONENT_PREFIX = "clay_removal_condition." + ClaySoldiersCommon.MOD_ID + ".";
    public static final Codec<Float> CHANCE_CODEC = CodecUtils.CHANCE_CODEC;
    private final float chance;
    private final RemovalConditionContext.Type type;

    protected RemovalCondition(float chance, RemovalConditionContext.Type type) {
        this.chance = chance;
        this.type = type;
    }

    public abstract boolean shouldRemove(AbstractClaySoldierEntity soldier, RemovalConditionContext context);

    public abstract Component getDisplayName();

    public float getChance() {
        return chance;
    }
    public RemovalConditionContext.Type getType() {
        return type;
    }

    /**
     * Test if this {@code RemovalCondition} is for the correct Type.
     * @return this {@code RemovalCondition} is for the correct Type.
     */
    protected boolean baseTest(RemovalConditionContext.Type toTest, RandomSource randomSource) {
        return type == toTest && randomSource.nextFloat() <= getChance();
    }

    protected static <T extends RemovalCondition> StreamCodec<ByteBuf, T> createChacneStreamCodec(Function<Float, T> factory) {
        return ByteBufCodecs.FLOAT.map(factory, RemovalCondition::getChance);
    }
}
