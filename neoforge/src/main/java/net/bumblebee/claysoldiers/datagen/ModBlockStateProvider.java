package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ClaySoldiersCommon.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(ModBlocks.HAMSTER_WHEEL_BLOCK.get(), particleOnlyModelFile("hamster_wheel", ResourceLocation.withDefaultNamespace("block/stone")));
        entityAnimateBlock("hamster_wheel");

        simpleBlock(ModBlocks.EASEL_BLOCK.get(), particleOnlyModelFile("easel", ResourceLocation.withDefaultNamespace("block/oak_planks")));
        entityAnimateBlock("easel");

        simpleBlockWithItem(ModBlocks.ESCRITOIRE_BLOCK.get(), models().cubeBottomTop("escritoire",
                ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "block/escritoire_side"),
                ResourceLocation.withDefaultNamespace("block/spruce_planks"),
                ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "block/escritoire_top")
        ));
    }

    public ModelFile particleOnlyModelFile(String name, ResourceLocation particle) {
        return models().getBuilder(name).texture("particle", particle);
    }

    public ModelFile entityAnimateBlock(String name) {
        return itemModels().getBuilder(name).parent(new ModelFile.UncheckedModelFile("builtin/entity"))
                .transforms()
                .transform(ItemDisplayContext.GUI).rotation(30, 225, 0).scale(0.9f).end()
                .transform(ItemDisplayContext.GROUND).scale(0.5f).translation(0, 3,0).end()
                .transform(ItemDisplayContext.HEAD).rotation(0, 180, 0).end()
                .transform(ItemDisplayContext.FIXED).rotation(0, 180, 0).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).scale(0.5f).translation(0, 2.5f,0).rotation(75, 315, 0).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).scale(0.8f).rotation(0, 315, 0).end()
                .end();
    }
}
