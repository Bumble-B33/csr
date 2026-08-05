package net.bumblebee.claysoldiers.entity.goal.workgoal;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface IWorkGoal {
    String JOB_LANG_KEY = "clay_soldier_work.%s.job.%s";

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

    Component getWorkStatus();

    default String asString() {
        return this.getClass().getSimpleName();
    }
}
