package net.bumblebee.claysoldiers.block.soldiercontainer;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface ClayMobContainer {
    /**
     * Kills all ClayMobEntities this container holds.
     * @param player the player executing the kill
     * @return number of ClayMobEntities killed.
     */
    int killSoldier(ServerLevel level, ServerPlayer player);

}
