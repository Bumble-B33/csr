package net.bumblebee.claysoldiers.blueprint;

import net.bumblebee.claysoldiers.init.ModTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;

@FunctionalInterface
public interface BlueprintStateFilter {
    boolean isIllegal(BlockState state);

    private static <T extends Comparable<T>> boolean isIllegalSate(BlockState state, Property<T> property, T when) {
        return when.equals(state.getOptionalValue(property).orElse(null));
    }

    static BlueprintStateFilter createFilter() {
        return s -> {
            if (isIllegalSate(s, BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER)) return true;
            if (isIllegalSate(s, BlockStateProperties.BED_PART, BedPart.HEAD)) return true;
            if (s.is(ModTags.Blocks.BLUEPRINT_BLACK_LISTED)) return true;

            return s.isAir();
        };
    }
}
