package net.bumblebee.claysoldiers.team;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.BiConsumer;

public interface TeamPlayerData {
    Codec<PlayerData> PLAYER_DATA_CODEC = RecordCodecBuilder.create(in -> in.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(PlayerData::getUUID),
            ComponentSerialization.CODEC.fieldOf("lastKnowName").forGetter(PlayerData::getLastDisplayName)
    ).apply(in, PlayerData::new));

    /**
     * Makes the given {@code ClayMobTeam} loyal to the given {@code Player} if it is not loyal already.
     * @throws UnsupportedOperationException if called from the client
     * @return whether the {@code ClayMobTeam} could be claimed
     */
    boolean putPlayerIfAbsent(ResourceLocation teamId, Player player);
    /**
     * @return the {@code Player} the given team is loyal to.
     */
    @Nullable
    PlayerData getPlayerForTeam(ResourceLocation teamId);
    /**
     * @return the last timestamp this data was changed.
     */
    long lastChangeTime();
    /**
     * Performs an action for each {@code ClayMobTeam} that is loyal to a {@code Player}
     * @param action the action to perform
     */
    void forEach(BiConsumer<ResourceLocation, PlayerData> action);

    /**
     * Updates the player name of the {@code ClayMobTeam} that is loyal to the given {@code Player}.
     */
    void updatePlayerName(ResourceLocation teamId, Player player);

    class PlayerData {
        private final UUID player;
        private Component lastKnowName;

        public PlayerData(UUID player, Component lastKnowName) {
            this.player = player;
            this.lastKnowName = lastKnowName;
        }

        public static PlayerData of(Player player) {
            return new PlayerData(player.getUUID(), player.getDisplayName());
        }

        public UUID getUUID() {
            return player;
        }

        public void setLastKnowName(Component lastKnowName) {
            this.lastKnowName = lastKnowName;
        }

        public boolean is(Player player) {
            return getUUID().equals(player.getUUID());
        }

        public Component getLastDisplayName() {
            return lastKnowName;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            PlayerData data = (PlayerData) o;
            return player.equals(data.player);
        }

        @Override
        public int hashCode() {
            return player.hashCode();
        }

        @Override
        public String toString() {
            return "Player(%s)".formatted(lastKnowName.getString());
        }
    }
}
