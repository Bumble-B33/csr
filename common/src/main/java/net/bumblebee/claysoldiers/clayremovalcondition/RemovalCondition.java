package net.bumblebee.claysoldiers.clayremovalcondition;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.util.Chance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;

import java.util.function.Function;

public abstract class RemovalCondition {
    public static final String COMPONENT_PREFIX = "clay_removal_condition." + ClaySoldiersCommon.MOD_ID + ".";
    public static final Codec<Chance> CHANCE_CODEC = Chance.CODEC;
    private final Chance chance;
    private final RemovalConditionContext.Type type;

    protected RemovalCondition(Chance chance, RemovalConditionContext.Type type) {
        this.chance = chance;
        this.type = type;
    }

    protected RemovalCondition(float chance, RemovalConditionContext.Type type) {
        this.chance = Chance.of(chance);
        this.type = type;
    }

    public abstract boolean shouldRemove(AbstractClaySoldierEntity soldier, RemovalConditionContext context);

    public abstract Component getDisplayName();

    public Chance getChance() {
        return chance;
    }
    public RemovalConditionContext.Type getType() {
        return type;
    }

    /**
     * Test if this {@code RemovalCondition} is for the correct Type.
     * @return this {@code RemovalCondition} is for the correct Type.
     */
    protected boolean baseTest(RemovalConditionContext.Type toTest, RandomSource random, float luck) {
        return type == toTest && chance.testWithLuck(random, luck * -1f);
    }

    protected static <T extends RemovalCondition> StreamCodec<ByteBuf, T> createChanceStreamCodec(Function<Chance, T> factory) {
        return Chance.STREAM_CODEC.map(factory, RemovalCondition::getChance);
    }
}
