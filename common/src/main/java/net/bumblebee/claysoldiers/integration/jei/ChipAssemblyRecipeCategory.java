package net.bumblebee.claysoldiers.integration.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.chipassembler.ChipAssemblerBlock;
import net.bumblebee.claysoldiers.init.ModBlocks;
import net.bumblebee.claysoldiers.recipe.chip.BasicChipAssemblyRecipe;
import net.bumblebee.claysoldiers.recipe.chip.ChipAssemblyRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class ChipAssemblyRecipeCategory implements IRecipeCategory<BasicChipAssemblyRecipe> {
    public static final IRecipeType<BasicChipAssemblyRecipe> TYPE = IRecipeType.create(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "chip_assembly"), BasicChipAssemblyRecipe.class);

    private final IDrawable icon;

    public ChipAssemblyRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.CHIP_ASSEMBLER);
    }

    @Override
    public IRecipeType<BasicChipAssemblyRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(ChipAssemblerBlock.CONTAINER_TITLE);
    }

    @Override
    public int getWidth() {
        return 128;
    }

    @Override
    public int getHeight() {
        return 54;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BasicChipAssemblyRecipe recipe, IFocusGroup focuses) {
        var builderCenter = builder.addInputSlot(20, 11).setStandardSlotBackground();
        recipe.chip().ifPresent(builderCenter::add);

        var builderBL = builder.addInputSlot(0, 0).setStandardSlotBackground();
        var builderBR = builder.addInputSlot(40, 0).setStandardSlotBackground();
        var builderFL = builder.addInputSlot(0, 20).setStandardSlotBackground();
        var builderFR = builder.addInputSlot(40, 20).setStandardSlotBackground();

        List<IRecipeSlotBuilder> slots = List.of(
                builderBL, builderBR, builderFL, builderFR
        );
        List<Ingredient> inputs = recipe.getInputs();

        for (int i = 0; i < inputs.size(); i++) {
            slots.get(i).add(inputs.get(i));
        }

        builder.addOutputSlot(106, 15).add(recipe.createResult()).setOutputSlotBackground();
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, BasicChipAssemblyRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(recipe.adjustedBuiltTime()).setPosition(69, 11);

        addBuiltTime(builder, recipe);
        addEnergy(builder, recipe);
    }

    protected void addBuiltTime(IRecipeExtrasBuilder builder, ChipAssemblyRecipe recipe) {
        int builtTime = recipe.adjustedBuiltTime();
        if (builtTime > 0) {
            int builtTimeSeconds = builtTime / 20;
            Component timeString = Component.literal(builtTimeSeconds + "s");
            builder.addText(timeString, getWidth() - 20, 10)
                    .setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM)
                    .setTextAlignment(HorizontalAlignment.RIGHT)
                    .setTextAlignment(VerticalAlignment.BOTTOM)
                    .setColor(0xFF808080);

        }

    }

    protected void addEnergy(IRecipeExtrasBuilder builder, ChipAssemblyRecipe recipe) {
        int energyCost = recipe.energyCost() * recipe.adjustedBuiltTime();
        if (energyCost > 0) {
            Component experienceString = Component.literal(energyCost + ClaySoldiersCommon.PLATFORM.getEnergyUnitName());
            builder.addText(experienceString, getWidth() - 20, 10)
                    .setPosition(0, 0, getWidth() - 20, getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM)
                    .setTextAlignment(HorizontalAlignment.RIGHT)
                    .setTextAlignment(VerticalAlignment.BOTTOM)
                    .setColor(0xFF808080);
        }
    }
}
