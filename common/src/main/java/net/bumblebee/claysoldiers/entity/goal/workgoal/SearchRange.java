package net.bumblebee.claysoldiers.entity.goal.workgoal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record SearchRange(int horizontalRange, int verticalRange) {
    public static final Codec<SearchRange> CODEC = RecordCodecBuilder.create(in -> in.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("horzizontal_search_range").forGetter(SearchRange::horizontalRange),
            ExtraCodecs.POSITIVE_INT.fieldOf("vertical_search_range").forGetter(SearchRange::verticalRange)
    ).apply(in, SearchRange::new));
    public static final StreamCodec<ByteBuf, SearchRange> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SearchRange::horizontalRange,
            ByteBufCodecs.VAR_INT, SearchRange::verticalRange,
            SearchRange::new
    );



    public SearchRange(int range) {
        this(range, range);
    }
}
