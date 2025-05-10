package net.bumblebee.claysoldiers.soldierpoi;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunction;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiSource;
import net.bumblebee.claysoldiers.claypoifunction.ClaySoldierInventorySetter;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicate;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClaySoldierInventoryQuery;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;


/**
 * The SoldierPoi class represents a point of interest for a clay soldier which contains a set of effects,
 * a condition determining whether the effects can be performed, and a probability of the point being "broken" or used up.
 */
public class SoldierPoi {
    public static final Codec<SoldierPoi> CODEC = RecordCodecBuilder.create(in -> in.group(
            ClayPoiFunction.CODEC.listOf().fieldOf("effect").forGetter(p -> p.effects),
            ClayPredicate.CODEC.fieldOf("predicate").forGetter(p -> p.canUse),
            CodecUtils.CHANCE_CODEC.optionalFieldOf("break_chance", 0f).forGetter(p -> p.breakChance)
            ).apply(in, SoldierPoi::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SoldierPoi> STREAM_CODEC = StreamCodec.composite(
            ClayPoiFunction.LIST_STREAM_CODEC, p -> p.effects,
            ClayPredicate.STREAM_CODEC, SoldierPoi::getPredicate,
            ByteBufCodecs.FLOAT, SoldierPoi::getBreakChance,
            SoldierPoi::new
    );

    private final List<ClayPoiFunction<?>> effects;
    private final ClayPredicate<?> canUse;
    private final float breakChance;

    public SoldierPoi(List<ClayPoiFunction<?>> effects, ClayPredicate<?> canUse, float breakChance) {
        this.effects = effects;
        this.canUse = canUse;
        this.breakChance = breakChance;
    }
    public SoldierPoi(ClayPoiFunction<?> effect, ClayPredicate<?> canUse, float breakChance) {
        this(List.of(effect), canUse, breakChance);
    }

    public float getBreakChance() {
        return breakChance;
    }
    public boolean canPerformEffect(ClaySoldierInventoryQuery soldier) {
        return canUse.test(soldier);
    }
    public void performEffect(ClaySoldierInventorySetter soldier, ClayPoiSource source) {
        effects.forEach(ef -> ef.accept(soldier, source));
    }
    public List<ClayPoiFunction<?>> getEffects() {
        return effects;
    }
    public ClayPredicate<?> getPredicate() {
        return canUse;
    }

    @Override
    public String toString() {
        return "P[" + breakChance + "]";
    }
}
