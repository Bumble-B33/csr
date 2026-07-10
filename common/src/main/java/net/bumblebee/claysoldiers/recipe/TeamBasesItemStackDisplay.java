package net.bumblebee.claysoldiers.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.stream.Stream;

public record TeamBasesItemStackDisplay(ItemStackTemplate base) implements SlotDisplay {
    private static final StreamCodec<RegistryFriendlyByteBuf, TeamBasesItemStackDisplay> STREAM_CODEC = StreamCodec.composite(
            ItemStackTemplate.STREAM_CODEC, TeamBasesItemStackDisplay::base,
            TeamBasesItemStackDisplay::new
    );
    private static final MapCodec<TeamBasesItemStackDisplay> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            ItemStackTemplate.CODEC.fieldOf("base").forGetter(TeamBasesItemStackDisplay::base)
    ).apply(in, TeamBasesItemStackDisplay::new));
    public static final Type<TeamBasesItemStackDisplay> TYPE = new Type<>(CODEC, STREAM_CODEC);


    @Override
    public <T> Stream<T> resolve(ContextMap contextMap, DisplayContentsFactory<T> displayContentsFactory) {
        if (displayContentsFactory instanceof DisplayContentsFactory.ForStacks<T> stacks) {
            var registries = contextMap.getOptional(SlotDisplayContext.REGISTRIES);
            if (registries == null) {
                return Stream.of(stacks.forStack(base.create()));
            }
            return ClayMobTeamManger.getAll(registries).map(key -> stacks.forStack(base.apply(1, DataComponentPatch.builder().set(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), key.key()).build())));
        }

        return Stream.empty();
    }

    @Override
    public Type<? extends SlotDisplay> type() {
        return TYPE;
    }
}
