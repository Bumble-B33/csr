package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.ClaySoldiersClient;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.SpecialItemRenderers;
import net.bumblebee.claysoldiers.init.ModBlocks;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.ArrayList;
import java.util.List;

public class ModModelProvider extends ModelProvider {
    private static final ModelTemplate SPECIAL_BLOCK = ExtendedModelTemplateBuilder.builder()
            .parent(ResourceLocation.withDefaultNamespace("block/block"))
            /*.transform(ItemDisplayContext.GUI, t -> t.scale(0.9f).rotation(30, 225, 0))
            .transform(ItemDisplayContext.HEAD, t -> t.scale(0.5f).translation(0, 3, 0))
            .transform(ItemDisplayContext.FIXED, t -> t.rotation(0, 180, 0))
            .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.scale(0.5f).translation(0, 2.5f, 0).rotation(75, 315, 0))
            .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, t -> t.scale(0.8f).rotation(0, 315, 0))*/

            .build();

    public static ModelTemplate CLAY_STAFF_MODEL = ExtendedModelTemplateBuilder.builder()
            .parent(ResourceLocation.withDefaultNamespace("item/generated"))
            .guiLight(UnbakedModel.GuiLight.FRONT)
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(0, 30, 0).translation(11, 17, 4.5f))
            .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, t -> t.rotation(0, -30, 0).translation(11, 17, 4.5f))
            .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, t -> t.rotation(0, -90, 25).translation(-3, 17, 1))
            .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, t -> t.rotation(0, 90, 25).translation(13, 17, 1))
            .transform(ItemDisplayContext.GUI, t -> t.rotation(15, -25, -5).translation(2, 6, 0f))
            .transform(ItemDisplayContext.FIXED, t -> t.rotation(0, 180, 0).translation(-2, 4, 0.5f))
            .transform(ItemDisplayContext.GROUND, t -> t.rotation(0, 0, 0).translation(4, 16, 0.75f))
            .build()
            ;

    public ModModelProvider(PackOutput output) {
        super(output, ClaySoldiersCommon.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(ModItems.SHEAR_BLADE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.SHARPENED_STICK.get(), ModelTemplates.FLAT_HANDHELD_ITEM);

        itemModels.generateFlatItem(ModItems.BRICKED_CLAY_SOLDIER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.CLAY_COOKIE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.CLAY_DISRUPTOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.TERRACOTTA_DISRUPTOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.CLAY_GOGGLES.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SLIME_BOOTS.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.TEST_ITEM.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.CAKE_HORSE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.CAKE_PEGASUS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SNOW_HORSE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SNOW_PEGASUS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.MYCELIUM_HORSE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.MYCELIUM_PEGASUS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.GRASS_HORSE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.GRASS_PEGASUS.get(), ModelTemplates.FLAT_ITEM);

        itemModels.itemModelOutput.accept(
                ModItems.CLAY_STAFF.get(),
                ItemModelGenerators.createFlatModelDispatch(
                        ItemModelUtils.plainModel(itemModels.createFlatItemModel(ModItems.CLAY_STAFF.get(), ModelTemplates.FLAT_ITEM)),
                        new SpecialModelWrapper.Unbaked(
                                ModelLocationUtils.getModelLocation(ModItems.CLAY_STAFF.get(), "_in_hand"),
                                new SpecialItemRenderers.ClayStaffSpecialRenderer.Unbaked()
                        )
                )

        );
        CLAY_STAFF_MODEL.create(ModelLocationUtils.getModelLocation(ModItems.CLAY_STAFF.get(), "_in_hand"), TextureMapping.particle(ModelLocationUtils.getModelLocation(ModItems.CLAY_STAFF.get())), itemModels.modelOutput);


        TextureMapping texturemapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.SPRUCE_PLANKS))
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.SPRUCE_PLANKS))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(ModBlocks.ESCRITOIRE_BLOCK.get(), "_top"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(ModBlocks.ESCRITOIRE_BLOCK.get(), "_side"));

        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ModBlocks.ESCRITOIRE_BLOCK.get(), ModelTemplates.CUBE_BOTTOM_TOP.create(ModBlocks.ESCRITOIRE_BLOCK.get(), texturemapping, blockModels.modelOutput)));

        blockModels.createParticleOnlyBlock(ModBlocks.HAMSTER_WHEEL_BLOCK.get(), Blocks.COPPER_BLOCK);
        blockModels.createParticleOnlyBlock(ModBlocks.EASEL_BLOCK.get(), Blocks.OAK_PLANKS);

        blockModels.generateSimpleSpecialItemModel(ModBlocks.HAMSTER_WHEEL_BLOCK.get(), new SpecialItemRenderers.HamsterWheelSpecialRenderer.Unbaked());
        blockModels.generateSimpleSpecialItemModel(ModBlocks.EASEL_BLOCK.get(), new SpecialItemRenderers.EaselBlockSpecialRenderer.Unbaked());

        SPECIAL_BLOCK.create(ModBlocks.HAMSTER_WHEEL_BLOCK.asItem(), TextureMapping.particle(Blocks.COPPER_BLOCK), itemModels.modelOutput);
        SPECIAL_BLOCK.create(ModBlocks.EASEL_BLOCK.asItem(), TextureMapping.particle(Blocks.OAK_PLANKS), itemModels.modelOutput);

        var inHand = ItemModelUtils.tintedModel(
                ModelTemplates.FLAT_ITEM.create(ModItems.CLAY_SOLDIER.get(), TextureMapping.layer0(ModItems.CLAY_SOLDIER.get()), itemModels.modelOutput),
                ClaySoldiersClient.ClaySoldierItemTintSource.INSTANCE
        );
        var head = ItemModelUtils.tintedModel(
                ModelLocationUtils.getModelLocation(ModItems.CLAY_SOLDIER.get(), "_on_head"),
                ClaySoldiersClient.ClaySoldierItemTintSource.INSTANCE
        );

        itemModels.itemModelOutput.accept(ModItems.CLAY_SOLDIER.get(),
                ItemModelUtils.select(
                        new DisplayContext(),
                        inHand,
                        ItemModelUtils.when(ItemDisplayContext.HEAD, head)
                )
        );


        generateClayPouch(ModItems.CLAY_POUCH.get(), itemModels);
        generateClayBrush(ModItems.CLAY_BRUSH.get(), itemModels);

        itemModels.generateFlatItem(ModItems.BLUEPRINT_PAGE.get(), ModelTemplates.FLAT_ITEM);

        generateBlueprint(ModItems.BLUEPRINT.get(), ModItems.BLUEPRINT_PAGE.asItem(), itemModels);
    }

    private void generateClayPouch(Item pouch, ItemModelGenerators modelGenerators) {
        ResourceLocation baseTexture = TextureMapping.getItemTexture(pouch);
        ResourceLocation pouchColor = TextureMapping.getItemTexture(pouch, "_cross");

        modelGenerators.itemModelOutput.accept(pouch,
                ItemModelUtils.tintedModel(
                        ModelTemplates.TWO_LAYERED_ITEM.create(pouchColor, TextureMapping.layered(baseTexture, pouchColor), modelGenerators.modelOutput),
                        ItemModelGenerators.BLANK_LAYER, ClaySoldiersClient.ClayPouchItemTintSource.INSTANCE
                ));
    }

    private void generateClayBrush(Item clayBrush, ItemModelGenerators modelGenerators) {
        modelGenerators.itemModelOutput.accept(clayBrush,
                ItemModelUtils.rangeSelect(ClaySoldiersClient.ClayBrushConditionalProperty.INSTANCE, List.of(
                ItemModelUtils.override(
                        ItemModelUtils.plainModel(
                                ModelTemplates.TWO_LAYERED_ITEM.create(
                                        ModelLocationUtils.getModelLocation(clayBrush, "_command"),
                                        TextureMapping.layered(ModelLocationUtils.getModelLocation(clayBrush), ModelLocationUtils.getModelLocation(clayBrush, "_command")),
                                        modelGenerators.modelOutput
                                )
                        ),
                        ClayBrushItem.Mode.COMMAND.getOverrideProperty()
                ),
                ItemModelUtils.override(
                        ItemModelUtils.plainModel(
                                ModelTemplates.TWO_LAYERED_ITEM.create(
                                        ModelLocationUtils.getModelLocation(clayBrush, "_work"),
                                        TextureMapping.layered(ModelLocationUtils.getModelLocation(clayBrush), ModelLocationUtils.getModelLocation(clayBrush, "_work")),
                                        modelGenerators.modelOutput
                                )
                        ),
                        ClayBrushItem.Mode.WORK.getOverrideProperty()
                ),
                ItemModelUtils.override(
                        ItemModelUtils.plainModel(
                                ModelTemplates.TWO_LAYERED_ITEM.create(
                                        ModelLocationUtils.getModelLocation(clayBrush, "_poi"),
                                        TextureMapping.layered(ModelLocationUtils.getModelLocation(clayBrush), ModelLocationUtils.getModelLocation(clayBrush, "_poi")),
                                        modelGenerators.modelOutput
                                )
                        ),
                        ClayBrushItem.Mode.POI.getOverrideProperty()
                )
        )));
    }

    private void generateBlueprint(Item blueprint, Item blueprintPage, ItemModelGenerators modelGenerators) {
        int maxCount = 3;
        List<RangeSelectItemModel.Entry> models = new ArrayList<>();
        for (int i = 0; i < maxCount; i ++) {
            models.add(ItemModelUtils.override(
                    ItemModelUtils.plainModel(
                            ModelTemplates.TWO_LAYERED_ITEM.create(
                                    ModelLocationUtils.getModelLocation(blueprint, "_marking_" + i),
                                    TextureMapping.layered(ModelLocationUtils.getModelLocation(blueprintPage), ModelLocationUtils.getModelLocation(blueprint, "_marking_" + i)),
                                    modelGenerators.modelOutput
                            )
                    ),
                    (float) i / (maxCount - 1)
            ));
        }

        modelGenerators.itemModelOutput.accept(blueprint,
                ItemModelUtils.rangeSelect(
                        ClaySoldiersClient.BlueprintPageConditionalProperty.INSTANCE,
                        models
                ));
    }
}
