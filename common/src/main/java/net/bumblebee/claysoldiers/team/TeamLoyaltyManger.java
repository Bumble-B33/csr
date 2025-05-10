package net.bumblebee.claysoldiers.team;

import com.mojang.datafixers.util.Pair;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.networking.ClayTeamPlayerDataPayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class TeamLoyaltyManger {
    private static final Logger LOGGER = ClaySoldiersCommon.LOGGER;
    public static final StreamCodec<RegistryFriendlyByteBuf, TeamPlayerData.PlayerData> PLAYER_DATA_STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, TeamPlayerData.PlayerData::getUUID,
            ComponentSerialization.STREAM_CODEC, TeamPlayerData.PlayerData::getLastDisplayName,
            TeamPlayerData.PlayerData::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Pair<ResourceLocation, TeamPlayerData.PlayerData>> STREAM_CODEC_TEAM_PLAYER = StreamCodec.composite(ResourceLocation.STREAM_CODEC, Pair::getFirst, PLAYER_DATA_STREAM_CODEC, Pair::getSecond, Pair::of);
    public static final StreamCodec<RegistryFriendlyByteBuf, List<Pair<ResourceLocation, TeamPlayerData.PlayerData>>> STREAM_CODEC_TEAM_PLAYER_DATA = STREAM_CODEC_TEAM_PLAYER.apply(ByteBufCodecs.list());

    public static TeamPlayerData getTeamPlayerData(ServerLevel level) {
        return ServerTeamPlayerData.getFromLevel(level);
    }

    public static TeamPlayerData getClientTeamPlayerData() {
        TeamPlayerData instance = ClientTeamPlayerData.INSTANCE;
        if (instance == null) {
            LOGGER.error("ClientTeamPlayerData has not yet been instantiated. Setting it to Empty");
            ClientTeamPlayerData.setEmpty();
        }
        return instance;
    }

    @ApiStatus.Internal
    public static void createClientTeamPlayerData(List<Pair<ResourceLocation, TeamPlayerData.PlayerData>> data, boolean reload) {
        ClientTeamPlayerData.createInstance(data, reload);
    }

    @ApiStatus.Internal
    public static void updateClientTeamPlayerData(ResourceLocation teamId, TeamPlayerData.PlayerData player) {
        ClientTeamPlayerData.INSTANCE.update(teamId, player);
    }

    @ApiStatus.Internal
    public static void removeClientTeamPlayerData(ResourceLocation teamId) {
        ClientTeamPlayerData.INSTANCE.remove(teamId);
    }

    @ApiStatus.Internal
    public static List<Pair<ResourceLocation, TeamPlayerData.PlayerData>> getTeamData(ServerLevel serverLevel) {
        return ServerTeamPlayerData.getFromLevel(serverLevel).toData();
    }

    public static boolean setTeamPlayer(ServerLevel level, ResourceLocation team, @Nullable Player player) {
        ServerTeamPlayerData serverData = ServerTeamPlayerData.getFromLevel(level);
        if (player != null) {
            return serverData.putPlayer(team, player);
        } else {
            var prev = serverData.teamPlayerMap.remove(team);
            if (prev != null) {
                ClaySoldiersCommon.NETWORK_MANGER.sendToAllPlayers(level, new ClayTeamPlayerDataPayload.Remove(team));
                return true;
            }
            return false;
        }

    }

    private static class ClientTeamPlayerData implements TeamPlayerData {
        private static ClientTeamPlayerData INSTANCE = null;
        private static boolean wasCalledBeforeCreation = false;
        private final Map<ResourceLocation, PlayerData> teamPlayerMap;

        private ClientTeamPlayerData(List<Pair<ResourceLocation, PlayerData>> data) {
            teamPlayerMap = new HashMap<>(data.size());
            data.forEach(pair -> teamPlayerMap.put(pair.getFirst(), pair.getSecond()));
        }

        public static void createInstance(List<Pair<ResourceLocation, PlayerData>> data, boolean reload) {
            if (wasCalledBeforeCreation) {
                LOGGER.error("ClientTeamPlayerData was accessed before creation. Resetting it");
            }

            if (INSTANCE != null && !reload) {
                LOGGER.debug("ClientTeamPlayerData instantiated twice. Current: {} , new Data: {}", INSTANCE, data);
            }
            INSTANCE = new ClientTeamPlayerData(data);
        }

        private static void setEmpty() {
            INSTANCE = new ClientTeamPlayerData(List.of());
            wasCalledBeforeCreation = true;
        }

        public void update(ResourceLocation teamID, PlayerData player) {
            ResourceLocation toRemove = null;
            for (var entry : ClientTeamPlayerData.INSTANCE.teamPlayerMap.entrySet()) {
                if (entry.getValue().equals(player)) {
                    toRemove = entry.getKey();
                    break;
                }
            }
            teamPlayerMap.remove(toRemove);
            teamPlayerMap.put(teamID, player);
        }

        public void remove(ResourceLocation teamID) {
            teamPlayerMap.remove(teamID);
        }

        @Override
        public boolean putPlayerIfAbsent(ResourceLocation teamId, Player player) {
            throw new UnsupportedOperationException("ClayMobTeam cannot be modified from the Client");
        }

        @Override
        public PlayerData getPlayerForTeam(ResourceLocation teamId) {
            return teamPlayerMap.get(teamId);
        }

        @Override
        public long lastChangeTime() {
            return -1;
        }

        @Override
        public void forEach(BiConsumer<ResourceLocation, PlayerData> action) {
            teamPlayerMap.forEach(action);
        }

        @Override
        public void updatePlayerName(ResourceLocation teamId, Player player) {
            throw new UnsupportedOperationException("ClayMobTeam cannot be modified from the Client");
        }

        @Override
        public String toString() {
            return "ClientTeamLoyalData{" + teamPlayerMap + '}';
        }

    }

    private static class ServerTeamPlayerData extends SavedData implements TeamPlayerData {
        private static final Logger LOGGER = ClaySoldiersCommon.LOGGER;

        private static final String TAG_NAME = "teamPlayerMap";
        private final ServerLevel level;
        private final Map<ResourceLocation, PlayerData> teamPlayerMap = new HashMap<>();
        private long timeStampLastChange = 0L;

        private ServerTeamPlayerData(ServerLevel level) {
            this.level = level;
        }

        public static ServerTeamPlayerData getFromLevel(ServerLevel level) {
            return level.getServer().overworld().getDataStorage().computeIfAbsent(ServerTeamPlayerData.factory(level), "clayTeamLoyalty");
        }

        public List<Pair<ResourceLocation, PlayerData>> toData() {
            return teamPlayerMap.entrySet().stream().map(e -> Pair.of(e.getKey(), (PlayerData) e.getValue())).toList();
        }

        private static Factory<ServerTeamPlayerData> factory(ServerLevel pLevel) {
            return new Factory<>(() -> new ServerTeamPlayerData(pLevel), (tag, provider) -> load(pLevel, tag), null);
        }

        private static ServerTeamPlayerData load(ServerLevel level, CompoundTag tag) {
            ServerTeamPlayerData teamPlayerData = new ServerTeamPlayerData(level);
            if (!tag.contains(TAG_NAME)) {
                return teamPlayerData;
            }

            CompoundTag mapTag = tag.getCompound(TAG_NAME).copy();
            LOGGER.debug("Started Loading TeamLoyaltyData");
            for (String teamId : mapTag.getAllKeys()) {
                var resTeamId = ResourceLocation.parse(teamId);

                if (ClayMobTeamManger.isValidTeam(resTeamId, level.registryAccess())) {
                    loadPlayerData(mapTag.get(teamId),
                            (playerData) -> teamPlayerData.teamPlayerMap.put(resTeamId, playerData)
                    );
                } else {
                    LOGGER.error("{} Team does not exist anymore removing it from SavedData", teamId);
                }
            }
            LOGGER.debug("Finished Loading TeamLoyaltyData");
            return teamPlayerData;
        }

        @Override
        public CompoundTag save(CompoundTag pTag, HolderLookup.Provider pRegistries) {
            if (!teamPlayerMap.isEmpty()) {
                CompoundTag mapTag = new CompoundTag();
                teamPlayerMap.forEach((k, v) -> savePlayerData(mapTag, k.toString(), v));

                pTag.put(TAG_NAME, mapTag);
            }

            return pTag;
        }

        @Override
        public PlayerData getPlayerForTeam(ResourceLocation teamId) {
            return teamPlayerMap.get(teamId);
        }

        @Override
        public long lastChangeTime() {
            return timeStampLastChange;
        }

        @Override
        public boolean putPlayerIfAbsent(ResourceLocation teamId, Player player) {
            if (teamPlayerMap.containsKey(teamId)) {
                return false;
            }
            putPlayer(teamId, player);
            return true;
        }

        public boolean putPlayer(ResourceLocation teamId, Player player) {
            @Nullable
            PlayerData data = teamPlayerMap.get(teamId);
            if (data != null && data.is(player)) {
                return false;
            }

            ResourceLocation toRemove = null;
            for (var entry : teamPlayerMap.entrySet()) {
                if (entry.getValue().getUUID().equals(player.getUUID())) {
                    toRemove = entry.getKey();
                    break;
                }
            }
            teamPlayerMap.remove(toRemove);
            var playerData = playerDataFromPlayer(player);
            teamPlayerMap.put(teamId, playerData);
            setDirty();
            timeStampLastChange = level.getGameTime();
            ClaySoldiersCommon.NETWORK_MANGER.sendToAllPlayers(level, new ClayTeamPlayerDataPayload.Single(teamId, playerData));
            return true;
        }

        @Override
        public void forEach(BiConsumer<ResourceLocation, PlayerData> action) {
            teamPlayerMap.forEach(action);
        }

        @Override
        public void updatePlayerName(ResourceLocation teamId, Player player) {
            var data = teamPlayerMap.get(teamId);
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
            return "ServerTeamLoyalData{" + teamPlayerMap + '}';
        }

        public static PlayerData playerDataFromPlayer(Player player) {
            return new PlayerData(player.getUUID(), player.getDisplayName());
        }

        public static void loadPlayerData(Tag tag, Consumer<PlayerData> thenDo) {
            PLAYER_DATA_CODEC.parse(NbtOps.INSTANCE, tag)
                    .ifSuccess(thenDo)
                    .ifError(e -> LOGGER.error("Error Loading TeamLoyalty: {}", e.message()));
        }

        public static void savePlayerData(CompoundTag tag, String key, PlayerData data) {
            PLAYER_DATA_CODEC.encodeStart(NbtOps.INSTANCE, data)
                    .ifSuccess(t -> tag.put(key, t))
                    .ifError(e -> LOGGER.error("Error Saving TeamLoyalty for [{}|{}]: {}", key, data.getLastDisplayName(), e.message()));
        }
    }
}
