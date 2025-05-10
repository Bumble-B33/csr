package net.bumblebee.claysoldiers.datagen.tags;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModBlocks;
import net.bumblebee.claysoldiers.init.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ClaySoldiersCommon.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(BlockTags.NEEDS_STONE_TOOL).add(ModBlocks.HAMSTER_WHEEL_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.HAMSTER_WHEEL_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.ESCRITOIRE_BLOCK.get());


        tag(ModTags.Blocks.BLUEPRINT_BLACK_LISTED)
                .addTag(BlockTags.AIR)
                .add(Blocks.BARRIER,
                        Blocks.BEDROCK,
                        Blocks.END_PORTAL,
                        Blocks.END_PORTAL_FRAME,
                        Blocks.END_GATEWAY,
                        Blocks.COMMAND_BLOCK,
                        Blocks.REPEATING_COMMAND_BLOCK,
                        Blocks.CHAIN_COMMAND_BLOCK,
                        Blocks.STRUCTURE_BLOCK,
                        Blocks.STRUCTURE_VOID,
                        Blocks.JIGSAW,
                        Blocks.MOVING_PISTON,
                        Blocks.LIGHT);
    }
}
