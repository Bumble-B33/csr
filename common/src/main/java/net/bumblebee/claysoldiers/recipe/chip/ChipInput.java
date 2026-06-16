package net.bumblebee.claysoldiers.recipe.chip;

import net.bumblebee.claysoldiers.block.chipassembler.ChipAssemblerInventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChipInput implements RecipeInput {
    @NotNull
    private final ItemStack chip;
    private final List<ItemStack> inputs;

    private final StackedItemContents stackedContents = new StackedItemContents();
    private final int ingredientCount;

    public ChipInput(Map<ChipAssemblerInventory.Slot, @Nullable ItemStack> input) {
        this.chip = Objects.requireNonNullElse(input.get(ChipAssemblerInventory.Slot.CENTER), ItemStack.EMPTY);
        this.inputs = new ArrayList<>();
        for (ChipAssemblerInventory.Slot slot : ChipAssemblerInventory.Slot.values()) {
            if (slot == ChipAssemblerInventory.Slot.CENTER) {
                continue;
            }
            ItemStack stack = input.get(slot);
            if (stack != null && !stack.isEmpty()) {
                inputs.add(stack);
            }
        }

        int ingredientCount = 0;

        for(ItemStack item : inputs) {
            if (!item.isEmpty()) {
                ++ingredientCount;
                this.stackedContents.accountStack(item, 1);
            }
        }

        this.ingredientCount = ingredientCount;
    }

    public int ingredientCount() {
        return ingredientCount;
    }

    public StackedItemContents stackedContents() {
        return this.stackedContents;
    }

    @NotNull
    public ItemStack getChip() {
        return chip;
    }

    public List<ItemStack> getNonCenterInputs() {
        return inputs;
    }

    @Override
    public ItemStack getItem(int i) {
        return i == 0 ? chip : inputs.get(i - 1);
    }


    @Override
    public int size() {
        return inputs.size() + 1;
    }
}
