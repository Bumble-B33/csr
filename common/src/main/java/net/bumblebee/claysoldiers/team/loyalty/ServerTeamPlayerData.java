package net.bumblebee.claysoldiers.team.loyalty;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.networking.ClayTeamPlayerDataPayload;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ServerTeamPlayerData extends SavedData implements TeamPlayerData {
    private static final Logger LOGGER = ClaySoldiersCommon.LOGGER;
    private static final Codec<Map<ResourceKey<ClayMobTeam>, PlayerData>> BASE_CODEC = Codec.unboundedMap(ResourceKey.codec(ModRegistries.CLAY_MOB_TEAMS), TeamPlayerData.PLAYER_DATA_CODEC);
    private static final Codec<ServerTeamPlayerData> CODEC = BASE_CODEC.xmap(ServerTeamPlayerData::new, ServerTeamPlayerData::getOrCreate);

    public static final SavedDataType<ServerTeamPlayerData> SAVED_DATA_TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_team_loyalty"),
            ServerTeamPlayerData::createEmpty,
            CODEC,
            null
    );

    private ServerLevel level;
    private final Map<ResourceKey<ClayMobTeam>, PlayerData> keyTeamPlayerMap;

    private long timeStampLastChange = 0L;

    private static ServerTeamPlayerData createEmpty() {
        return new ServerTeamPlayerData(Map.of());
    }

    private ServerTeamPlayerData(Map<ResourceKey<ClayMobTeam>, PlayerData> teamPlayerMap) {
        this.keyTeamPlayerMap = new HashMap<>(teamPlayerMap);
    }

    public static ServerTeamPlayerData getFromLevel(ServerLevel level) {
        return level.getServer().getDataStorage().computeIfAbsent(SAVED_DATA_TYPE).verify(level);
    }

    public List<Pair<ResourceKey<ClayMobTeam>, PlayerData>> toData() {
        return keyTeamPlayerMap.entrySet().stream().map(e -> Pair.of(e.getKey(), (PlayerData) e.getValue())).toList();
    }

    private Map<ResourceKey<ClayMobTeam>, PlayerData> getOrCreate() {
        if (!keyTeamPlayerMap.isEmpty()) {
            return keyTeamPlayerMap;
        }
        return keyTeamPlayerMap;
    }

    private ServerTeamPlayerData verify(ServerLevel level) {
        this.level = level;

        var map = new HashMap<>(keyTeamPlayerMap);
        keyTeamPlayerMap.clear();

        LOGGER.debug("Started Loading TeamLoyaltyData");
        for (var entry : map.entrySet()) {
            ResourceKey<ClayMobTeam> teamId = entry.getKey();

            var team = ClayMobTeamManger.get(teamId, level.registryAccess());

            if (team.isEmpty()) {
                LOGGER.error("{} Team does not exist anymore removing it from SavedData", teamId);
            } else if (!team.orElseThrow().value().canBeTamed()) {
                LOGGER.error("{} Team cannot be loyal to anyone removing it from SavedData", teamId);
            } else {
                var data = keyTeamPlayerMap.put(team.orElseThrow().key(), entry.getValue());
                if (data != null) {
                    LOGGER.error("{} Team already had player data overriding it from SavedData. Old ({}), New ({})", teamId, data, entry.getValue());
                }
            }
        }


        LOGGER.debug("Finished Loading TeamLoyaltyData");
        return this;
    }

    @Override
    public PlayerData getPlayerForTeam(ResourceKey<ClayMobTeam> teamId) {
        return keyTeamPlayerMap.get(teamId);
    }

    @Override
    public long lastChangeTime() {
        return timeStampLastChange;
    }

    @Override
    public boolean putPlayerIfAbsent(ResourceKey<ClayMobTeam> teamId, Player player) {
        if (keyTeamPlayerMap.containsKey(teamId)) {
            return false;
        }
        putPlayer(teamId, player);
        return true;
    }

    public boolean putPlayer(ResourceKey<ClayMobTeam> teamId, Player player) {
        @Nullable
        PlayerData data = keyTeamPlayerMap.get(teamId);
        if (data != null && data.is(player)) {
            return false;
        }

        ResourceKey<ClayMobTeam> toRemove = null;
        for (var entry : keyTeamPlayerMap.entrySet()) {
            if (entry.getValue().getUUID().equals(player.getUUID())) {
                toRemove = entry.getKey();
                break;
            }
        }
        keyTeamPlayerMap.remove(toRemove);
        PlayerData playerData = PlayerData.of(player);
        keyTeamPlayerMap.put(teamId, playerData);
        setDirty();
        timeStampLastChange = level.getGameTime();
        ClaySoldiersCommon.NETWORK_MANGER.sendToAllPlayers(level, new ClayTeamPlayerDataPayload.Single(teamId, playerData));
        return true;
    }

    @Override
    public void forEach(BiConsumer<ResourceKey<ClayMobTeam>, PlayerData> action) {
        keyTeamPlayerMap.forEach(action);
    }

    @Override
    public void updatePlayerName(ResourceKey<ClayMobTeam> teamId, Player player) {
        var data = keyTeamPlayerMap.get(teamId);
        if (data == null) {
            return;
        }
        if (!data.getLastDisplayName().equals(player.getDisplayName())) {
            data.setLastKnowName(player.getDisplayName());
            ClaySoldiersCommon.NETWORK_MANGER.sendToAllPlayers(level, new ClayTeamPlayerDataPayload.Single(teamId, data));

        }
    }

    @Override
    public String toString() {
        return "ServerTeamLoyalData{" + keyTeamPlayerMap + '}';
    }

    public static boolean setTeamPlayer(ServerLevel level, Holder.Reference<ClayMobTeam> team, @Nullable Player player) {
        ServerTeamPlayerData serverData = ServerTeamPlayerData.getFromLevel(level);
        if (player != null) {
            return serverData.putPlayer(team.key(), player);
        } else {
            var prev = serverData.keyTeamPlayerMap.remove(team.key());
            if (prev != null) {
                ClaySoldiersCommon.NETWORK_MANGER.sendToAllPlayers(level, new ClayTeamPlayerDataPayload.Remove(team.key()));
                return true;
            }
            return false;
        }

    }
}
