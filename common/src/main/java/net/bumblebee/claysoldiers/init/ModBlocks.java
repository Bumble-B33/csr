package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.blueprint.EaselBlock;
import net.bumblebee.claysoldiers.block.blueprint.EscritoireBlock;
import net.bumblebee.claysoldiers.block.cacti.ClayCactusBlock;
import net.bumblebee.claysoldiers.block.chipassembler.ChipAssemblerBlock;
import net.bumblebee.claysoldiers.block.hammock.SugarCaneHammockBlock;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlock;
import net.bumblebee.claysoldiers.platform.ItemLikeSupplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

public class ModBlocks {
    public static final ItemLikeSupplier<Block> HAMSTER_WHEEL_BLOCK = ClaySoldiersCommon.PLATFORM.registerBlockWithItem("hamster_wheel",
            HamsterWheelBlock::new, BlockBehaviour.Properties.of()
                    .strength(2)
                    .requiresCorrectToolForDrops().strength(1.5F, 6.0F)
                    .noOcclusion()
    );

    public static final ItemLikeSupplier<Block> EASEL_BLOCK = ClaySoldiersCommon.PLATFORM.registerBlockWithItem("easel",
            EaselBlock::new, BlockBehaviour.Properties.of().noOcclusion().instabreak().noCollision()
    );

    public static final ItemLikeSupplier<Block> ESCRITOIRE_BLOCK = ClaySoldiersCommon.PLATFORM.registerBlockWithItem("escritoire",
            EscritoireBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.LECTERN)
    );

    public static final ItemLikeSupplier<Block> CHIP_ASSEMBLER = ClaySoldiersCommon.PLATFORM.registerBlockWithItem("chip_assembler",
            ChipAssemblerBlock::new, BlockBehaviour.Properties.of()
                    .strength(2)
                    .requiresCorrectToolForDrops().strength(1.5F, 6.0F)
                    .noOcclusion()
    );

    public static final Supplier<SugarCaneHammockBlock> SUGAR_CANE_HAMMOCK = ClaySoldiersCommon.PLATFORM.registerBlockWithoutItem("sugar_cane_hammock",
            SugarCaneHammockBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)
    );

    public static final ItemLikeSupplier<ClayCactusBlock> CACTUS_HOUSE = ClaySoldiersCommon.PLATFORM.registerBlockWithItem("cactus_house",
            ClayCactusBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .randomTicks()
                    .strength(0.4F)
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.DESTROY)

    );

    public static void init() {
    }
}
