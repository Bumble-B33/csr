package net.bumblebee.claysoldiers.integration.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.blueprint.EscritoireBlock;
import net.bumblebee.claysoldiers.init.ModBlocks;
import net.bumblebee.claysoldiers.init.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class BlueprintRecipeCategory implements IRecipeCategory<ItemStack> {
    public static final IRecipeType<ItemStack> TYPE = IRecipeType.create(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "blueprint_drawing"), ItemStack.class);

    private final IDrawable icon;

    public BlueprintRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.ESCRITOIRE_BLOCK);
    }

    @Override
    public IRecipeType<ItemStack> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(EscritoireBlock.CONTAINER_TITLE);
    }

    @Override
    public int getWidth() {
        return 82;
    }

    @Override
    public int getHeight() {
        return 34;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ItemStack stack, IFocusGroup iFocusGroup) {
        builder.addInputSlot(1, 9)
                .setStandardSlotBackground()
                .add(ModItems.BLUEPRINT_PAGE);

        builder.addOutputSlot(61,  9)
                .setOutputSlotBackground()
                .add(stack);

    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, ItemStack recipe, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(26, 9);
    }
}
