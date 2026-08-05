package net.bumblebee.claysoldiers.clayremovalcondition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.util.Chance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

public class OnTeleportCondition extends RemovalCondition {
    private static final Codec<OnTeleportCondition> CODEC_SMALL = RemovalConditionContext.MovementType.CODEC.xmap(t -> new OnTeleportCondition(t, Chance.ALWAYS), t -> t.teleportationTyp);
    private static final Codec<OnTeleportCondition> CODEC_BIG = RecordCodecBuilder.create(in -> in.group(
            RemovalConditionContext.MovementType.CODEC.fieldOf("teleportation_type").forGetter(t -> t.teleportationTyp),
            CHANCE_CODEC.optionalFieldOf("chance", Chance.ALWAYS).forGetter(RemovalCondition::getChance)
    ).apply(in, OnTeleportCondition::new));
    public static final Codec<OnTeleportCondition> CODEC = Codec.either(CODEC_SMALL, CODEC_BIG).xmap(either -> {
                if (either.left().isPresent()) {
                    return either.left().get();
                }
                if (either.right().isPresent()) {
                    return either.right().get();
                }
                throw new IllegalStateException("No side of Either present");
            },
            teleportCondition -> teleportCondition.getChance().alwaysTrue() ? Either.left(teleportCondition) : Either.right(teleportCondition));
    public static final StreamCodec<FriendlyByteBuf, OnTeleportCondition> STREAM_CODEC = StreamCodec.composite(
            RemovalConditionContext.MovementType.STREAM_CODEC, t -> t.teleportationTyp,
            Chance.STREAM_CODEC, RemovalCondition::getChance,
            OnTeleportCondition::new
    );

    public static final String TELEPORTATION_LANG_KEY = COMPONENT_PREFIX + ".teleportation";

    private final RemovalConditionContext.MovementType teleportationTyp;

    public OnTeleportCondition(RemovalConditionContext.MovementType type, Chance chance) {
        super(chance, RemovalConditionContext.Type.TELEPORT);
        this.teleportationTyp = type;
    }

    @Override
    public boolean shouldRemove(AbstractClaySoldierEntity soldier, RemovalConditionContext context) {
        return baseTest(context.getType(), soldier.getRandom(), soldier.getChanceLuck()) && context.getMovementType() == teleportationTyp;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(TELEPORTATION_LANG_KEY, teleportationTyp.getDisplayName());
    }
}
