package net.bumblebee.claysoldiers.block;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface ClayMobContainer {
    /**
     * Kills all ClayMobEntities this container holds.
     * @param player the player executing the kill
     */
    void killSoldier(ServerLevel level, ServerPlayer player);

    /**
     * Checks whether the given player can kill all contained ClayMob.
     * @param player the player executing the kill
     */
    boolean canKillClayMob(ServerLevel level, ServerPlayer player);
}
