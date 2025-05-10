package net.bumblebee.claysoldiers.team;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.ToIntFunction;

public class ClayMobTeam {
    public static final Codec<ClayMobTeam> CODEC_JSON = RecordCodecBuilder.create(in -> in.group(
            Codec.STRING.fieldOf("name").forGetter(ClayMobTeam::getName),
            ColorHelper.CODEC.optionalFieldOf("color", ColorHelper.EMPTY).forGetter(c -> c.color),
            Codec.BOOL.optionalFieldOf("friendly_fire", false).forGetter(ClayMobTeam::isFriendlyFireAllowed),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("from", Items.AIR).forGetter(ClayMobTeam::getGetFromOrAir),
            SoldierPropertyMap.CODEC_FOR_NON_ITEM.optionalFieldOf("properties", SoldierPropertyMap.EMPTY_MAP).forGetter(ClayMobTeam::getProperties),
            PlayerUUIDAndName.CODEC.listOf().optionalFieldOf("players", List.of()).forGetter(c -> c.players)
    ).apply(in, ClayMobTeam::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClayMobTeam> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClayMobTeam::getName,
            ColorHelper.STREAM_CODEC, c -> c.color,
            ByteBufCodecs.BOOL, ClayMobTeam::isFriendlyFireAllowed,
            ByteBufCodecs.registry(Registries.ITEM), t -> t.getFrom == null ? Items.AIR : t.getFrom,
            SoldierPropertyMap.STREAM_CODEC, ClayMobTeam::getProperties,
            PlayerUUIDAndName.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), c -> c.players,
            ClayMobTeam::new
    );

    public static final String TEAM_ID_TAG = "team_id";
    private static final String FORMATTED_TEAM_ID_TAG = "%s_" + TEAM_ID_TAG;

    private final String name;
    private final ColorHelper color;
    private final boolean friendlyFire;
    @Nullable
    private final Item getFrom;
    private final SoldierPropertyMap properties;
    private final List<PlayerUUIDAndName> players;
    private final Collection<UUID> playerUUIDs;
    private final List<String> playerNames;

    private ClayMobTeam(String name, ColorHelper color, boolean friendlyFire, @NotNull Item getFrom, SoldierPropertyMap properties, Collection<PlayerUUIDAndName> players) {
        this.name = name;
        this.color = color;
        this.friendlyFire = friendlyFire;
        this.getFrom = getFrom == Items.AIR ? null : getFrom;
        this.properties = properties;
        this.players = List.copyOf(players);
        this.playerUUIDs = players.stream().map(PlayerUUIDAndName::uuid).toList();
        this.playerNames = players.stream().map(PlayerUUIDAndName::name).toList();
    }

    protected ClayMobTeam(String name, ColorHelper color, boolean friendlyFire, @NotNull Item getFrom) {
        this(name, color, friendlyFire, getFrom, SoldierPropertyMap.EMPTY_MAP, List.of());
    }

    private ClayMobTeam(String name, ColorHelper color, boolean friendlyFire, @NotNull Item getFrom, SoldierPropertyMap properties) {
        this(name, color, friendlyFire, getFrom, properties, List.of());
    }

    public SoldierPropertyMap getProperties() {
        return properties;
    }

    /**
     * Returns the Language Component of this team with the color already applied.
     *
     * @param colorFunction converts the {@code ColorHelper} to its color.
     * @return display name of the team
     */
    public Component getDisplayNameWithColor(ToIntFunction<ColorHelper> colorFunction) {
        return Component.literal(getName()).withColor(colorFunction.applyAsInt(color));
    }

    /**
     * Returns the localized name of the Team.
     *
     * @return the localized name of the Team
     */
    public Component getDisplayName() {
        return Component.literal(getName());
    }

    private String getName() {
        return name;
    }

    /**
     * Returns the dynamic color of this Team.
     */
    public int getColor(LivingEntity livingEntity, float partialTick) {
        return color.getColor(livingEntity, partialTick);
    }

    /**
     * Returns the dynamic color of this Team.
     */
    public int getColor(int offsetStart, int tick, float partialTick) {
        return color.getColor(offsetStart, tick, partialTick);
    }

    /**
     * Returns the {@code Item} associated with this team.
     *
     * @return the {@code Item} associated with this team
     */
    @Nullable
    public Item getGetFrom() {
        return getFrom;
    }

    private Item getGetFromOrAir() {
        return getFrom != null ? getFrom : Items.AIR;
    }

