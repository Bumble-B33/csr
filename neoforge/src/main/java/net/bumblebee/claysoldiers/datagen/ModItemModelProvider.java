package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.blueprint.BlueprintItem;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Objects;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClaySoldiersCommon.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicTool(ModItems.SHEAR_BLADE.get());
        basicTool(ModItems.SHARPENED_STICK.get());

        basicItem(ModItems.BRICKED_CLAY_SOLDIER.get());
        basicItem(ModItems.CLAY_DISRUPTOR.get());
        basicItem(ModItems.TERRACOTTA_DISRUPTOR.get());
        basicItem(ModItems.CLAY_COOKIE.get());
        basicItem(ModItems.CLAY_GOGGLES.get());

        basicItem(ModItems.TEST_ITEM.get());

        clayBrush();
        basicItem(ModItems.BLUEPRINT_PAGE.get());
        blueprint();
        basicSoldier(ModItems.CLAY_SOLDIER.get());

        basicItem(ModItems.CAKE_HORSE.get());
        basicItem(ModItems.GRASS_HORSE.get());
        basicItem(ModItems.SNOW_HORSE.get());
        basicItem(ModItems.MYCELIUM_HORSE.get());
        basicItem(ModItems.CAKE_PEGASUS.get());
        basicItem(ModItems.GRASS_PEGASUS.get());
        basicItem(ModItems.SNOW_PEGASUS.get());
        basicItem(ModItems.MYCELIUM_PEGASUS.get());
        basicItem(ModItems.CLAY_POUCH.get())
                .texture("layer1", getDefaultItemTextureLocation(BuiltInRegistries.ITEM.getKey(ModItems.CLAY_POUCH.get())).withSuffix("_cross"));

        getBuilder(BuiltInRegistries.ITEM.getKey(ModItems.CLAY_STAFF.get()).toString())
                .parent(new ModelFile.UncheckedModelFile("builtin/entity"))
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(0, 30, 0).translation(11, 17, 4.5f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(0, -30, 0).translation(11, 17, 4.5f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, -90, 25).translation(-3, 17, 1).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 90, -25).translation(13, 17, 1).end()
                .transform(ItemDisplayContext.GUI).rotation(15, -25, -5).translation(2, 6, 0).scale(0.65f).end()
                .transform(ItemDisplayContext.FIXED).rotation(0, 180, 0).translation(-2, 4, -5).scale(0.5f).end()
                .transform(ItemDisplayContext.GROUND).rotation(0, 0, 0).translation(4, 16, 2).scale(0.75f).end()
                .end()
        ;
    }

    public void basicSoldier(Item item) {
        ResourceLocation location = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item));

        getBuilder(location.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ClaySoldierSpawnItem.TEXTURE_LOCATION);
    }

    public ItemModelBuilder basicTool(Item item) {
        ResourceLocation resourceLocation = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item));

        return getBuilder(resourceLocation.toString())
                .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                .texture("layer0", getDefaultItemTextureLocation(resourceLocation));

    }

    public ResourceLocation getDefaultItemTextureLocation(ResourceLocation location) {
        return ResourceLocation.fromNamespaceAndPath(location.getNamespace(), "item/" + location.getPath());
    }


    private void clayBrush() {
        ResourceLocation location = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(ModItems.CLAY_BRUSH.get()));


        ItemModelBuilder builder = getBuilder(location.toString())
                .parent(new ModelFile.ExistingModelFile(ResourceLocation.withDefaultNamespace("item/brush"), existingFileHelper))
                .texture("layer0", getDefaultItemTextureLocation(location));


        for (var mode : ClayBrushItem.Mode.values()) {
            builder.override().predicate(ClayBrushItem.MODE_PROPERTY, mode.getOverrideProperty()).model(clayBrushState(location, mode)).end();
        }
    }

    private ItemModelBuilder clayBrushState(ResourceLocation itemKey, ClayBrushItem.Mode mode) {
        ResourceLocation modeLocation = itemKey.withSuffix("_" + mode.getSerializedName());

        return this.getBuilder(modeLocation.toString())
                .parent(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(modid, "item/clay_brush")))
                .texture("layer1", modeLocation.withPrefix("item/"));
    }

    private void blueprint() {
        ResourceLocation baseLocation = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(ModItems.BLUEPRINT.get()));
        ResourceLocation parent = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(ModItems.BLUEPRINT_PAGE.get()));

        getBuilder(baseLocation.toString())
                .parent(new ModelFile.ExistingModelFile(parent.withPrefix("item/"), existingFileHelper))
                .override().predicate(BlueprintItem.MARKING_PROPERTY, 0.0f).model(blueprintState(baseLocation, 0)).end()
                .override().predicate(BlueprintItem.MARKING_PROPERTY, 0.1f).model(blueprintState(baseLocation, 1)).end()
                .override().predicate(BlueprintItem.MARKING_PROPERTY, 0.2f).model(blueprintState(baseLocation, 2)).end();
    }

    private ItemModelBuilder blueprintState(ResourceLocation itemKey, int number) {
        ResourceLocation modeLocation = itemKey.withSuffix("_marking_" + number);

        return this.getBuilder(modeLocation.toString())
                .parent(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(modid, "item/blueprint")))
                .texture("layer1", modeLocation.withPrefix("item/"));
    }
}
