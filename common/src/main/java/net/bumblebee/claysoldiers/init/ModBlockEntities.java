package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.blueprint.EaselBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;
import java.util.function.Supplier;

public final class ModBlockEntities {
    public static final Supplier<BlockEntityType<HamsterWheelBlockEntity>> HAMSTER_WHEEL_BLOCK_ENTITY =
            ClaySoldiersCommon.PLATFORM.registerBlockEntity("master_wheel_block_entity", HamsterWheelBlockEntity::new, List.of(ModBlocks.HAMSTER_WHEEL_BLOCK));

    public static final Supplier<BlockEntityType<EaselBlockEntity>> EASEL_BLOCK_ENTITY =
            ClaySoldiersCommon.PLATFORM.registerBlockEntity("easel_block_entity", EaselBlockEntity::new, List.of(ModBlocks.EASEL_BLOCK));

    public static void init() {

    }

    private ModBlockEntities() {
    }
}
