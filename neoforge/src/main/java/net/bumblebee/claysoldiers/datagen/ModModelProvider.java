package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.ClaySoldiersClient;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.SpecialItemRenderers;
import net.bumblebee.claysoldiers.block.cacti.ClayCactusBlock;
import net.bumblebee.claysoldiers.datamap.ThrowableTransform;
import net.bumblebee.claysoldiers.entity.client.programmable.ProgrammableClaySoldierRenderer;
import net.bumblebee.claysoldiers.init.ModBlocks;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.*;

public class ModModelProvider extends ModelProvider {
    private static final ModelTemplate SPECIAL_BLOCK = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .build();

    public static final ModelTemplate CLAY_STAFF_MODEL = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("item/generated"))
            .guiLight(UnbakedModel.GuiLight.FRONT)
            .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(0, 30, 0).translation(11, 17, 4.5f))
            .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, t -> t.rotation(0, -30, 0).translation(11, 17, 4.5f))
            .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, t -> t.rotation(0, -90, 25).translation(-3, 17, 1))
            .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, t -> t.rotation(0, 90, 25).translation(13, 17, 1))
            .transform(ItemDisplayContext.GUI, t -> t.rotation(15, -25, -5).translation(2, 6, 0f))
            .transform(ItemDisplayContext.FIXED, t -> t.rotation(0, 180, 0).translation(-2, 4, 0.5f))
            .transform(ItemDisplayContext.GROUND, t -> t.rotation(0, 0, 0).translation(4, 16, 0.75f))
            .build();
    private static final TextureSlot LAYER3 = TextureSlot.create("layer3");

    public static ModelTemplate CLAY_SOLDIER_CHIP_ADDON_OVERLAY = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("item/generated"))
            .requiredTextureSlot(TextureSlot.LAYER0)
            .requiredTextureSlot(TextureSlot.LAYER1)
            .requiredTextureSlot(TextureSlot.LAYER2)
            .requiredTextureSlot(LAYER3)
            .build();

    public static final ModelTemplate CACTUS_HOME_TEMPLATE = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "block/cactus_home_base"))
            .requiredTextureSlot(TextureSlot.NORTH)
            .requiredTextureSlot(TextureSlot.SOUTH)
            .requiredTextureSlot(TextureSlot.EAST)
            .requiredTextureSlot(TextureSlot.WEST)
            .build();

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
                                Optional.empty(),
                                new SpecialItemRenderers.ClayStaffSpecialRenderer.Unbaked()
                        )
                )

        );
        CLAY_STAFF_MODEL.create(ModelLocationUtils.getModelLocation(ModItems.CLAY_STAFF.get(), "_in_hand"), new TextureMapping(), itemModels.modelOutput);

        itemModels.generateFlatItem(ModItems.BLANK_ADDON.get(), ModelTemplates.FLAT_ITEM);
        generateAddon(ModItems.RANGE_ADDON.get(), ModItems.BLANK_ADDON.get(), itemModels);
        generateAddon(ModItems.TREASURY_ADDON.get(), ModItems.BLANK_ADDON.get(), itemModels);
        generateAddon(ModItems.NO_BREAK_ADDON.get(), ModItems.BLANK_ADDON.get(), itemModels);
        generateAddon(ModItems.TARGET_ANIMAL_ADDON.get(), ModItems.BLANK_ADDON.get(), itemModels);
        generateAddon(ModItems.TARGET_MONSTER_ADDON.get(), ModItems.BLANK_ADDON.get(), itemModels);
        generateAddon(ModItems.TARGET_IGNORE_BABIES_ADDON.get(), ModItems.BLANK_ADDON.get(), itemModels);
        generateAddon(ModItems.ACCELERATION_ADDON.get(), ModItems.BLANK_ADDON.get(), itemModels);

        ItemModel.Unbaked fishingRodModel = ItemModelUtils.plainModel(
                ModelLocationUtils.getModelLocation(Items.FISHING_ROD, "_cast")
        );


        itemModels.itemModelOutput.register(
                ProgrammableClaySoldierRenderer.FISHING_ROD_CAST_MODEL,
                new ClientItem(
                        fishingRodModel,
                        ClientItem.Properties.DEFAULT
                )
        );


        createThrown(itemModels, ThrowableTransform.SWEET_BERRY_MODEL);
        createThrown(itemModels, ThrowableTransform.GLOW_BERRY_MODEL);

        TextureMapping texturemapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.SPRUCE_PLANKS))
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.SPRUCE_PLANKS))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(ModBlocks.ESCRITOIRE_BLOCK.get(), "_top"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(ModBlocks.ESCRITOIRE_BLOCK.get(), "_side"));

        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
                        ModBlocks.ESCRITOIRE_BLOCK.get(),
                        BlockModelGenerators.plainVariant(ModelTemplates.CUBE_BOTTOM_TOP.create(ModBlocks.ESCRITOIRE_BLOCK.get(), texturemapping, blockModels.modelOutput))
                )
        );

        blockModels.createNonTemplateHorizontalBlock(ModBlocks.CHIP_ASSEMBLER.get());


        blockModels.createParticleOnlyBlock(ModBlocks.HAMSTER_WHEEL_BLOCK.get(), Blocks.COPPER_BLOCK);
        blockModels.createParticleOnlyBlock(ModBlocks.EASEL_BLOCK.get(), Blocks.OAK_PLANKS);

        blockModels.createCrossBlock(ModBlocks.SUGAR_CANE_HAMMOCK.get(), BlockModelGenerators.PlantType.TINTED);

        createCactusHome(blockModels, ModBlocks.CACTUS_HOUSE.get(), ClayCactusBlock.COUNT.getPossibleValues().stream().max(Integer::compareTo).orElse(0));

        blockModels.generateSimpleSpecialItemModel(ModBlocks.HAMSTER_WHEEL_BLOCK.get(), Optional.empty(), new SpecialItemRenderers.HamsterWheelSpecialRenderer.Unbaked());
        blockModels.generateSimpleSpecialItemModel(ModBlocks.EASEL_BLOCK.get(), Optional.empty(), new SpecialItemRenderers.EaselBlockSpecialRenderer.Unbaked());

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
                        ItemModelUtils.when(List.of(ItemDisplayContext.HEAD, ItemDisplayContext.ON_SHELF), head)
                )
        );

        generateClayPouch(ModItems.CLAY_POUCH.get(), itemModels);
        generateClayBrush(ModItems.CLAY_BRUSH.get(), itemModels);

        itemModels.generateFlatItem(ModItems.BLUEPRINT_PAGE.get(), ModelTemplates.FLAT_ITEM);

        generateBlueprint(ModItems.BLUEPRINT.get(), ModItems.BLUEPRINT_PAGE.asItem(), itemModels);

        itemModels.generateFlatItem(ModItems.STATOMETER.get(), ModelTemplates.FLAT_ITEM);

        var chipModel = createChipModel(ModItems.BLANK_CHIP.asItem(), itemModels);

        itemModels.itemModelOutput.accept(ModItems.BLANK_CHIP.get(), chipModel);
        itemModels.itemModelOutput.accept(ModItems.POI_CHIP.get(), chipModel);
        itemModels.itemModelOutput.accept(ModItems.COMBAT_CHIP.get(), chipModel);
        itemModels.itemModelOutput.accept(ModItems.DIG_CHIP.get(), chipModel);
        itemModels.itemModelOutput.accept(ModItems.PICK_UP_ITEMS_CHIP.get(), chipModel);
        itemModels.itemModelOutput.accept(ModItems.BREAK_CROPS_CHIP.get(), chipModel);
        itemModels.itemModelOutput.accept(ModItems.PLACE_SEEDS_CHIP.get(), chipModel);
        itemModels.itemModelOutput.accept(ModItems.FISHING_CHIP.get(), chipModel);
        itemModels.itemModelOutput.accept(ModItems.BLUEPRINT_CHIP.get(), chipModel);
        itemModels.itemModelOutput.accept(ModItems.BEE_KEEPING_CHIP.get(), chipModel);

    }

    private void generateClayPouch(Item pouch, ItemModelGenerators modelGenerators) {
        Material baseTexture = TextureMapping.getItemTexture(pouch);
        Material pouchColor = TextureMapping.getItemTexture(pouch, "_cross");

        modelGenerators.itemModelOutput.accept(pouch,
                ItemModelUtils.tintedModel(
                        ModelTemplates.TWO_LAYERED_ITEM.create(pouchColor.sprite(), TextureMapping.layered(baseTexture, pouchColor), modelGenerators.modelOutput),
                        ItemModelGenerators.BLANK_LAYER, ClaySoldiersClient.ClayPouchItemTintSource.INSTANCE
                ));
    }

    private void generateClayBrush(Item clayBrush, ItemModelGenerators modelGenerators) {
        modelGenerators.itemModelOutput.accept(clayBrush,
                ItemModelUtils.rangeSelect(ClaySoldiersClient.ClayBrushConditionalProperty.INSTANCE, List.of(
                        ItemModelUtils.override(
                                ItemModelUtils.plainModel(
                                        modelGenerators.generateLayeredItem(
                                                ModelLocationUtils.getModelLocation(clayBrush, "_command"),
                                                TextureMapping.getItemTexture(clayBrush),
                                                TextureMapping.getItemTexture(clayBrush, "_command")
                                        )
                                ),
                                ClayBrushItem.Mode.COMMAND.getOverrideProperty()
                        ),
                        ItemModelUtils.override(
                                ItemModelUtils.plainModel(
                                        modelGenerators.generateLayeredItem(
                                                ModelLocationUtils.getModelLocation(clayBrush, "_poi"),
                                                TextureMapping.getItemTexture(clayBrush),
                                                TextureMapping.getItemTexture(clayBrush, "_poi")
                                        )
                                ),
                                ClayBrushItem.Mode.POI.getOverrideProperty()
                        )
                )));
    }

    private void generateBlueprint(Item blueprint, Item blueprintPage, ItemModelGenerators modelGenerators) {
        int maxCount = 3;
        List<RangeSelectItemModel.Entry> models = new ArrayList<>();
        for (int i = 0; i < maxCount; i++) {
            models.add(ItemModelUtils.override(
                    ItemModelUtils.plainModel(
                            modelGenerators.generateLayeredItem(
                                    ModelLocationUtils.getModelLocation(blueprint, "_marking_" + i),
                                    TextureMapping.getItemTexture(blueprintPage),
                                    TextureMapping.getItemTexture(blueprint, "_marking_" + i)
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

    private void generateAddon(Item addon, Item blankAddon, ItemModelGenerators modelGenerators) {
        Identifier model = modelGenerators.generateLayeredItem(
                addon, TextureMapping.getItemTexture(blankAddon),
                TextureMapping.getItemTexture(addon)
        );

        modelGenerators.itemModelOutput.accept(addon, ItemModelUtils.plainModel(model));
    }

    private ItemModel.Unbaked createChipModel(Item chip, ItemModelGenerators modelGenerators) {
        Identifier modelId = modelGenerators.generateLayeredItem(chip, TextureMapping.getItemTexture(chip, "_base"), TextureMapping.getItemTexture(chip, "_connection"));

        return ItemModelUtils.composite(
                ItemModelUtils.tintedModel(modelId, ClaySoldiersClient.ClaySoldierChipTintSource.BASE, ClaySoldiersClient.ClaySoldierChipTintSource.CONNECTION),
                generateAddonOverlay(modelGenerators)
        );
    }

    private ItemModel.Unbaked generateAddonOverlay(ItemModelGenerators modelGenerators) {
        return ItemModelUtils.tintedModel(CLAY_SOLDIER_CHIP_ADDON_OVERLAY.create(
                        Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "item/clay_soldier_chip_addon_overlay"),
                        TextureMapping.layered(
                                new Material(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "item/clay_soldier_chip_addon_1")),
                                new Material(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "item/clay_soldier_chip_addon_2")),
                                new Material(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "item/clay_soldier_chip_addon_3"))
                        ).put(LAYER3, new Material(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "item/clay_soldier_chip_addon_4"))),
                        modelGenerators.modelOutput
                ),
                new ClaySoldiersClient.ClaySoldierChipAddonTintSource(0),
                new ClaySoldiersClient.ClaySoldierChipAddonTintSource(1),
                new ClaySoldiersClient.ClaySoldierChipAddonTintSource(2),
                new ClaySoldiersClient.ClaySoldierChipAddonTintSource(3)
        );
    }


    private static void createThrown(ItemModelGenerators modelGenerators, Identifier model) {
        modelGenerators.itemModelOutput.register(
                model,
                new ClientItem(
                        ItemModelUtils.plainModel(
                                ModelTemplates.FLAT_ITEM.create(model.withPrefix("item/"), TextureMapping.layer0(new Material(model.withPrefix("item/"))), modelGenerators.modelOutput)),
                        ClientItem.Properties.DEFAULT
                )
        );
    }


    private static void createCactusHome(BlockModelGenerators modelGenerators, Block cactus, int count) {
        var dispatchBuilder = PropertyDispatch.initial(ClayCactusBlock.COUNT);

        if (count != 4) {
            throw new IllegalStateException("Count cannot not be not 4: " + count);
        }

        Map<Integer, List<Variant>> variants = new HashMap<>(count);
        for (int i = 0; i <= count; i++) {
            variants.put(i, new ArrayList<>());
        }

        for (int i = 0; i < 16; i++) {
            boolean north = (i & 1) == 1;
            boolean east = (i & 2) == 2;
            boolean south = (i & 4) == 4;
            boolean west = (i & 8) == 8;

            String name = "%s%s%s%s%s".formatted(
                    north || east || south || west ? "_" : "",
                    north ? "n" : "",
                    east ? "e" : "",
                    south ? "s" : "",
                    west ? "w" : ""
            );

            int currentCount = (north ? 1 : 0) +
                    (east ? 1 : 0) +
                    (south ? 1 : 0) +
                    (west ? 1 : 0);

            TextureMapping textureMapping = textureMappingCactus(cactus, north, east, south, west);

            Identifier model = CACTUS_HOME_TEMPLATE.create(
                    ModelLocationUtils.getModelLocation(cactus, name),
                    textureMapping,
                    modelGenerators.modelOutput
            );

            Variant variant = BlockModelGenerators.plainModel(model);

            variants.get(currentCount).add(variant);
        }

        modelGenerators.registerSimpleItemModel(cactus.asItem(), variants.get(0).getFirst().modelLocation());

        variants.forEach((k, v) -> {
            dispatchBuilder.select(k, BlockModelGenerators.variants(v.toArray(Variant[]::new)));
        });


        modelGenerators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(cactus)
                        .with(dispatchBuilder)
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    private static Material getCactiHomeTexture(Block block, String suffix) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        return new Material(id.withPath(path -> "block/" + path + "/" + suffix));
    }

    private static TextureMapping textureMappingCactus(Block cactus, boolean north, boolean east, boolean south, boolean west) {
        String northId;
        if (north) {
            northId = west ? "full" : "empty_full";
        } else {
            northId = west ? "full_empty" : "empty";
        }

        return new TextureMapping()
                .put(TextureSlot.NORTH, getCactiHomeTexture(cactus, northId + "_north"))
                .put(TextureSlot.EAST, getCactiHomeTexture(cactus, (east ? "full" : "empty") + "_east"))
                .put(TextureSlot.SOUTH, getCactiHomeTexture(cactus, (south ? "full" : "empty") + "_south"))
                .put(TextureSlot.WEST, getCactiHomeTexture(cactus, (west ? "full" : "empty") + "_west"));

    }
}
