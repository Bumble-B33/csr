package net.bumblebee.claysoldiers.block.blueprint;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.menu.escritoire.EscritoireMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EscritoireBlock extends Block {
    public static final String CONTAINER_TITLE = ClaySoldiersCommon.MOD_ID +  ".container.escritoire";

    public EscritoireBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            ClaySoldiersCommon.COMMON_HOOKS.openMenu(player, getMenuProvider(state, level, pos), 0);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) -> new EscritoireMenu(id, inventory, ContainerLevelAccess.create(level, pos)), Component.translatable(CONTAINER_TITLE));
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return ClaySoldiersCommon.COMMON_HOOKS.isBlueprintEnabled(enabledFeatures);
    }
}