    /**
     * Returns whether clay soldier can hurt other team members.
     */
    public boolean isFriendlyFireAllowed() {
        return friendlyFire;
    }

    /**
     * Returns whether this team is can cooperate with other members of this team.
     *
     * @return whether this team is can cooperate
     */
    public boolean isCooperative() {
        return true;
    }

    @UnmodifiableView
    public Collection<UUID> getPlayers() {
        return playerUUIDs;
    }
    @UnmodifiableView
    public List<String> getPlayerNames() {
        return playerNames;
    }

    public boolean canBeUsed(Player player) {
        return players.isEmpty() || players.contains(new PlayerUUIDAndName(player.getGameProfile()));
    }

    public boolean hasPlayer() {
        return !players.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        ClayMobTeam otherTeam = (ClayMobTeam) other;
        return color.equals(otherTeam.color)
                && friendlyFire == otherTeam.friendlyFire
                && name.equalsIgnoreCase(otherTeam.name)
                && Objects.equals(players, otherTeam.players);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, color, friendlyFire);
    }

    @Override
    public String toString() {
        return "ClayMobTeam{" +
                name + '\'' +
                ", " + color +
                (friendlyFire ? " friendlyFire" : "") +
                (playerNames.isEmpty() ? "" : playerNames) +
                '}';
    }


    /**
     * Saves this team to a given {@code CompoundTag}.
     *
     * @param key the key to save
     * @param tag tag the tag to save to
     */
    public static void save(ResourceLocation key, CompoundTag tag) {
        tag.putString(TEAM_ID_TAG, key.toString());
    }

    /**
     * Reads the {@code ClayMobTeamId} from the given {@code CompoundTag}
     *
     * @param tag the tag to read from
     */
    public static ResourceLocation read(CompoundTag tag) {
        return ResourceLocation.parse(tag.getString(TEAM_ID_TAG));
    }

    /**
     * Saves this team to a given {@code CompoundTag}.
     *
     * @param key    the key to save
     * @param tag    tag the tag to save to
     * @param prefix the prefix to distinguish this team from the normal team
     */
    public static void save(ResourceLocation key, CompoundTag tag, String prefix) {
        tag.putString(FORMATTED_TEAM_ID_TAG.formatted(prefix), key.toString());
    }

    /**
     * Reads the {@code ClayMobTeamId} from the given {@code CompoundTag}
     *
     * @param tag    the tag to read from
     * @param prefix the prefix to distinguish this team from the normal team
     */
    public static ResourceLocation read(CompoundTag tag, String prefix) {
        return ResourceLocation.parse(tag.getString(FORMATTED_TEAM_ID_TAG.formatted(prefix)));
    }

    public static Builder of(String name, ColorHelper color) {
        return new Builder(name, color);
    }

    private record PlayerUUIDAndName(UUID uuid, String name) {
        public static final Codec<PlayerUUIDAndName> CODEC = RecordCodecBuilder.create(in -> in.group(
                UUIDUtil.CODEC.fieldOf("uuid").forGetter(PlayerUUIDAndName::uuid),
                Codec.STRING.fieldOf("name").forGetter(PlayerUUIDAndName::name)
        ).apply(in, PlayerUUIDAndName::new));
        public static final StreamCodec<ByteBuf, PlayerUUIDAndName> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, PlayerUUIDAndName::uuid, ByteBufCodecs.STRING_UTF8,
                PlayerUUIDAndName::name, PlayerUUIDAndName::new
        );

        private PlayerUUIDAndName(GameProfile profile) {
            this(profile.getId(), profile.getName());
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            PlayerUUIDAndName that = (PlayerUUIDAndName) o;
            return Objects.equals(uuid, that.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(uuid);
        }
    }

    public static class Builder {
        private SoldierPropertyMap properties = SoldierPropertyMap.EMPTY_MAP;
        private Item getFrom = Items.AIR;
        private boolean friendlyFire = false;
        private final String name;
        private final ColorHelper color;

        public Builder(String name, ColorHelper color) {
            this.name = name;
            this.color = color;
        }

        public Builder setProperties(SoldierPropertyMap properties) {
            this.properties = properties;
            return this;
        }

        public Builder setGetFrom(@NotNull Item getFrom) {
            this.getFrom = getFrom;
            return this;
        }

        public Builder allowFriendlyFire() {
            this.friendlyFire = true;
            return this;
        }

        public ClayMobTeam build() {
            return new ClayMobTeam(name, color, friendlyFire, getFrom, properties);
        }
    }
}
