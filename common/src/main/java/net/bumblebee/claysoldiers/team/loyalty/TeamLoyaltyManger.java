package net.bumblebee.claysoldiers.team.loyalty;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

public final class TeamLoyaltyManger {
    static final Logger LOGGER = ClaySoldiersCommon.LOGGER;
    public static final StreamCodec<ByteBuf, ResourceKey<ClayMobTeam>> KEY_STREAM_CODEC = ResourceKey.streamCodec(ModRegistries.CLAY_MOB_TEAMS);
    public static final StreamCodec<RegistryFriendlyByteBuf, TeamPlayerData.PlayerData> PLAYER_DATA_STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, TeamPlayerData.PlayerData::getUUID,
            ComponentSerialization.STREAM_CODEC, TeamPlayerData.PlayerData::getLastDisplayName,
            TeamPlayerData.PlayerData::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Pair<ResourceKey<ClayMobTeam>, TeamPlayerData.PlayerData>> STREAM_CODEC_TEAM_PLAYER = StreamCodec.composite(KEY_STREAM_CODEC, Pair::getFirst, PLAYER_DATA_STREAM_CODEC, Pair::getSecond, Pair::of);
    public static final StreamCodec<RegistryFriendlyByteBuf, List<Pair<ResourceKey<ClayMobTeam>, TeamPlayerData.PlayerData>>> STREAM_CODEC_TEAM_PLAYER_DATA = STREAM_CODEC_TEAM_PLAYER.apply(ByteBufCodecs.list());

    public static TeamPlayerData getTeamPlayerData(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return getTeamPlayerData(serverLevel);
        }
        return getClientTeamPlayerData();
    }

    public static TeamPlayerData getTeamPlayerData(ServerLevel level) {
        return ServerTeamPlayerData.getFromLevel(level);
    }

    public static TeamPlayerData getClientTeamPlayerData() {
        return ClientTeamPlayerData.getInstance();
    }

    @ApiStatus.Internal
    public static void createClientTeamPlayerData(List<Pair<ResourceKey<ClayMobTeam>, TeamPlayerData.PlayerData>> data, boolean reload) {
        ClientTeamPlayerData.createInstance(data, reload);
    }

    @ApiStatus.Internal
    public static void updateClientTeamPlayerData(ResourceKey<ClayMobTeam> teamId, TeamPlayerData.PlayerData player) {
        ClientTeamPlayerData.INSTANCE.update(teamId, player);
    }

    @ApiStatus.Internal
    public static void removeClientTeamPlayerData(ResourceKey<ClayMobTeam> teamId) {
        ClientTeamPlayerData.INSTANCE.remove(teamId);
    }

    @ApiStatus.Internal
    public static List<Pair<ResourceKey<ClayMobTeam>, TeamPlayerData.PlayerData>> getTeamData(ServerLevel serverLevel) {
        return ServerTeamPlayerData.getFromLevel(serverLevel).toData();
    }

    public static boolean setTeamPlayer(ServerLevel level, Holder.Reference<ClayMobTeam> team, @Nullable Player player) {
        return ServerTeamPlayerData.setTeamPlayer(level, team, player);
    }
}
