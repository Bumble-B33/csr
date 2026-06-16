package net.bumblebee.claysoldiers.blueprint;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BlueprintRequestResult {
    private static final BlueprintRequestResult FAIL = new BlueprintRequestResult(false, ItemStack.EMPTY);
    private static final BlueprintRequestResult SUCCESS_USED = new BlueprintRequestResult(false, ItemStack.EMPTY);

    private final boolean success;
    @NotNull
    private final ItemStack remainder;

    public BlueprintRequestResult(boolean success, @NotNull ItemStack remainder) {
        this.success = success;
        this.remainder = remainder;
    }

    public boolean isSuccess() {
        return success;
    }

    @NotNull
    public ItemStack getRemainder() {
        return remainder;
    }

    public boolean hasRemainder() {
        return !remainder.isEmpty();
    }

    public static BlueprintRequestResult fail() {
        return FAIL;
    }
    public static BlueprintRequestResult success() {
        return SUCCESS_USED;
    }

    public static BlueprintRequestResult success(ItemStack remainder) {
        return new BlueprintRequestResult(true, remainder);
    }
}
