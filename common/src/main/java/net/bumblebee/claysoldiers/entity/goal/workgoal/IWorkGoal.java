package net.bumblebee.claysoldiers.entity.goal.workgoal;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface IWorkGoal {
    String STATUS_LANG_KEY = "clay_soldier_work.%s.status.%s";
    String JOB_LANG_KEY = "clay_soldier_work.%s.job.%s";
    String DEFAULT_STATUS_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "working");

    /**
     * Returns the display name of this goal.
     */
    Component getDisplayName();

    /**
     * Returns whether this goal requires the {@code ClaySoldier} to carry items.
     */
    default boolean workRequiresItemCarrying(ItemStack stack) {
        return false;
    }
    /**
     * Returns whether this goal requires the {@code ClaySoldier} to pick up items on the ground.
     */
    default boolean workRequiresItemPickUp(ItemStack stack) {
        return false;
    }

    /**
     * Converts the given status id into a display name.
     */
    default Component decodeStatus(byte id) {
        return Component.translatable(DEFAULT_STATUS_LANG);
    }

    default String asString() {
        return this.getClass().getSimpleName();
    }
}
