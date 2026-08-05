package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.blueprint.EaselBlockEntity;
import net.bumblebee.claysoldiers.block.cacti.ClayCactusBlockEntity;
import net.bumblebee.claysoldiers.block.chargingpad.SoldierChargingPadBlockEntity;
import net.bumblebee.claysoldiers.block.chipassembler.ChipAssemblerBlockEntity;
import net.bumblebee.claysoldiers.block.hammock.SugarCaneHammockBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;
import java.util.function.Supplier;

public final class ModBlockEntities {
    public static final Supplier<BlockEntityType<HamsterWheelBlockEntity>> HAMSTER_WHEEL_BLOCK_ENTITY =
            ClaySoldiersCommon.PLATFORM.registerBlockEntity("master_wheel_block_entity", HamsterWheelBlockEntity::new, List.of(ModBlocks.HAMSTER_WHEEL_BLOCK));

    public static final Supplier<BlockEntityType<EaselBlockEntity>> EASEL_BLOCK_ENTITY =
            ClaySoldiersCommon.PLATFORM.registerBlockEntity("easel_block_entity", EaselBlockEntity::new, List.of(ModBlocks.EASEL_BLOCK));

    public static final Supplier<BlockEntityType<ChipAssemblerBlockEntity>> CHIP_ASSEMBLER_BLOCK_ENTITY =
            ClaySoldiersCommon.PLATFORM.registerBlockEntity("chip_assembler_block_entity", ChipAssemblerBlockEntity::new, List.of(ModBlocks.CHIP_ASSEMBLER));

    public static final Supplier<BlockEntityType<SugarCaneHammockBlockEntity>> SUGAR_CANE_HAMMOCK_BLOCK_ENTITY =
            ClaySoldiersCommon.PLATFORM.registerBlockEntity("suagr_cane_hammock_block_entity", SugarCaneHammockBlockEntity::new, List.of(ModBlocks.SUGAR_CANE_HAMMOCK));

    public static final Supplier<BlockEntityType<ClayCactusBlockEntity>> CLAY_CACTUS_BLOCK_ENTITY =
            ClaySoldiersCommon.PLATFORM.registerBlockEntity("clay_house", ClayCactusBlockEntity::new, List.of(ModBlocks.CACTUS_HOUSE));

    public static final Supplier<BlockEntityType<SoldierChargingPadBlockEntity>> SOLDIER_CHARGING_PAD =
            ClaySoldiersCommon.PLATFORM.registerBlockEntity("charging_pad", SoldierChargingPadBlockEntity::new, List.of(ModBlocks.SOLDIER_CHARGING_PAD));


    public static void init() {
    }

    private ModBlockEntities() {
    }
}
