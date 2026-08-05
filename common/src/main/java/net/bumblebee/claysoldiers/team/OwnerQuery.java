package net.bumblebee.claysoldiers.team;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.team.loyalty.TeamLoyaltyManger;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;


public class OwnerQuery {
    public static final Codec<OwnerQuery> CODEC = RecordCodecBuilder.create(in -> in.group(
            Type.CODEC.fieldOf("type").forGetter(s -> s.type),
            ResourceKey.codec(ModRegistries.CLAY_MOB_TEAMS).optionalFieldOf("team").forGetter(s -> Optional.ofNullable(s.team)),
            UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(s -> Optional.ofNullable(s.owner))
    ).apply(in, (t, team, owner) -> create(t, team.orElse(null), owner.orElse(null))));

    private static final OwnerQuery NONE = new OwnerQuery(Type.NONE, null, null);

    private final Type type;
    private final @Nullable ResourceKey<ClayMobTeam> team;
    private final @Nullable UUID owner;

    private OwnerQuery(Type type, @Nullable ResourceKey<ClayMobTeam> team, @Nullable UUID owner) {
        this.type = type;
        this.team = team;
        this.owner = owner;
    }

    private static OwnerQuery create(Type type, @Nullable ResourceKey<ClayMobTeam> team, @Nullable UUID owner) {
        if ((type == Type.STORED && owner == null) ||
                (type == Type.TEAM && team == null) ||
                (type == Type.NONE)
        ) {
            return NONE;
        }

        return new OwnerQuery(type, team, owner);
    }

    public static OwnerQuery none() {
        return NONE;
    }

    public static OwnerQuery team(@NotNull Holder.Reference<ClayMobTeam> teamReference) {
        return new OwnerQuery(Type.TEAM, teamReference.key(), null);
    }

    public static OwnerQuery owner(@NotNull UUID owner) {
        return new OwnerQuery(Type.STORED, null, owner);
    }

    public @Nullable UUID getOwner(ServerLevel level) {
        return switch (type) {
            case NONE -> null;
            case STORED -> owner;
            case TEAM -> {
                var data = TeamLoyaltyManger.getTeamPlayerData(level).getPlayerForTeam(team);
                yield data == null ? null : data.getUUID();
            }
        };
    }

    private enum Type implements StringRepresentable {
        NONE("none"),
        STORED("stored"),
        TEAM("team");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final String serializedName;

        Type(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
