package net.bumblebee.claysoldiers.block.soldiercontainer;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface ClayMobContainer {
    /**
     * Kills all ClayMobs this container holds.
     * @param player the player executing the kill
     * @return number of ClayMobs killed.
     */
    int killSoldiers(ServerLevel level, ServerPlayer player);

}
