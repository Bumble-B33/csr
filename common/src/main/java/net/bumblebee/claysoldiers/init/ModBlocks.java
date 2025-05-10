package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.blueprint.EaselBlock;
import net.bumblebee.claysoldiers.block.blueprint.EscritoireBlock;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlock;
import net.bumblebee.claysoldiers.item.blueprint.BlueprintDependendBlockItem;
import net.bumblebee.claysoldiers.platform.ItemLikeSupplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    public static final ItemLikeSupplier<Block> HAMSTER_WHEEL_BLOCK = ClaySoldiersCommon.PLATFORM.registerBlockWithItem("hamster_wheel",
            HamsterWheelBlock::new, BlockBehaviour.Properties.of()
                    .strength(2)
                    .requiresCorrectToolForDrops().strength(1.5F, 6.0F)
                    .noOcclusion()
    );

    public static final ItemLikeSupplier<Block> EASEL_BLOCK = ClaySoldiersCommon.PLATFORM.registerBlockWithItem("easel",
            EaselBlock::new, BlockBehaviour.Properties.of().noOcclusion().instabreak().noCollission(), BlueprintDependendBlockItem::new
    );

    public static final ItemLikeSupplier<Block> ESCRITOIRE_BLOCK = ClaySoldiersCommon.PLATFORM.registerBlockWithItem("escritoire",
            EscritoireBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.LECTERN), BlueprintDependendBlockItem::new
    );


    public static void init() {
    }
}
