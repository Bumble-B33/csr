package net.bumblebee.claysoldiers.soldierproperties.customproperties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.soldierproperties.combined.ValueCombiner;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

public class DamageBlock {
    public static final DamageBlock EMPTY = new DamageBlock(0f, 0f, false) {
        @Override
        public boolean isEmpty() {
            return true;
        }
    };
    public static final ValueCombiner<DamageBlock> COMBINER = CombinedDamageBlock::new;
    public static final ToIntFunction<DamageBlock> TO_INT = (d) -> d.blockAmount > 0 && d.blockChance > 0 ? 1 : 0;
    public static final Codec<DamageBlock> CODEC = RecordCodecBuilder.create(in -> in.group(
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("chance").forGetter(d -> d.blockChance),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("amount").forGetter(d -> d.blockAmount),
            Codec.BOOL.optionalFieldOf("pierceable", true).forGetter(d -> d.pierceable)
    ).apply(in, DamageBlock::new));
    public static final StreamCodec<ByteBuf, DamageBlock> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, d -> d.blockChance,
            ByteBufCodecs.FLOAT, d -> d.blockAmount,
            ByteBufCodecs.BOOL, d -> d.pierceable,
            DamageBlock::new
    );
    public static final BiFunction<String, DamageBlock, List<Component>> DISPLAY_NAME_CREATOR = (key, value) -> {
        var list = new ArrayList<Component>();
        list.add(Component.translatable(key).append(":"));
        value.appendNameToList(list);
        return list;
    };

    private final float blockChance;
    private final float blockAmount;
    private final boolean pierceable;

    public DamageBlock(float blockChance, float blockAmount, boolean pierceable) {
        this.blockChance = blockChance;
        this.blockAmount = blockAmount;
        this.pierceable = pierceable;
    }

    public DamageBlock(float blockChance, float blockAmount) {
        this(blockChance, blockAmount, true);
    }

    public float blocked(RandomSource random, float damage, boolean isPiercing) {
        if (blockChance <= 0 || blockAmount <= 0) {
            return damage;
        }

        if ((!isPiercing || pierceable) && random.nextFloat() > blockChance) {
            float newDamage = damage - blockAmount;
            return newDamage < 0 ? 0 : newDamage;
        }
        return damage;
    }
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String toString() {
        if (blockAmount <= 0 || blockChance <= 0) {
            return "DamageBlock(Empty)";
        }

        return "DamageBlock(Chance: %f, Amount: %f)".formatted(blockChance, blockAmount);
    }

    public String asString() {
        if (blockAmount <= 0 || blockChance <= 0) {
            return "";
        }

        return " (Chance: %.2f Amount: %.2f)".formatted(blockChance, blockAmount);
    }

    protected void appendNameToList(List<Component> tooltip) {
        if (blockAmount <= 0 || blockChance <= 0) {
            return;
        }
        tooltip.add(CommonComponents.space().append(asString()).withStyle(ChatFormatting.DARK_GRAY));
    }

    private static class CombinedDamageBlock extends DamageBlock {
        private final DamageBlock damageBlock1;
        private final DamageBlock damageBlock2;

        public CombinedDamageBlock(DamageBlock damageBlock1, DamageBlock damageBlock2) {
            super(0, 0, false);
            this.damageBlock1 = damageBlock1;
            this.damageBlock2 = damageBlock2;
        }

        @Override
        public float blocked(RandomSource random, float damage, boolean isPiercing) {
            float newDamage = damageBlock1.blocked(random, damage, isPiercing);
            return damageBlock2.blocked(random, newDamage, isPiercing);
        }

        @Override
        public String toString() {
            if (damageBlock1.isEmpty()) {
                return damageBlock2.toString();
            }
            return damageBlock1 + (damageBlock2.isEmpty() ? "" : ", " + damageBlock2);
        }

        @Override
        protected void appendNameToList(List<Component> tooltip) {
            damageBlock1.appendNameToList(tooltip);
            damageBlock2.appendNameToList(tooltip);
        }
    }
}
