package net.bumblebee.claysoldiers.team.loyalty;

import com.mojang.datafixers.util.Pair;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ClientTeamPlayerData implements TeamPlayerData {
    public static ClientTeamPlayerData INSTANCE = null;
    private static boolean wasCalledBeforeCreation = false;
    private final Map<ResourceKey<ClayMobTeam>, TeamPlayerData.PlayerData> teamPlayerMap;

    private ClientTeamPlayerData(List<Pair<ResourceKey<ClayMobTeam>, TeamPlayerData.PlayerData>> data) {
        teamPlayerMap = new HashMap<>(data.size());
        data.forEach(pair -> teamPlayerMap.put(pair.getFirst(), pair.getSecond()));
    }

    public static void createInstance(List<Pair<ResourceKey<ClayMobTeam>, TeamPlayerData.PlayerData>> data, boolean reload) {
        if (wasCalledBeforeCreation) {
            TeamLoyaltyManger.LOGGER.error("ClientTeamPlayerData was accessed before creation. Resetting it");
        }

        if (INSTANCE != null && !reload) {
            ClaySoldiersCommon.ERROR_HANDLER.debug("ClientTeamPlayerData instantiated twice. Current: %s , new Data: %s".formatted(INSTANCE, data));
        }
        INSTANCE = new ClientTeamPlayerData(data);
    }

    public static TeamPlayerData getInstance() {
        TeamPlayerData instance = ClientTeamPlayerData.INSTANCE;
        if (instance == null) {
            TeamLoyaltyManger.LOGGER.error("ClientTeamPlayerData has not yet been instantiated. Setting it to Empty");
            ClientTeamPlayerData.setEmpty();
        }
        return instance;
    }

    private static void setEmpty() {
        INSTANCE = new ClientTeamPlayerData(List.of());
        wasCalledBeforeCreation = true;
    }

    public void update(ResourceKey<ClayMobTeam> teamID, TeamPlayerData.PlayerData player) {
        ResourceKey<ClayMobTeam> toRemove = null;
        for (var entry : ClientTeamPlayerData.INSTANCE.teamPlayerMap.entrySet()) {
            if (entry.getValue().equals(player)) {
                toRemove = entry.getKey();
                break;
            }
        }
        teamPlayerMap.remove(toRemove);
        teamPlayerMap.put(teamID, player);
    }

    public void remove(ResourceKey<ClayMobTeam> teamID) {
        teamPlayerMap.remove(teamID);
    }

    @Override
    public boolean putPlayerIfAbsent(ResourceKey<ClayMobTeam> teamId, Player player) {
        throw new UnsupportedOperationException("ClayMobTeam cannot be modified from the Client");
    }

    @Override
    public TeamPlayerData.PlayerData getPlayerForTeam(ResourceKey<ClayMobTeam> teamId) {
        return teamPlayerMap.get(teamId);
    }

    @Override
    public long lastChangeTime() {
        return -1;
    }

    @Override
    public void forEach(BiConsumer<ResourceKey<ClayMobTeam>, TeamPlayerData.PlayerData> action) {
        teamPlayerMap.forEach(action);
    }

    @Override
    public void updatePlayerName(ResourceKey<ClayMobTeam> teamId, Player player) {
        throw new UnsupportedOperationException("ClayMobTeam cannot be modified from the Client");
    }

    @Override
    public String toString() {
        return "ClientTeamLoyalData{" + teamPlayerMap + '}';
    }

}
