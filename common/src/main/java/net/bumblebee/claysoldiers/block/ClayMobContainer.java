package net.bumblebee.claysoldiers.block;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public interface ClayMobContainer {
    /**
     * Kills all ClayMobEntities this container holds.
     * @param player the player executing the kill
     */
    void killSoldier(ServerLevel level, Player player);
}
