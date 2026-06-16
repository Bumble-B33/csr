package net.bumblebee.claysoldiers.networking;

import com.mojang.datafixers.util.Pair;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.platform.services.NetworkManger;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.loyalty.TeamLoyaltyManger;
import net.bumblebee.claysoldiers.team.loyalty.TeamPlayerData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.List;

public final class ClayTeamPlayerDataPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, Single> STREAM_CODEC_SINGLE = StreamCodec.composite(TeamLoyaltyManger.STREAM_CODEC_TEAM_PLAYER, Single::getPair, Single::create);
    public static final StreamCodec<RegistryFriendlyByteBuf, Remove> STREAM_CODEC_REMOVE = TeamLoyaltyManger.KEY_STREAM_CODEC.map(Remove::new, Remove::teamId).cast();
    public static final StreamCodec<RegistryFriendlyByteBuf, Creation> STREAM_CODEC_CREATION = StreamCodec.composite(TeamLoyaltyManger.STREAM_CODEC_TEAM_PLAYER_DATA, Creation::data, ByteBufCodecs.BOOL, Creation::reload, Creation::new);

    public record Single(ResourceKey<ClayMobTeam> teamId, TeamPlayerData.PlayerData player) implements IClientPayload {
        public static final Type<Single> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_team_player_data_single"));

        private static Single create(Pair<ResourceKey<ClayMobTeam>, TeamPlayerData.PlayerData> pair) {
            return new Single(pair.getFirst(), pair.getSecond());
        }

        private Pair<ResourceKey<ClayMobTeam>, TeamPlayerData.PlayerData> getPair() {
            return Pair.of(teamId, player);
        }

        @Override
        public Type<Single> type() {
            return ID;
        }

        @Override
        public void handleClient(NetworkManger.PayloadContext context) {
            TeamLoyaltyManger.updateClientTeamPlayerData(teamId, player);
        }
    }

    public record Remove(ResourceKey<ClayMobTeam> teamId) implements IClientPayload {
        public static final Type<Remove> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_team_player_data_remove"));

        @Override
        public Type<Remove> type() {
            return ID;
        }

        @Override
        public void handleClient(NetworkManger.PayloadContext context) {
            TeamLoyaltyManger.removeClientTeamPlayerData(teamId);
        }
    }

    public record Creation(List<Pair<ResourceKey<ClayMobTeam>, TeamPlayerData.PlayerData>> data,
                           boolean reload) implements IClientPayload {
        public static final Type<Creation> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_team_player_data_create"));

        @Override
        public Type<Creation> type() {
            return ID;
        }

        @Override
        public void handleClient(NetworkManger.PayloadContext context) {
            TeamLoyaltyManger.createClientTeamPlayerData(data, reload);
        }
    }
}
