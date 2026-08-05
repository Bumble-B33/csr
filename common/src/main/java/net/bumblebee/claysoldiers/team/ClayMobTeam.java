package net.bumblebee.claysoldiers.team;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.ToIntFunction;

public class ClayMobTeam {
    public static final Codec<ResourceKey<ClayMobTeam>> KEY_CODEC = ResourceKey.codec(ModRegistries.CLAY_MOB_TEAMS);
    public static final StreamCodec<ByteBuf, ResourceKey<ClayMobTeam>> KEY_STREAM_CODE = ResourceKey.streamCodec(ModRegistries.CLAY_MOB_TEAMS);

    public static final Codec<ClayMobTeam> CODEC_JSON = RecordCodecBuilder.create(in -> in.group(
            Codec.STRING.fieldOf("name").forGetter(ClayMobTeam::getName),
            ColorHelper.CODEC.optionalFieldOf("color", ColorHelper.CLAY_COLOR).forGetter(c -> c.color),
            Codec.BOOL.optionalFieldOf("friendly_fire", false).forGetter(ClayMobTeam::isFriendlyFireAllowed),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("from", Items.AIR).forGetter(ClayMobTeam::getGetFromOrAir),
            SoldierPropertyMap.CODEC_FOR_NON_ITEM.optionalFieldOf("properties", SoldierPropertyMap.EMPTY_MAP).forGetter(ClayMobTeam::getProperties),
            Codec.BOOL.optionalFieldOf("tamable", true).forGetter(ClayMobTeam::canBeTamed),
            PlayerUUIDAndName.CODEC.listOf().optionalFieldOf("players", List.of()).forGetter(c -> c.players)
    ).apply(in, ClayMobTeam::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Holder.Reference<ClayMobTeam>> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Holder.Reference<ClayMobTeam> decode(RegistryFriendlyByteBuf byteBuf) {
            var opt = byteBuf.registryAccess().lookupOrThrow(ModRegistries.CLAY_MOB_TEAMS).get(byteBuf.readVarInt());
            if (opt.isEmpty()) {
                ClaySoldiersCommon.ERROR_HANDLER.warn("StreamCodec with invalid id, for Clay Mob Team");
            }

            return opt.orElse(ClayMobTeamManger.getDefault(byteBuf.registryAccess()));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf o, Holder.Reference<ClayMobTeam> clayMobTeamHolder) {
            int id = o.registryAccess().lookupOrThrow(ModRegistries.CLAY_MOB_TEAMS).getId(clayMobTeamHolder.value());
            o.writeVarInt(id);
        }
    };
    public static final String TEAM_ID_TAG = "team_id";
    private static final String FORMATTED_TEAM_ID_TAG = "%s_" + TEAM_ID_TAG;

    private final String name;
    private final ColorHelper color;
    private final boolean friendlyFire;
    @Nullable
    private final Item getFrom;
    private final SoldierPropertyMap properties;
    private final List<PlayerUUIDAndName> players;
    private final List<String> playerNames;
    private final boolean tamable;

    private ClayMobTeam(String name, ColorHelper color, boolean friendlyFire, @NotNull Item getFrom, SoldierPropertyMap properties, boolean tamable, Collection<PlayerUUIDAndName> players) {
        this.name = name;
        this.color = color;
        this.friendlyFire = friendlyFire;
        this.getFrom = getFrom == Items.AIR ? null : getFrom;
        this.properties = properties;
        this.players = List.copyOf(players);
        this.playerNames = players.stream().map(PlayerUUIDAndName::name).toList();
        this.tamable = tamable;
        if (color.isEmpty()) {
            throw new IllegalArgumentException("Clay Team Color Cannot be Empty");
        }
    }

    protected ClayMobTeam(String name, ColorHelper color, boolean friendlyFire, boolean tamable, @NotNull Item getFrom) {
        this(name, color, friendlyFire, getFrom, SoldierPropertyMap.EMPTY_MAP, tamable, List.of());
    }

    private ClayMobTeam(String name, ColorHelper color, boolean friendlyFire, @NotNull Item getFrom, SoldierPropertyMap properties, boolean tamable) {
        this(name, color, friendlyFire, getFrom, properties, tamable, List.of());
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

    public ColorHelper getColor() {
        return color;
    }


    /**
     * Returns the dynamic color of this Team.
     */
    public int getColor(int offsetStart, int tick, float partialTick) {
        return color.getColor(offsetStart, tick, partialTick);
    }

    /**
     * Returns the dynamic color of this Team.
     */
    @Deprecated
    public int getColor(int offsetStart, float ageInTicks) {
        return color.getColor(offsetStart, ageInTicks);
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
     * @return whether this team can be loyal to any player.
     */
    public boolean canBeTamed() {
        return tamable;
    }

    @UnmodifiableView
    public List<String> getPlayerNames() {
        return playerNames;
    }

    public boolean canBeUsed(Player player) {
        return players.isEmpty() || players.contains(new PlayerUUIDAndName(player.getGameProfile()));
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
    public static void save(Identifier key, CompoundTag tag) {
        tag.putString(TEAM_ID_TAG, key.toString());
    }

    public static void save(ResourceKey<ClayMobTeam> key, CompoundTag tag) {
        tag.store(TEAM_ID_TAG, KEY_CODEC, key);
    }

    public static void save(Identifier key, ValueOutput tag) {
        tag.putString(TEAM_ID_TAG, key.toString());
    }

    /**
     * Reads the {@code ClayMobTeamId} from the given {@code CompoundTag}
     *
     * @param tag the tag to read from
     */
    public static Optional<Identifier> read(CompoundTag tag) {
        return tag.getString(TEAM_ID_TAG).map(Identifier::parse);
    }

    public static Optional<Identifier> read(ValueInput tag) {
        return tag.getString(TEAM_ID_TAG).map(Identifier::parse);
    }

    public static void store(Holder.Reference<ClayMobTeam> team, ValueOutput tag, String prefix) {
        tag.store(FORMATTED_TEAM_ID_TAG.formatted(prefix), KEY_CODEC, team.key());
    }

    public static Optional<Holder.Reference<ClayMobTeam>> read(ValueInput tag, String prefix, RegistryAccess registry) {
        return tag.read(FORMATTED_TEAM_ID_TAG.formatted(prefix), KEY_CODEC).flatMap(registry::get);
    }

    public static void store(Holder.Reference<ClayMobTeam> team, ValueOutput tag) {
        tag.store(TEAM_ID_TAG, KEY_CODEC, team.key());
    }

    public static Optional<Holder.Reference<ClayMobTeam>> read(ValueInput tag, RegistryAccess registry) {
        return tag.read(TEAM_ID_TAG, KEY_CODEC).flatMap(registry::get);
    }

    /**
     * Saves this team to a given {@code CompoundTag}.
     *
     * @param key    the key to save
     * @param tag    tag the tag to save to
     * @param prefix the prefix to distinguish this team from the normal team
     */
    public static void save(Identifier key, ValueOutput tag, String prefix) {
        tag.putString(FORMATTED_TEAM_ID_TAG.formatted(prefix), key.toString());
    }

    /**
     * Reads the {@code ClayMobTeamId} from the given {@code CompoundTag}
     *
     * @param tag    the tag to read from
     * @param prefix the prefix to distinguish this team from the normal team
     */
    public static Identifier read(ValueInput tag, String prefix) {
        return Identifier.parse(tag.getString(FORMATTED_TEAM_ID_TAG.formatted(prefix)).orElseThrow());
    }

    public static Builder of(String name, ColorHelper color) {
        return new Builder(name, color);
    }

    private record PlayerUUIDAndName(UUID uuid, String name) {
        public static final Codec<PlayerUUIDAndName> CODEC = RecordCodecBuilder.create(in -> in.group(
                UUIDUtil.CODEC.fieldOf("uuid").forGetter(PlayerUUIDAndName::uuid),
                Codec.STRING.fieldOf("name").forGetter(PlayerUUIDAndName::name)
        ).apply(in, PlayerUUIDAndName::new));

        private PlayerUUIDAndName(GameProfile profile) {
            this(profile.id(), profile.name());
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
        private boolean tamable = true;

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

        public Builder disableTaming() {
            this.tamable = false;
            return this;
        }

        public ClayMobTeam build() {
            return new ClayMobTeam(name, color, friendlyFire, getFrom, properties, tamable);
        }
    }
}
